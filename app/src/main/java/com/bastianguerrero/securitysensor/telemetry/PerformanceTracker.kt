package com.bastianguerrero.securitysensor.telemetry

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.SystemClock
import android.util.Log
import android.view.FrameMetrics
import android.view.Window
import androidx.annotation.RequiresApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

object PerformanceTracker {
    private const val TAG = "PerformanceTracker"
    private const val SAMPLE_INTERVAL_MS = 1000L

    private var appStartTimeMs: Long = 0L
    private var initialLoadTimeSec: Float = -1.0f

    private var sessionStartTimeMs: Long = 0L
    private var isSessionRunning: Boolean = false

    // CPU Metrics
    private var lastProcessCpuTimeMs: Long = 0L
    private var lastWallTimeMs: Long = 0L
    private var cpuSum: Float = 0.0f
    private var cpuSampleCount: Int = 0

    // Memory Metrics (MB)
    private var memSumMb: Float = 0.0f
    private var memMaxMb: Float = 0.0f
    private var memSampleCount: Int = 0

    // FPS Metrics
    private var fpsSum: Float = 0.0f
    private var fpsCount: Int = 0
    private var fpsMin: Int = Int.MAX_VALUE
    private var fpsMax: Int = 0

    // Handler Thread for background sampling
    private var samplerThread: HandlerThread? = null
    private var samplerHandler: Handler? = null

    // FrameMetrics listener reference
    @RequiresApi(Build.VERSION_CODES.N)
    private var frameMetricsListener: Window.OnFrameMetricsAvailableListener? = null

    fun onAppCreated() {
        if (appStartTimeMs == 0L) {
            appStartTimeMs = SystemClock.elapsedRealtime()
            Log.d(TAG, "Inicio de aplicación registrado a los $appStartTimeMs ms")
        }
    }

    fun markInitialLoadCompleted() {
        if (initialLoadTimeSec < 0.0f && appStartTimeMs > 0L) {
            val elapsedMs = SystemClock.elapsedRealtime() - appStartTimeMs
            initialLoadTimeSec = elapsedMs / 1000.0f
            Log.i(TAG, "Carga inicial completada en $initialLoadTimeSec segundos")
        }
    }

    @Synchronized
    fun startSession() {
        if (isSessionRunning) return

        isSessionRunning = true
        sessionStartTimeMs = SystemClock.elapsedRealtime()

        // Reset metrics
        cpuSum = 0.0f
        cpuSampleCount = 0
        lastProcessCpuTimeMs = Process.getElapsedCpuTime()
        lastWallTimeMs = SystemClock.elapsedRealtime()

        memSumMb = 0.0f
        memMaxMb = 0.0f
        memSampleCount = 0

        fpsSum = 0.0f
        fpsCount = 0
        fpsMin = Int.MAX_VALUE
        fpsMax = 0

        // Start sampler thread
        samplerThread = HandlerThread("PerfSamplerThread").apply {
            start()
            samplerHandler = Handler(looper)
            samplerHandler?.post(sampleRunnable)
        }

        Log.i(TAG, "Sesión de seguimiento de rendimiento iniciada.")
    }

    @Synchronized
    fun stopAndExportSession(context: Context): File? {
        if (!isSessionRunning) return null

        isSessionRunning = false
        val sessionEndTimeMs = SystemClock.elapsedRealtime()
        val durationSec = (sessionEndTimeMs - sessionStartTimeMs) / 1000.0f

        // Stop background sampler
        samplerHandler?.removeCallbacks(sampleRunnable)
        samplerThread?.quitSafely()
        samplerThread = null
        samplerHandler = null

        val cpuAvg = if (cpuSampleCount > 0) cpuSum / cpuSampleCount else 0.0f
        val memAvg = if (memSampleCount > 0) memSumMb / memSampleCount else 0.0f
        val finalFpsAvg = if (fpsCount > 0) fpsSum / fpsCount else 0.0f
        val finalFpsMin = if (fpsMin != Int.MAX_VALUE) fpsMin else 0
        val finalFpsMax = fpsMax

        val timestampStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val summary = PerformanceSummary(
            timestamp = timestampStr,
            initialLoadTimeSec = if (initialLoadTimeSec > 0) initialLoadTimeSec else 0.0f,
            cpuAvgPercent = cpuAvg,
            fpsAvg = finalFpsAvg,
            fpsMin = finalFpsMin,
            fpsMax = finalFpsMax,
            memAvgMb = memAvg,
            memMaxMb = memMaxMb,
            sessionDurationSec = durationSec
        )

        Log.i(TAG, "Sesión finalizada. Carga inicial: ${summary.initialLoadTimeSec}s, CPU Prom: ${summary.cpuAvgPercent}%, FPS Prom: ${summary.fpsAvg}, Mem Prom: ${summary.memAvgMb}MB")

        return CsvExporter.saveSessionSummary(context, summary)
    }

    private val sampleRunnable = object : Runnable {
        override fun run() {
            if (!isSessionRunning) return

            sampleCpuAndMemory()
            samplerHandler?.postDelayed(this, SAMPLE_INTERVAL_MS)
        }
    }

    private fun sampleCpuAndMemory() {
        val currentCpuMs = Process.getElapsedCpuTime()
        val currentWallMs = SystemClock.elapsedRealtime()
        val cores = Runtime.getRuntime().availableProcessors()

        val deltaCpu = currentCpuMs - lastProcessCpuTimeMs
        val deltaWall = currentWallMs - lastWallTimeMs

        if (deltaWall > 0) {
            val cpuPercent = (deltaCpu.toFloat() / (deltaWall * cores)) * 100.0f
            cpuSum += min(100.0f, max(0.0f, cpuPercent))
            cpuSampleCount++
        }
        lastProcessCpuTimeMs = currentCpuMs
        lastWallTimeMs = currentWallMs

        val runtime = Runtime.getRuntime()
        val usedHeapBytes = runtime.totalMemory() - runtime.freeMemory()
        val nativeBytes = Debug.getNativeHeapAllocatedSize()
        val totalMemMb = (usedHeapBytes + nativeBytes) / (1024.0f * 1024.0f)

        memSumMb += totalMemMb
        if (totalMemMb > memMaxMb) {
            memMaxMb = totalMemMb
        }
        memSampleCount++
    }

    fun attachToActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val window = activity.window
            var listener = frameMetricsListener
            if (listener == null) {
                listener = Window.OnFrameMetricsAvailableListener { _, frameMetrics, _ ->
                    val frameDurationNs = frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION)
                    if (frameDurationNs > 0) {
                        val frameFps = (1_000_000_000.0f / frameDurationNs).toInt()
                        if (frameFps in 1..240) {
                            recordFps(frameFps)
                        }
                    }
                }
                frameMetricsListener = listener
            }
            try {
                window.addOnFrameMetricsAvailableListener(listener, Handler(activity.mainLooper))
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo registrar OnFrameMetricsAvailableListener", e)
            }
        }
    }

    fun detachFromActivity(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val listener = frameMetricsListener
            if (listener != null) {
                try {
                    activity.window.removeOnFrameMetricsAvailableListener(listener)
                } catch (e: Exception) {
                    Log.w(TAG, "Error al remover OnFrameMetricsAvailableListener", e)
                }
            }
        }
    }

    private fun recordFps(fps: Int) {
        fpsSum += fps
        fpsCount++
        if (fps < fpsMin) fpsMin = fps
        if (fps > fpsMax) fpsMax = fps
    }
}

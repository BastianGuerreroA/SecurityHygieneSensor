package com.bastianguerrero.securitysensor.telemetry

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.util.Locale

data class PerformanceSummary(
    val timestamp: String,
    val initialLoadTimeSec: Float,
    val cpuAvgPercent: Float,
    val fpsAvg: Float,
    val fpsMin: Int,
    val fpsMax: Int,
    val memAvgMb: Float,
    val memMaxMb: Float,
    val sessionDurationSec: Float
)

object CsvExporter {
    private const val TAG = "CsvExporter"
    private const val CSV_FILENAME = "performance_metrics.csv"

    fun saveSessionSummary(context: Context, summary: PerformanceSummary): File? {
        return try {
            val docsDir = context.filesDir
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }

            val csvFile = File(docsDir, CSV_FILENAME)

            FileWriter(csvFile, true).use { writer ->
                writer.append("--- SESION DE RENDIMIENTO ---\n")
                writer.append(String.format(Locale.US, "Fecha_Hora,%s\n", summary.timestamp))
                writer.append(String.format(Locale.US, "Carga_Inicial_s,%.3f\n", summary.initialLoadTimeSec))
                writer.append(String.format(Locale.US, "CPU_Prom_Pct,%.2f%%\n", summary.cpuAvgPercent))
                writer.append(String.format(Locale.US, "FPS_Prom,%.1f\n", summary.fpsAvg))
                writer.append(String.format(Locale.US, "FPS_Min,%d\n", summary.fpsMin))
                writer.append(String.format(Locale.US, "FPS_Max,%d\n", summary.fpsMax))
                writer.append(String.format(Locale.US, "Mem_Prom_MB,%.2f MB\n", summary.memAvgMb))
                writer.append(String.format(Locale.US, "Mem_Max_MB,%.2f MB\n", summary.memMaxMb))
                writer.append(String.format(Locale.US, "Duracion_Sesion_s,%.2f s\n\n", summary.sessionDurationSec))
            }

            Log.i(TAG, "Métricas de rendimiento guardadas exitosamente en: ${csvFile.absolutePath}")
            csvFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar reporte CSV de rendimiento", e)
            null
        }
    }

    fun getCsvFile(context: Context): File {
        val docsDir = context.filesDir
        return File(docsDir, CSV_FILENAME)
    }
}

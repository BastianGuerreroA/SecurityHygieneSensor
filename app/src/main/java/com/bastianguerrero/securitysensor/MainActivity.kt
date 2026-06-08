package com.bastianguerrero.securitysensor

import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.widget.ImageView

import com.bastianguerrero.securitysensor.ui.LsgViewModel
import androidx.activity.viewModels
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private val COLOR_BLUE  = Color.parseColor("#A3C7FF")
    private val COLOR_GREEN = Color.parseColor("#3FB950")
    private val COLOR_AMBER = Color.parseColor("#D29922")
    private val COLOR_RED   = Color.parseColor("#F85149")
    private val COLOR_MUTED = Color.parseColor("#2D333B")

    // Agrega esta propiedad en la clase
    private val lsgViewModel: LsgViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            insets
        }

        findViewById<Button>(R.id.btnScan).setOnClickListener { runScan() }

        // Observa el nombre de usuario
        lsgViewModel.userName.observe(this) { name ->
            if (name.isNotEmpty()) {
                findViewById<TextView>(R.id.tvUserWelcome).text = "Hola, $name"
            }
        }

        // Observa si el envío del puntaje a LSG fue exitoso o no
        lsgViewModel.ingestResult.observe(this) { success ->
            when (success) {
                true  -> Toast.makeText(this, "✓ Puntaje enviado a LSG", Toast.LENGTH_SHORT).show()
                false -> Toast.makeText(this, "✗ Error al enviar puntaje", Toast.LENGTH_SHORT).show()
                null  -> { }
            }
        }
    }

    private fun runScan() {
        val scanner = SecurityScanner(this)
        val btn = findViewById<Button>(R.id.btnScan)
        btn.isEnabled = false
        btn.text = "ESCANEANDO..."

        // --- Recopilar datos ---
        val pinOk    = scanner.isDeviceSecured()
        val bioState = scanner.getBiometricStatus()
        val patch    = scanner.getSecurityPatchLevel()
        val appsRisk = scanner.areUnknownSourcesAllowed()

        // --- Puntaje ---
        val pinPts   = if (pinOk) 40 else 0
        val bioPts   = bioState.points  // 15 si READY, 0 si no
        val patchPts = if (isPatchRecent(patch)) 30 else 0
        val appsPts  = if (!appsRisk) 15 else 0
        val total    = pinPts + bioPts + patchPts + appsPts

        // --- Color según puntaje ---
        val percentage = total.toFloat() / SecurityScanner.MAX_SCORE

        val mainColor = when {
            percentage >= 0.75f -> COLOR_GREEN
            percentage >= 0.40f -> COLOR_AMBER
            else                -> COLOR_BLUE
        }
        findViewById<ImageView>(R.id.ivShield).setColorFilter(mainColor)

        val statusText = when {
            percentage >= 0.75f -> "SEGURO"
            percentage >= 0.40f -> "RIESGO MEDIO"
            else                -> "RIESGO ALTO"
        }

        // --- Actualizar UI ---
        val progressBar = findViewById<ProgressBar>(R.id.circularProgressBar)
        ObjectAnimator.ofInt(progressBar, "progress", 0, total)
            .setDuration(1000)
            .start()

        findViewById<TextView>(R.id.tvScore).apply {
            text = total.toString()
            setTextColor(mainColor)
        }

        findViewById<TextView>(R.id.tvStatus).apply {
            text = statusText
            setTextColor(mainColor)
        }

        setCard(R.id.dotPin, R.id.tvPinVal, R.id.tvPinPts,
            if (pinOk) "PIN / Patrón activo" else "Sin bloqueo configurado",
            pinPts, if (pinOk) COLOR_GREEN else COLOR_RED)

        setCard(R.id.dotBio, R.id.tvBioVal, R.id.tvBioPts,
            bioState.message, bioPts,
            if (bioState == BiometricState.READY) COLOR_GREEN else COLOR_AMBER)

        setCard(R.id.dotPatch, R.id.tvPatchVal, R.id.tvPatchPts,
            patch + if (patchPts > 0) "" else " — desactualizado",
            patchPts, if (patchPts > 0) COLOR_GREEN else COLOR_AMBER)

        setCard(R.id.dotApps, R.id.tvAppsVal, R.id.tvAppsPts,
            if (!appsRisk) "Bloqueadas" else "Habilitadas — riesgo",
            appsPts, if (!appsRisk) COLOR_GREEN else COLOR_RED)

        // Preparar el detalle técnico para el raw_payload según requerimiento
        val daysSinceUpdate = try {
            val patchDate = LocalDate.parse(patch, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ChronoUnit.DAYS.between(patchDate, LocalDate.now()).toInt()
        } catch (e: Exception) { -1 }

        val deviceSecurityDetails = mapOf(
            "screen_lock_enabled" to pinOk,
            "screen_lock_type" to if (pinOk) "SECURE_LOCK" else "NONE",
            "biometric_enabled" to (bioState == BiometricState.READY),
            "unknown_sources_enabled" to appsRisk,
            "last_security_update_days" to daysSinceUpdate
        )

        // Enviar resultado a LSG de forma automática tras el escaneo con el detalle técnico
        lsgViewModel.sendScanResult(total, deviceSecurityDetails)

        btn.isEnabled = true
        btn.text = "ESCANEAR DE NUEVO"
    }

    private fun setCard(dotId: Int, valId: Int, ptsId: Int,
                        value: String, pts: Int, color: Int) {
        findViewById<View>(dotId).backgroundTintList =
            android.content.res.ColorStateList.valueOf(color)
        findViewById<TextView>(valId).apply {
            text = value
            setTextColor(color)
        }
        findViewById<TextView>(ptsId).apply {
            text = "+$pts"
            setTextColor(if (pts > 0) color else COLOR_MUTED)
        }
    }

    private fun isPatchRecent(dateStr: String): Boolean {
        return try {
            val patch = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            ChronoUnit.MONTHS.between(patch, LocalDate.now()) <= 3
        } catch (e: Exception) { false }
    }
}
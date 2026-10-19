package com.bastianguerrero.securitysensor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bastianguerrero.securitysensor.telemetry.PerformanceTracker
import com.bastianguerrero.securitysensor.ui.LsgViewModel

/**
 * Actividad de autenticación de usuario.
 *
 * Provee la interfaz para ingreso de credenciales (nombre de usuario y contraseña),
 * gestiona los estados de validación local y carga visual, e inicia el flujo hacia
 * [MainActivity] una vez autenticado satisfactoriamente ante el servicio LSG.
 */
class LoginActivity : AppCompatActivity() {

    private val lsgViewModel: LsgViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsername  = findViewById<EditText>(R.id.etUsername)
        val etPassword  = findViewById<EditText>(R.id.etPassword)
        val btnLogin    = findViewById<Button>(R.id.btnLogin)
        val tvError     = findViewById<TextView>(R.id.tvLoginError)
        val pbLoading   = findViewById<ProgressBar>(R.id.pbLogin)

        lsgViewModel.loginResult.observe(this) { success ->
            pbLoading.visibility = View.GONE
            btnLogin.isEnabled   = true

            if (success == true) {
                goToScanner()
            } else if (success == false) {
                tvError.text       = "Credenciales incorrectas. Intenta nuevamente."
                tvError.visibility = View.VISIBLE
            }
        }

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                tvError.text       = "Completa todos los campos."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            tvError.visibility = View.GONE
            pbLoading.visibility = View.VISIBLE
            btnLogin.isEnabled   = false

            lsgViewModel.login(username, password)
        }
    }

    /**
     * Inicia [MainActivity] y finaliza la actividad actual para impedir el retorno a la pantalla de login.
     */
    private fun goToScanner() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // PerformanceTracker.markInitialLoadCompleted() // Telemetría deshabilitada temporalmente
        }
    }
}
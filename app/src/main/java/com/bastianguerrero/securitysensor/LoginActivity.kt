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

class LoginActivity : AppCompatActivity() {

    // Referencia al ViewModel que maneja la lógica de login
    private val lsgViewModel: LsgViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Referencias a los elementos visuales
        val etUsername  = findViewById<EditText>(R.id.etUsername)
        val etPassword  = findViewById<EditText>(R.id.etPassword)
        val btnLogin    = findViewById<Button>(R.id.btnLogin)
        val tvError     = findViewById<TextView>(R.id.tvLoginError)
        val pbLoading   = findViewById<ProgressBar>(R.id.pbLogin)

        // Observa el resultado del login desde el ViewModel
        lsgViewModel.loginResult.observe(this) { success ->
            // Oculta el loading
            pbLoading.visibility = View.GONE
            btnLogin.isEnabled   = true

            if (success == true) {
                // Login exitoso → ir al scanner
                goToScanner()
            } else if (success == false) {
                // Login fallido → mostrar error
                tvError.text       = "Credenciales incorrectas. Intenta nuevamente."
                tvError.visibility = View.VISIBLE
            }
        }

        // Acción del botón ingresar
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validación básica de campos vacíos
            if (username.isEmpty() || password.isEmpty()) {
                tvError.text       = "Completa todos los campos."
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            // Oculta error anterior, muestra loading y bloquea botón
            tvError.visibility = View.GONE
            pbLoading.visibility = View.VISIBLE
            btnLogin.isEnabled   = false

            // Llama al login en el ViewModel
            lsgViewModel.login(username, password)
        }
    }

    // Navega al scanner y cierra el login para que no se pueda volver atrás
    private fun goToScanner() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // PerformanceTracker.markInitialLoadCompleted() // Telemetría deshabilitada
        }
    }
}
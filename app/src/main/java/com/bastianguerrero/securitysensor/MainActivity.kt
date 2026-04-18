package com.bastianguerrero.securitysensor

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializamos el sensor
        val scanner = SecurityScanner(this)
        val statusBiometria = scanner.getBiometricStatus()


        // Imprimimos los resultados en Logcat
        android.util.Log.d("SENSOR_TESIS", "--- RESULTADOS DEL SENSOR ---")
        android.util.Log.d("SENSOR_TESIS", "PIN Configurado: ${scanner.isDeviceSecured()}")
        android.util.Log.d("SENSOR_TESIS", "Biometría: ${statusBiometria.message} ${statusBiometria.points}")
        android.util.Log.d("SENSOR_TESIS", "Parche de Seguridad: ${scanner.getSecurityPatchLevel()}")
        android.util.Log.d("SENSOR_TESIS", "Apps Desconocidas permitidas: ${scanner.areUnknownSourcesAllowed()}")
    }
}
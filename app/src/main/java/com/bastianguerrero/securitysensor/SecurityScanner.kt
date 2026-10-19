package com.bastianguerrero.securitysensor

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager


/**
 * Representa el estado de autenticación biométrica fuerte del dispositivo,
 * asociando el puntaje asignado y la descripción para el usuario.
 *
 * @property points Puntaje otorgado según el estado biométrico (máximo 15 puntos).
 * @property message Mensaje descriptivo para presentación en la interfaz.
 */
enum class BiometricState(val points: Int, val message: String) {
    READY(15, "Biometría fuerte lista (huella/face)"),
    NO_HARDWARE(0, "Sin hardware biométrico"),
    NOT_ENROLLED(0, "Hardware disponible pero sin huella registrada"),
    UNAVAILABLE(0, "Hardware biométrico no disponible ahora"),
    UNKNOWN(0, "Estado no reconocido")
}

/**
 * Escáner de higiene y seguridad para evaluar configuraciones de seguridad del dispositivo Android.
 *
 * Evalúa cuatro pilares clave:
 * 1. Bloqueo de pantalla (PIN, patrón o contraseña) (hasta 40 puntos).
 * 2. Disponibilidad y registro de biometría fuerte (hasta 15 puntos).
 * 3. Nivel de actualización del parche de seguridad del sistema operativo (hasta 30 puntos).
 * 4. Restricción de instalación de aplicaciones de orígenes desconocidos (hasta 15 puntos).
 *
 * @param context Contexto de la aplicación para acceder a servicios del sistema y configuración.
 */
class SecurityScanner(private val context: Context) {

    companion object {
        /** Puntaje máximo acumulable por el escáner (40 + 15 + 30 + 15 = 100). */
        const val MAX_SCORE = 100
    }

    /**
     * Verifica si el dispositivo cuenta con un mecanismo de bloqueo de pantalla seguro
     * configurado (PIN, patrón, contraseña o credencial biométrica vinculada).
     *
     * @return `true` si el dispositivo está asegurado con pantalla de bloqueo; `false` en caso contrario.
     */
    fun isDeviceSecured(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    /**
     * Evalúa el estado del hardware biométrico y registro de credenciales fuertes (Class 3 / Biometric Strong).
     *
     * @return Instancia de [BiometricState] correspondiente al estado detectado.
     */
    fun getBiometricStatus(): BiometricState {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS               -> BiometricState.READY
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE     -> BiometricState.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED   -> BiometricState.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE  -> BiometricState.UNAVAILABLE
            else                                             -> BiometricState.UNKNOWN
        }
    }

    /**
     * Obtiene la fecha del nivel del parche de seguridad del sistema operativo en formato ISO (YYYY-MM-DD).
     *
     * @return Cadena con la fecha del parche si la API es >= 23 (Marshmallow), o mensaje descriptivo alternativo.
     */
    fun getSecurityPatchLevel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "Desconocido (Versión muy antigua)"
        }
    }

    /**
     * Comprueba si el dispositivo permite la instalación de aplicaciones desde fuentes externas desconocidas.
     *
     * @return `true` si se permite la instalación desde orígenes desconocidos; `false` si está bloqueada o no definida.
     */
    fun areUnknownSourcesAllowed(): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS) == 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }
}
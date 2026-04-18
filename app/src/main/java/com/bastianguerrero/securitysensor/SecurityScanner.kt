package com.bastianguerrero.securitysensor

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager

//guarda el estado, el mensaje y los puntos
enum class BiometricState(val points: Int, val message: String) {
    READY(10, "Biometría fuerte lista (huella/face)"),
    NO_HARDWARE(0, "Sin hardware biométrico"),
    NOT_ENROLLED(0, "Hardware disponible pero sin huella registrada"),
    UNAVAILABLE(0, "Hardware biométrico no disponible ahora"),
    UNKNOWN(0, "Estado no reconocido")
}
class SecurityScanner(private val context: Context) {

    // 1. Captura la presencia de bloqueo de pantalla (PIN/Patrón/Contraseña)
    fun isDeviceSecured(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    // 2. Estado de Biometría (¿Está configurada y lista para usarse?)
    // 2. Tu función ahora devuelve esta clase en lugar de un String suelto
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

    // 3. Nivel de parche de seguridad de Android
    fun getSecurityPatchLevel(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else {
            "Desconocido (Versión muy antigua)"
        }
    }

    // 4. Verificación básica: ¿Permite instalar apps desconocidas?
    fun areUnknownSourcesAllowed(): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.INSTALL_NON_MARKET_APPS) == 1
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }
}
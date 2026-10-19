package com.bastianguerrero.securitysensor.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastianguerrero.securitysensor.data.repository.LsgRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de la lógica de presentación y sincronización con el repositorio LSG.
 *
 * Administra el flujo de autenticación, el envío concurrente de métricas de escaneo y ajuste de puntos,
 * y la cuenta regresiva periódica para el período de enfriamiento (cooldown) de 24 horas entre escaneos.
 */
class LsgViewModel : ViewModel() {

    private val repo = LsgRepository

    /** Estado del resultado del intento de login (`true` exitoso, `false` fallido, `null` en espera). */
    private val _loginResult = MutableLiveData<Boolean?>()
    val loginResult: LiveData<Boolean?> get() = _loginResult

    /** Estado del resultado de la transmisión del puntaje y ajuste de puntos a LSG. */
    private val _ingestResult = MutableLiveData<Boolean?>()
    val ingestResult: LiveData<Boolean?> get() = _ingestResult

    /** Nombre del usuario autenticado actualmente. */
    private val _userName = MutableLiveData<String>(repo.getUserName())
    val userName: LiveData<String> get() = _userName

    private var timerJob: Job? = null

    /** Indica si está permitido ejecutar un nuevo escaneo de seguridad. */
    private val _isScanAllowed = MutableLiveData<Boolean>(true)
    val isScanAllowed: LiveData<Boolean> get() = _isScanAllowed

    /** Representación formateada en "HH:mm:ss" del tiempo restante del cooldown, o null si está libre. */
    private val _remainingTimeFormatted = MutableLiveData<String?>()
    val remainingTimeFormatted: LiveData<String?> get() = _remainingTimeFormatted

    /**
     * Autentica al usuario contra el servicio LSG y actualiza el estado observable.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña de acceso.
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            val success = repo.login(username, password)
            if (success) {
                _userName.postValue(repo.getUserName())
            }
            _loginResult.postValue(success)
        }
    }

    /**
     * Despacha el resultado del escaneo de seguridad tanto al webhook del sensor como al ajuste
     * de puntos de la dimensión Mental en la plataforma LSG.
     *
     * @param score Puntaje numérico obtenido en el escaneo (0 a 100).
     * @param deviceSecurity Mapa con los detalles técnicos de configuración de seguridad del dispositivo.
     */
    fun sendScanResult(score: Int, deviceSecurity: Map<String, Any>) {
        viewModelScope.launch {
            val sensorSuccess = repo.ingestSensorData(score, deviceSecurity)
            val pointsSuccess = repo.adjustUserPoints(score)
            _ingestResult.postValue(sensorSuccess || pointsSuccess)
        }
    }

    /**
     * Inicializa y gestiona el temporizador de cuenta regresiva basado en la marca temporal del último escaneo.
     *
     * Si no ha transcurrido el tiempo límite de 24 horas, suspende la habilitación del escaneo y actualiza
     * cada segundo la cadena formateada de tiempo restante.
     *
     * @param timestamp Marca de tiempo en milisegundos (epoch) del último escaneo realizado.
     */
    fun setLastScanTimestamp(timestamp: Long) {
        timerJob?.cancel()
        if (timestamp == 0L) {
            _isScanAllowed.postValue(true)
            _remainingTimeFormatted.postValue(null)
            return
        }

        val cooldownMs = 24 * 60 * 60 * 1000L // 24 horas en milisegundos
        val initialTimeLeft = cooldownMs - (System.currentTimeMillis() - timestamp)
        if (initialTimeLeft <= 0) {
            _isScanAllowed.postValue(true)
            _remainingTimeFormatted.postValue(null)
            return
        }

        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val timeLeft = cooldownMs - (now - timestamp)

                if (timeLeft <= 0) {
                    _isScanAllowed.postValue(true)
                    _remainingTimeFormatted.postValue(null)
                    break
                } else {
                    _isScanAllowed.postValue(false)
                    val hours = timeLeft / (1000 * 60 * 60)
                    val minutes = (timeLeft % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (timeLeft % (1000 * 60)) / 1000
                    val formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                    _remainingTimeFormatted.postValue(formatted)
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
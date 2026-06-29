package com.bastianguerrero.securitysensor.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastianguerrero.securitysensor.data.repository.LsgRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LsgViewModel : ViewModel() {

    private val repo = LsgRepository

    // Resultado del login: true=éxito, false=fallo, null=esperando
    private val _loginResult = MutableLiveData<Boolean?>()
    val loginResult: LiveData<Boolean?> get() = _loginResult

    // Resultado del envío del puntaje al sensor y ajuste de puntos
    private val _ingestResult = MutableLiveData<Boolean?>()
    val ingestResult: LiveData<Boolean?> get() = _ingestResult

    // Información del usuario
    private val _userName = MutableLiveData<String>(repo.getUserName())
    val userName: LiveData<String> get() = _userName

    // Control de límite de escaneo de 24 horas
    private var timerJob: Job? = null
    private val _isScanAllowed = MutableLiveData<Boolean>(true)
    val isScanAllowed: LiveData<Boolean> get() = _isScanAllowed

    private val _remainingTimeFormatted = MutableLiveData<String?>()
    val remainingTimeFormatted: LiveData<String?> get() = _remainingTimeFormatted

    // ─── FUNCIÓN 1: Solo hace login y guarda el token ───────────────────────
    fun login(username: String, password: String) {
        viewModelScope.launch { //habre un hilo/proceso secundario para que no se peque la app
            val success = repo.login(username, password)
            if (success) {
                _userName.postValue(repo.getUserName())
            }
            _loginResult.postValue(success)
        }
    }

    // ─── FUNCIÓN 2: Envía el puntaje a LSG (Sensor webhook + Puntos base Mental) ───
    fun sendScanResult(score: Int, deviceSecurity: Map<String, Any>) {
        viewModelScope.launch {
            val sensorSuccess = repo.ingestSensorData(score, deviceSecurity)
            val pointsSuccess = if (sensorSuccess) repo.adjustUserPoints(score) else false
            _ingestResult.postValue(sensorSuccess && pointsSuccess)
        }
    }

    // Calcula el tiempo restante hasta completar las 24 horas de cooldown
    fun setLastScanTimestamp(timestamp: Long) {
        timerJob?.cancel()
        if (timestamp == 0L) {
            _isScanAllowed.postValue(true)
            _remainingTimeFormatted.postValue(null)
            return
        }

        timerJob = viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val timePassed = now - timestamp
                val cooldown = 24 * 60 * 60 * 1000L // 24 horas en milisegundos
                val timeLeft = cooldown - timePassed

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
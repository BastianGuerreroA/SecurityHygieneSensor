package com.bastianguerrero.securitysensor.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastianguerrero.securitysensor.data.repository.LsgRepository
import kotlinx.coroutines.launch

class LsgViewModel : ViewModel() {

    private val repo = LsgRepository

    // Resultado del login: true=éxito, false=fallo, null=esperando
    private val _loginResult = MutableLiveData<Boolean?>()
    val loginResult: LiveData<Boolean?> get() = _loginResult

    // Resultado del envío del puntaje al sensor
    private val _ingestResult = MutableLiveData<Boolean?>()
    val ingestResult: LiveData<Boolean?> get() = _ingestResult

    // Información del usuario
    private val _userName = MutableLiveData<String>(repo.getUserName())
    val userName: LiveData<String> get() = _userName

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

    // ─── FUNCIÓN 2: Solo envía el puntaje del sensor a LSG ──────────────────
    fun sendScanResult(score: Int, deviceSecurity: Map<String, Any>) {
        viewModelScope.launch {
            val success = repo.ingestSensorData(score, deviceSecurity)
            _ingestResult.postValue(success)
        }
    }
}
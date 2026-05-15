package com.bastianguerrero.securitysensor.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bastianguerrero.securitysensor.data.repository.LsgRepository
import kotlinx.coroutines.launch

class LsgViewModel : ViewModel() {

    private val repo = LsgRepository()

    // Resultado del login: true=éxito, false=fallo, null=esperando
    private val _loginResult = MutableLiveData<Boolean?>()
    val loginResult: LiveData<Boolean?> get() = _loginResult

    // Resultado del envío del puntaje al sensor
    private val _ingestResult = MutableLiveData<Boolean?>()
    val ingestResult: LiveData<Boolean?> get() = _ingestResult

    // ─── FUNCIÓN 1: Solo hace login y guarda el token ───────────────────────
    fun login(username: String, password: String) {
        viewModelScope.launch {
            val success = repo.login(username, password)
            _loginResult.postValue(success)
        }
    }

    // ─── FUNCIÓN 2: Solo envía el puntaje del sensor a LSG ──────────────────
    fun sendScanResult(score: Int) {
        viewModelScope.launch {
            val success = repo.ingestSensorData(score)
            _ingestResult.postValue(success)
        }
    }
}
package com.bastianguerrero.securitysensor.data.repository

import android.util.Log
import com.bastianguerrero.securitysensor.data.model.SensorIngestRequest
import com.bastianguerrero.securitysensor.data.network.RetrofitInstance
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Repositorio Singleton para mantener la sesión y los datos del usuario
 * en toda la aplicación.
 */
object LsgRepository {

    // Datos dinámicos del usuario obtenidos vía /whoami
    private var playerId: Int = 0
    private var userName: String = ""

    // ⚠️ Reemplaza con tus IDs reales de la plataforma LSG
    private val SENSOR_ENDPOINT_ID         = 3
    private val PLAYERS_SENSOR_ENDPOINT_ID = 8

    // Token guardado en memoria tras el login exitoso
    private var token: String? = null

    fun getPlayerId() = playerId
    fun getUserName() = userName

    // ─── FUNCIÓN 1: Login → guarda el token y obtiene info del usuario ──────
    suspend fun login(username: String, password: String): Boolean {
        return try {
            val response = RetrofitInstance.authApi.login(username, password)

            if (response.isSuccessful) {
                token = response.body()?.access_token
                Log.d("LSG", "Login exitoso. Token guardado ✓")
                
                // Obtener información del usuario inmediatamente
                fetchUserInfo()
            } else {
                Log.e("LSG", "Login fallido: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red en login: ${e.message}")
            false
        }
    }

    private suspend fun fetchUserInfo(): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = RetrofitInstance.authApi.whoami("Bearer $currentToken")
            if (response.isSuccessful) {
                val data = response.body()
                playerId = data?.id_players ?: 0
                userName = data?.name ?: ""
                Log.d("LSG", "Usuario identificado: $userName (ID: $playerId)")
                true
            } else {
                Log.e("LSG", "Error obteniendo whoami: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red en whoami: ${e.message}")
            false
        }
    }

    // ─── FUNCIÓN 2: Envía el puntaje del escaneo al webhook de LSG ──────────
    suspend fun ingestSensorData(score: Int): Boolean {
        if (token == null) {
            Log.e("LSG", "No hay token. El usuario debe loguearse primero.")
            return false
        }

        return try {
            val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

            val request = SensorIngestRequest(
                player_id                  = playerId,
                sensor_endpoint_id         = SENSOR_ENDPOINT_ID,
                players_sensor_endpoint_id = PLAYERS_SENSOR_ENDPOINT_ID,
                raw_payload   = mapOf("security_score" to score, "date" to now),
                parsed_value  = score.toDouble(),
                status        = "OK",
                error_message = null,
                occurred_at   = now
            )

            val response = RetrofitInstance.coreApi.ingestSensorEvent(
                authHeader = "Bearer $token",
                request    = request
            )

            if (response.isSuccessful) {
                Log.d("LSG", "Puntaje $score enviado a LSG ✓")
                true
            } else {
                Log.e("LSG", "Ingest fallido: ${response.code()} ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red en ingest: ${e.message}")
            false
        }
    }
}
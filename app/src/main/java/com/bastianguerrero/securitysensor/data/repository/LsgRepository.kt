package com.bastianguerrero.securitysensor.data.repository

import android.util.Log
import com.bastianguerrero.securitysensor.data.model.SensorIngestRequest
import com.bastianguerrero.securitysensor.data.network.RetrofitInstance
import java.time.Instant
import java.time.format.DateTimeFormatter

class LsgRepository {

    // ojo
    private val PLAYER_ID                  = 0
    private val SENSOR_ENDPOINT_ID         = 0
    private val PLAYERS_SENSOR_ENDPOINT_ID = 0

    // Token guardado en memoria tras el login exitoso
    private var token: String? = null

    // ─── FUNCIÓN 1: Login → guarda el token para usarlo después ─────────────
    suspend fun login(username: String, password: String): Boolean {
        return try {
            val response = RetrofitInstance.authApi.login(username, password)

            if (response.isSuccessful) {
                // Guarda el token en memoria
                token = response.body()?.access_token
                Log.d("LSG", "Login exitoso. Token guardado ✓")
                true
            } else {
                Log.e("LSG", "Login fallido: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red en login: ${e.message}")
            false
        }
    }

    // ─── FUNCIÓN 2: Envía el puntaje del escaneo al webhook de LSG ──────────
    suspend fun ingestSensorData(score: Int): Boolean {
        // Verifica que haya token disponible antes de enviar
        if (token == null) {
            Log.e("LSG", "No hay token. El usuario debe loguearse primero.")
            return false
        }

        return try {
            // Timestamp actual en formato ISO 8601
            val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

            // Construye el cuerpo de la solicitud con el puntaje obtenido
            val request = SensorIngestRequest(
                player_id                  = PLAYER_ID,
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
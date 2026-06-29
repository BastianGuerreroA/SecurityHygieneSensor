package com.bastianguerrero.securitysensor.data.repository

import android.util.Log
import com.bastianguerrero.securitysensor.data.model.PointsAdjustRequest
import com.bastianguerrero.securitysensor.data.model.SensorIngestRequest
import com.bastianguerrero.securitysensor.data.model.TokenRemainingResponse
import com.bastianguerrero.securitysensor.data.network.RetrofitInstance
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repositorio Singleton para mantener la sesión y los datos del usuario
 * en toda la aplicación.
 */
object LsgRepository {

    // Datos dinámicos del usuario obtenidos vía /whoami
    private var playerId: Int = 0
    private var userName: String = ""

    // ID fijo para el tipo de sensor "Higiene y Seguridad"
    private val SENSOR_ENDPOINT_ID = 3
    
    // Este ID ahora es DINÁMICO para cada jugador
    private var playersSensorEndpointId: Int = 0

    // Token guardado en memoria tras el login exitoso
    private var token: String? = null

    fun getPlayerId() = playerId
    fun getUserName() = userName

    suspend fun login(username: String, password: String): Boolean {
        return try {
            val response = RetrofitInstance.authApi.login(username, password)

            if (response.isSuccessful) {
                token = response.body()?.access_token
                Log.d("LSG", "Login exitoso. Token guardado ✓")
                
                // Obtener información del usuario e IDs dinámicos inmediatamente
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
                
                // PASO SOLICITADO: Buscar el ID dinámico de vinculación jugador-sensor
                fetchDynamicSensorId()
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

    /**
     * Recupera la lista de sensores del jugador y busca el ID de vinculación correcto
     * para el sensor de "Higiene y Seguridad" (id_sensor_endpoint == 3).
     */
    private suspend fun fetchDynamicSensorId() {
        val currentToken = token ?: return
        try {
            val response = RetrofitInstance.coreApi.getPlayerSensors("Bearer $currentToken", playerId)
            if (response.isSuccessful) {
                // Buscamos el registro que pertenezca a nuestro SENSOR_ENDPOINT_ID
                val sensorMapping = response.body()?.find { it.id_sensor_endpoint == SENSOR_ENDPOINT_ID }
                
                if (sensorMapping != null) {
                    playersSensorEndpointId = sensorMapping.id_players_sensor_endpoint
                    Log.d("LSG", "Vinculación encontrada. players_sensor_endpoint_id: $playersSensorEndpointId")
                } else {
                    Log.e("LSG", "No se encontró vinculación para el sensor $SENSOR_ENDPOINT_ID")
                }
            } else {
                Log.e("LSG", "Error obteniendo sensores del jugador: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red al obtener sensores: ${e.message}")
        }
    }

    // ─── FUNCIÓN 2: Envía el puntaje del escaneo al webhook de LSG ──────────
    suspend fun ingestSensorData(score: Int, deviceSecurity: Map<String, Any>): Boolean {
        if (!checkAndRefreshToken()) {
            Log.e("LSG", "Token inválido o expirado. No se puede realizar la ingesta.")
            return false
        }
        val currentToken = token ?: return false

        if (playersSensorEndpointId == 0) {
            Log.e("LSG", "No se puede enviar: players_sensor_endpoint_id no recuperado.")
            return false
        }

        return try {
            val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

            val request = SensorIngestRequest(
                player_id                  = playerId,
                sensor_endpoint_id         = SENSOR_ENDPOINT_ID,
                players_sensor_endpoint_id = playersSensorEndpointId, // Dinámico
                raw_payload   = mapOf(
                    "device_security" to deviceSecurity
                ),
                parsed_value  = score.toDouble(),
                status        = "OK",      // Requerido por el profesor
                error_message = null,
                occurred_at   = now        // Requerido por el profesor
            )

            val response = RetrofitInstance.coreApi.ingestSensorEvent(
                authHeader = "Bearer $currentToken",
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

    // ─── FUNCIÓN 3: Envía el puntaje a la dimensión Mental como ajuste ─────
    suspend fun adjustUserPoints(score: Int): Boolean {
        if (!checkAndRefreshToken()) {
            Log.e("LSG", "Token inválido o expirado. No se puede realizar el ajuste de puntos.")
            return false
        }
        val currentToken = token ?: return false

        return try {
            val request = PointsAdjustRequest(
                attribute_id = 4, // MENTAL_BASE
                direction = "CREDIT",
                amount = score,
                reason = "Security Hygiene Scan Score - ${LocalDate.now()}",
                videogame_id = 14
            )

            val response = RetrofitInstance.coreApi.adjustPoints(
                authHeader = "Bearer $currentToken",
                playerId   = playerId,
                request    = request
            )

            if (response.isSuccessful) {
                Log.d("LSG", "Ajuste de puntos exitoso para dimensión Mental ✓")
                true
            } else {
                Log.e("LSG", "Ajuste de puntos fallido: ${response.code()} ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red en adjustPoints: ${e.message}")
            false
        }
    }

    /**
     * Comprueba el tiempo de vida restante del token. Si es inferior a 5 minutos (300 segundos),
     * realiza el refresco automático llamando al endpoint /token/refresh de lsg-auth.
     * Si el token ya expiró o falla el refresco (retorna 401), limpia el token y devuelve false.
     */
    suspend fun checkAndRefreshToken(): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = RetrofitInstance.authApi.getTokenRemaining("Bearer $currentToken")
            if (response.isSuccessful) {
                val remainingSeconds = response.body()?.expires_in_seconds ?: 0
                Log.d("LSG", "Tiempo restante del token: $remainingSeconds segundos")

                if (remainingSeconds < 300) {
                    Log.d("LSG", "Token próximo a expirar (< 5 min). Refrescando...")
                    val refreshResponse = RetrofitInstance.authApi.refreshToken("Bearer $currentToken")
                    if (refreshResponse.isSuccessful) {
                        token = refreshResponse.body()?.access_token
                        Log.d("LSG", "Token refrescado exitosamente ✓")
                        true
                    } else {
                        Log.e("LSG", "Error al refrescar token: ${refreshResponse.code()}")
                        if (refreshResponse.code() == 401) {
                            token = null
                        }
                        false
                    }
                } else {
                    true
                }
            } else {
                Log.e("LSG", "Error al verificar tiempo restante: ${response.code()}")
                if (response.code() == 401) {
                    token = null
                }
                false
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red al verificar/refrescar token: ${e.message}")
            false
        }
    }
}

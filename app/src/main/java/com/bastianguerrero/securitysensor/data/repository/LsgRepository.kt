package com.bastianguerrero.securitysensor.data.repository

import android.util.Log
import com.bastianguerrero.securitysensor.data.model.LinkSensorEndpointRequest
import com.bastianguerrero.securitysensor.data.model.LinkSensorRequest
import com.bastianguerrero.securitysensor.data.model.PointsAdjustRequest
import com.bastianguerrero.securitysensor.data.model.SensorIngestRequest
import com.bastianguerrero.securitysensor.data.model.TokenRemainingResponse
import com.bastianguerrero.securitysensor.data.network.RetrofitInstance
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repositorio central (Singleton) responsable de la sesión del usuario,
 * autenticación ante LSG, vinculación dinámica de sensores y envío de datos/puntos.
 */
object LsgRepository {

    private var playerId: Int = 0
    private var userName: String = ""

    /** Identificador del tipo de sensor de Higiene y Seguridad según el catálogo de endpoints de LSG. */
    private const val SENSOR_ENDPOINT_ID = 3
    
    /** Identificador dinámico de la relación jugador-sensor asignado por la plataforma. */
    private var playersSensorEndpointId: Int = 0

    /** Token JWT de acceso mantenido en memoria para las peticiones autenticadas. */
    private var token: String? = null

    /** Retorna el identificador único del jugador autenticado. */
    fun getPlayerId(): Int = playerId

    /** Retorna el nombre de visualización del jugador autenticado. */
    fun getUserName(): String = userName

    /**
     * Autentica las credenciales del usuario contra el servicio LSG-Auth y recupera
     * la información del perfil y sus sensores asociados.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña de usuario.
     * @return `true` si la autenticación fue exitosa y se obtuvo el token; `false` en caso contrario.
     */
    suspend fun login(username: String, password: String): Boolean {
        return try {
            val response = RetrofitInstance.authApi.login(username, password)

            if (response.isSuccessful) {
                token = response.body()?.access_token
                Log.d("LSG", "Login exitoso. Token guardado.")
                
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

    /**
     * Consulta el endpoint `/whoami` para obtener los datos de identidad del jugador
     * y dispara la consulta del identificador dinámico de sensor.
     *
     * @return `true` si se recuperó el perfil satisfactoriamente; `false` en caso de error.
     */
    private suspend fun fetchUserInfo(): Boolean {
        val currentToken = token ?: return false
        return try {
            val response = RetrofitInstance.authApi.whoami("Bearer $currentToken")
            if (response.isSuccessful) {
                val data = response.body()
                playerId = data?.id_players ?: 0
                userName = data?.name ?: ""
                Log.d("LSG", "Usuario identificado: $userName (ID: $playerId)")

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
     * Recupera la lista de sensores del jugador y busca el ID de vinculación correspondiente
     * al sensor de Higiene y Seguridad ([SENSOR_ENDPOINT_ID]).
     *
     * Si no existe vinculación previa, solicita la auto-vinculación mediante [autoLinkSensorEndpoint].
     */
    private suspend fun fetchDynamicSensorId() {
        val currentToken = token ?: return
        try {
            val response = RetrofitInstance.coreApi.getPlayerSensors("Bearer $currentToken", playerId)
            if (response.isSuccessful) {
                val sensorMapping = response.body()?.find { it.id_sensor_endpoint == SENSOR_ENDPOINT_ID }
                
                if (sensorMapping != null) {
                    playersSensorEndpointId = sensorMapping.id_players_sensor_endpoint
                    Log.d("LSG", "Vinculación encontrada. players_sensor_endpoint_id: $playersSensorEndpointId")
                } else {
                    Log.w("LSG", "No se encontró vinculación para el sensor $SENSOR_ENDPOINT_ID. Intentando auto-vincular...")
                    autoLinkSensorEndpoint()
                }
            } else {
                Log.e("LSG", "Error obteniendo sensores del jugador: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red al obtener sensores: ${e.message}")
        }
    }

    /**
     * Vincula el sensor online base (ID 1) y activa el endpoint específico ([SENSOR_ENDPOINT_ID])
     * para el jugador actual en el servidor LSG.
     */
    private suspend fun autoLinkSensorEndpoint() {
        val currentToken = token ?: return
        try {
            val linkSensorReq = LinkSensorRequest(sensor_id = 1)
            RetrofitInstance.coreApi.linkSensor("Bearer $currentToken", playerId, linkSensorReq)

            val request = LinkSensorEndpointRequest(
                sensor_endpoint_id = SENSOR_ENDPOINT_ID,
                activated = true,
                schedule_time = null
            )
            val response = RetrofitInstance.coreApi.linkSensorEndpoint("Bearer $currentToken", playerId, request)
            if (response.isSuccessful) {
                val newId = response.body()?.id_players_sensor_endpoint ?: 0
                if (newId != 0) {
                    playersSensorEndpointId = newId
                    Log.d("LSG", "Auto-vinculación exitosa. Nuevo players_sensor_endpoint_id: $playersSensorEndpointId")
                } else {
                    Log.w("LSG", "La API respondió 200 pero sin ID de vinculación de sensor.")
                }
            } else {
                Log.e("LSG", "Auto-vinculación rechazada por LSG (${response.code()}). Puede requerir que un admin/researcher vincule el sensor al jugador.")
            }
        } catch (e: Exception) {
            Log.e("LSG", "Error de red al auto-vincular sensor: ${e.message}")
        }
    }

    /**
     * Envía las métricas y la puntuación del escaneo al webhook de ingestión de sensores de LSG.
     *
     * @param score Puntuación calculada del escaneo (0 a 100).
     * @param deviceSecurity Mapa con los detalles técnicos de la configuración del dispositivo.
     * @return `true` si el servidor confirmó la recepción del evento; `false` en caso de error.
     */
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
                players_sensor_endpoint_id = playersSensorEndpointId,
                raw_payload   = mapOf(
                    "device_security" to deviceSecurity
                ),
                parsed_value  = score.toDouble(),
                status        = "OK",
                error_message = null,
                occurred_at   = now
            )

            val response = RetrofitInstance.coreApi.ingestSensorEvent(
                authHeader = "Bearer $currentToken",
                request    = request
            )

            if (response.isSuccessful) {
                Log.d("LSG", "Puntaje $score enviado a LSG.")
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

    /**
     * Acredita los puntos obtenidos en el escaneo al atributo Mental del jugador en LSG.
     *
     * @param score Cantidad de puntos a acreditar.
     * @return `true` si la acreditación fue procesada exitosamente por LSG; `false` en caso contrario.
     */
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
                Log.d("LSG", "Ajuste de puntos exitoso para dimensión Mental.")
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
     * Comprueba el tiempo de vida restante del token de autenticación.
     *
     * Si resta menos de 5 minutos (300 segundos), invoca `/token/refresh`.
     * Si el token está vencido o la renovación responde 401, limpia la sesión local.
     *
     * @return `true` si el token es válido o fue renovado con éxito; `false` si expiró o no se pudo renovar.
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
                        Log.d("LSG", "Token refrescado exitosamente.")
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

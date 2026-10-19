package com.bastianguerrero.securitysensor.data.network

import com.bastianguerrero.securitysensor.data.model.LinkSensorEndpointRequest
import com.bastianguerrero.securitysensor.data.model.LinkSensorEndpointResponse
import com.bastianguerrero.securitysensor.data.model.LinkSensorRequest
import com.bastianguerrero.securitysensor.data.model.PlayerSensorResponse
import com.bastianguerrero.securitysensor.data.model.PointsAdjustRequest
import com.bastianguerrero.securitysensor.data.model.SensorIngestRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Definición de endpoints de Retrofit para la API principal de LSG (LSG-Core-API).
 *
 * Provee interacción con los servicios de sensores de jugadores, auto-vinculación,
 * ingestión de datos a través de webhooks y ajuste de atributos de gamificación.
 */
interface LsgApiService {

    /**
     * Obtiene el listado de sensores y endpoints asociados al jugador especificado.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @param playerId Identificador del jugador.
     * @return [Response] con la lista de [PlayerSensorResponse].
     */
    @GET("sensors/players/{player_id}")
    suspend fun getPlayerSensors(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int
    ): Response<List<PlayerSensorResponse>>

    /**
     * Vincula un sensor online base a la cuenta de un jugador.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @param playerId Identificador del jugador.
     * @param request Carga útil [LinkSensorRequest] con el sensor_id a vincular.
     * @return [Response] genérica del backend.
     */
    @POST("sensors/players/{player_id}/link")
    suspend fun linkSensor(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int,
        @Body request: LinkSensorRequest
    ): Response<Map<String, Any>>

    /**
     * Activa y vincula un endpoint específico de sensor para un jugador.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @param playerId Identificador del jugador.
     * @param request Carga útil [LinkSensorEndpointRequest] con el sensor_endpoint_id.
     * @return [Response] con [LinkSensorEndpointResponse] conteniendo el ID de relación asignado.
     */
    @POST("sensors/players/{player_id}/link-endpoint")
    suspend fun linkSensorEndpoint(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int,
        @Body request: LinkSensorEndpointRequest
    ): Response<LinkSensorEndpointResponse>

    /**
     * Envía eventos y métricas de escaneo al webhook de ingesta de sensores.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @param request Carga útil [SensorIngestRequest] con los datos analizados y raw payload.
     * @return [Response] con el resultado de la ingesta en el servidor.
     */
    @POST("sensors/ingest/webhook")
    suspend fun ingestSensorEvent(
        @Header("Authorization") authHeader: String,
        @Body request: SensorIngestRequest
    ): Response<Map<String, Any>>

    /**
     * Ajusta el balance de puntos de un atributo específico para el jugador indicado.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @param playerId Identificador del jugador.
     * @param request Carga útil [PointsAdjustRequest] con el monto, dirección y razón.
     * @return [Response] con el resultado de la operación.
     */
    @POST("players/{player_id}/points/adjust")
    suspend fun adjustPoints(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int,
        @Body request: PointsAdjustRequest
    ): Response<Map<String, Any>>
}
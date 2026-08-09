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

interface LsgApiService {

    @GET("sensors/players/{player_id}")
    suspend fun getPlayerSensors(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int
    ): Response<List<PlayerSensorResponse>>

    @POST("sensors/players/{player_id}/link")
    suspend fun linkSensor(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int,
        @Body request: LinkSensorRequest
    ): Response<Map<String, Any>>

    @POST("sensors/players/{player_id}/link-endpoint")
    suspend fun linkSensorEndpoint(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int,
        @Body request: LinkSensorEndpointRequest
    ): Response<LinkSensorEndpointResponse>

    @POST("sensors/ingest/webhook")
    suspend fun ingestSensorEvent(
        @Header("Authorization") authHeader: String,
        @Body request: SensorIngestRequest
    ): Response<Map<String, Any>>

    @POST("players/{player_id}/points/adjust")
    suspend fun adjustPoints(
        @Header("Authorization") authHeader: String,
        @Path("player_id") playerId: Int,
        @Body request: PointsAdjustRequest
    ): Response<Map<String, Any>>
}
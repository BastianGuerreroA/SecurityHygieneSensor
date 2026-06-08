package com.bastianguerrero.securitysensor.data.network

import com.bastianguerrero.securitysensor.data.model.PlayerSensorResponse
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

    @POST("sensors/ingest/webhook")
    suspend fun ingestSensorEvent(
        @Header("Authorization") authHeader: String,
        @Body request: SensorIngestRequest
    ): Response<Map<String, Any>>
}
package com.bastianguerrero.securitysensor.data.network

import com.bastianguerrero.securitysensor.data.model.SensorIngestRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LsgApiService {

    @POST("sensors/ingest/webhook")
    suspend fun ingestSensorEvent(
        @Header("Authorization") authHeader: String,
        @Body request: SensorIngestRequest
    ): Response<Map<String, Any>>
}
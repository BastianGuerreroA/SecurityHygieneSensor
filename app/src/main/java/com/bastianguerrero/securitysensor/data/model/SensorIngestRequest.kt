package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SensorIngestRequest(
    @SerializedName("player_id") val player_id: Int,
    @SerializedName("sensor_endpoint_id") val sensor_endpoint_id: Int,
    @SerializedName("players_sensor_endpoint_id") val players_sensor_endpoint_id: Int,
    @SerializedName("raw_payload") val raw_payload: Map<String, Any>,
    @SerializedName("parsed_value") val parsed_value: Double,
    @SerializedName("status") val status: String,
    @SerializedName("error_message") val error_message: String?,
    @SerializedName("occurred_at") val occurred_at: String
)
package com.bastianguerrero.securitysensor.data.model

data class SensorIngestRequest(
    val player_id: Int,
    val sensor_endpoint_id: Int,
    val players_sensor_endpoint_id: Int,
    val raw_payload: Map<String, Any>,
    val parsed_value: Double,
    val status: String,
    val error_message: String?,
    val occurred_at: String
)
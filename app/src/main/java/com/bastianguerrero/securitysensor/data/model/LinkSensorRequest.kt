package com.bastianguerrero.securitysensor.data.model

data class LinkSensorRequest(
    val sensor_id: Int = 1,
    val tokens: Map<String, String> = emptyMap()
)

package com.bastianguerrero.securitysensor.data.model

data class LinkSensorEndpointRequest(
    val sensor_endpoint_id: Int = 3,
    val activated: Boolean = true,
    val schedule_time: Int? = null
)

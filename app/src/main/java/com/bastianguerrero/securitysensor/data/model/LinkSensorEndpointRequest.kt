package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LinkSensorEndpointRequest(
    @SerializedName("sensor_endpoint_id") val sensor_endpoint_id: Int = 3,
    @SerializedName("activated") val activated: Boolean = true,
    @SerializedName("schedule_time") val schedule_time: Int? = null
)

package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LinkSensorRequest(
    @SerializedName("sensor_id") val sensor_id: Int = 1,
    @SerializedName("tokens") val tokens: Map<String, String> = emptyMap()
)

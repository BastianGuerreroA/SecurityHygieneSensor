package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LinkSensorEndpointResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("id_players_sensor_endpoint") val id_players_sensor_endpoint: Int? = null,
    @SerializedName("player_id") val player_id: Int? = null,
    @SerializedName("sensor_endpoint_id") val sensor_endpoint_id: Int? = null,
    @SerializedName("activated") val activated: Boolean? = null
)

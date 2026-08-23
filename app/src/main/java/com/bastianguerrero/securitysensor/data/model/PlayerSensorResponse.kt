package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PlayerSensorResponse(
    @SerializedName("id_players_online_sensor") val id_players_online_sensor: Int,
    @SerializedName("id_players") val id_players: Int,
    @SerializedName("id_online_sensor") val id_online_sensor: Int,
    @SerializedName("id_sensor_endpoint") val id_sensor_endpoint: Int,
    @SerializedName("id_players_sensor_endpoint") val id_players_sensor_endpoint: Int,
    @SerializedName("sensor_name") val sensor_name: String,
    @SerializedName("endpoint_name") val endpoint_name: String
)
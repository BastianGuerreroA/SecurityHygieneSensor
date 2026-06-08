package com.bastianguerrero.securitysensor.data.model

data class PlayerSensorResponse(
    val id_players_online_sensor: Int,
    val id_players: Int,
    val id_online_sensor: Int,
    val id_sensor_endpoint: Int,
    val id_players_sensor_endpoint: Int,
    val sensor_name: String,
    val endpoint_name: String
)
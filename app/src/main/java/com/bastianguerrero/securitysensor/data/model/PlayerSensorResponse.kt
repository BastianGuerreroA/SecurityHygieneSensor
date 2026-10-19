package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Representa la vinculación activa de un sensor y su endpoint para un jugador en LSG.
 *
 * @property id_players_online_sensor Identificador de la vinculación del sensor base con el jugador.
 * @property id_players Identificador del jugador.
 * @property id_online_sensor Identificador del sensor base en el sistema.
 * @property id_sensor_endpoint Identificador del tipo de endpoint (ej. 3 para higiene y seguridad).
 * @property id_players_sensor_endpoint Identificador único de vinculación utilizado para la ingestión.
 * @property sensor_name Nombre descriptivo del sensor.
 * @property endpoint_name Nombre del endpoint asociado.
 */
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
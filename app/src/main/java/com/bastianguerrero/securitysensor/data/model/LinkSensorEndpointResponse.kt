package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Respuesta del servidor tras solicitar la vinculación o activación de un endpoint de sensor.
 *
 * @property status Estado devuelto por el servidor (ej. "success").
 * @property id_players_sensor_endpoint Identificador generado de la relación entre jugador y endpoint del sensor.
 * @property player_id Identificador del jugador.
 * @property sensor_endpoint_id Identificador del endpoint del sensor.
 * @property activated Estado de activación resultante.
 */
@Keep
data class LinkSensorEndpointResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("id_players_sensor_endpoint") val id_players_sensor_endpoint: Int? = null,
    @SerializedName("player_id") val player_id: Int? = null,
    @SerializedName("sensor_endpoint_id") val sensor_endpoint_id: Int? = null,
    @SerializedName("activated") val activated: Boolean? = null
)

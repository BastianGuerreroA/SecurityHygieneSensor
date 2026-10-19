package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Carga útil para activar y vincular un endpoint específico de un sensor a la cuenta del jugador.
 *
 * @property sensor_endpoint_id Identificador del endpoint del sensor (por defecto 3 para Higiene y Seguridad).
 * @property activated Estado de activación deseado (por defecto true).
 * @property schedule_time Horario programado opcional para la ejecución del sensor.
 */
@Keep
data class LinkSensorEndpointRequest(
    @SerializedName("sensor_endpoint_id") val sensor_endpoint_id: Int = 3,
    @SerializedName("activated") val activated: Boolean = true,
    @SerializedName("schedule_time") val schedule_time: Int? = null
)

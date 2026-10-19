package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Carga útil para vincular un sensor online base a la cuenta del jugador en LSG.
 *
 * @property sensor_id Identificador del sensor online base (por defecto 1).
 * @property tokens Mapa opcional de tokens de autenticación externos requeridos por el sensor.
 */
@Keep
data class LinkSensorRequest(
    @SerializedName("sensor_id") val sensor_id: Int = 1,
    @SerializedName("tokens") val tokens: Map<String, String> = emptyMap()
)

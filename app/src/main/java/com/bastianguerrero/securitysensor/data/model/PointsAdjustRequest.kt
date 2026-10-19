package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Carga útil para ajustar el balance de puntos de un atributo de un jugador en LSG.
 *
 * @property attribute_id Identificador del atributo a modificar (ej. 4 para MENTAL_BASE).
 * @property direction Dirección del ajuste ("CREDIT" para sumar, "DEBIT" para restar).
 * @property amount Cantidad numérica de puntos a ajustar.
 * @property reason Motivo o justificación descriptiva del ajuste.
 * @property videogame_id Identificador del videojuego o experiencia asociada.
 */
@Keep
data class PointsAdjustRequest(
    @SerializedName("attribute_id") val attribute_id: Int?,
    @SerializedName("direction") val direction: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("reason") val reason: String?,
    @SerializedName("videogame_id") val videogame_id: Int?
)

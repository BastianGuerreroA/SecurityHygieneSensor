package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Respuesta recibida al consultar la vigencia del token JWT activo.
 *
 * @property expires_in_seconds Segundos restantes antes de que el token expire.
 * @property expires_at Fecha y hora de expiración en formato legible/ISO.
 * @property issued_at Fecha y hora de emisión del token.
 */
@Keep
data class TokenRemainingResponse(
    @SerializedName("expires_in_seconds") val expires_in_seconds: Int,
    @SerializedName("expires_at") val expires_at: String,
    @SerializedName("issued_at") val issued_at: String
)

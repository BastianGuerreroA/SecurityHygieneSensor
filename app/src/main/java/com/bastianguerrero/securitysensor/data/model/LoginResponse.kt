package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Respuesta devuelta por el servicio de autenticación tras un inicio de sesión exitoso.
 *
 * @property access_token Token de acceso JWT utilizado para autorizar peticiones subsecuentes.
 * @property token_type Tipo de token emitido (habitualmente "bearer").
 */
@Keep
data class LoginResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("token_type") val token_type: String
)
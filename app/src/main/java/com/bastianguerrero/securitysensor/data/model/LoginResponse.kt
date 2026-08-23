package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LoginResponse(
    @SerializedName("access_token") val access_token: String,
    @SerializedName("token_type") val token_type: String
)
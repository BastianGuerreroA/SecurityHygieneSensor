package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TokenRemainingResponse(
    @SerializedName("expires_in_seconds") val expires_in_seconds: Int,
    @SerializedName("expires_at") val expires_at: String,
    @SerializedName("issued_at") val issued_at: String
)

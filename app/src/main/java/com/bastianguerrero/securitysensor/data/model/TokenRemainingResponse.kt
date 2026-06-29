package com.bastianguerrero.securitysensor.data.model

data class TokenRemainingResponse(
    val expires_in_seconds: Int,
    val expires_at: String,
    val issued_at: String
)

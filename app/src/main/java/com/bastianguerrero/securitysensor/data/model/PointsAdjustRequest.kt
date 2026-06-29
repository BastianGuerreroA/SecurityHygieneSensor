package com.bastianguerrero.securitysensor.data.model

data class PointsAdjustRequest(
    val attribute_id: Int?,
    val direction: String, // "CREDIT" o "DEBIT"
    val amount: Int,
    val reason: String?,
    val videogame_id: Int?
)

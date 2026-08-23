package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class PointsAdjustRequest(
    @SerializedName("attribute_id") val attribute_id: Int?,
    @SerializedName("direction") val direction: String, // "CREDIT" o "DEBIT"
    @SerializedName("amount") val amount: Int,
    @SerializedName("reason") val reason: String?,
    @SerializedName("videogame_id") val videogame_id: Int?
)

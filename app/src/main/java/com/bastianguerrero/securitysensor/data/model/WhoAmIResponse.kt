package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WhoAmIResponse(
    @SerializedName("id_players") val id_players: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("age") val age: Int,
    @SerializedName("roles") val roles: List<String>
)
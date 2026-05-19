package com.bastianguerrero.securitysensor.data.model

data class WhoAmIResponse(
    val id_players: Int,
    val name: String,
    val email: String,
    val age: Int,
    val roles: List<String>
)
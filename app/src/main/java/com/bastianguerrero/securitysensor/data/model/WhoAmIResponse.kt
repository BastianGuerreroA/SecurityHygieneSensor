package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Información de identidad y perfil del jugador obtenida desde el servicio de autenticación.
 *
 * @property id_players Identificador numérico del jugador en el ecosistema LSG.
 * @property name Nombre completo o apodo del jugador.
 * @property email Correo electrónico registrado del jugador.
 * @property age Edad registrada del jugador.
 * @property roles Lista de roles o permisos asociados al usuario (ej. "player", "admin").
 */
@Keep
data class WhoAmIResponse(
    @SerializedName("id_players") val id_players: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("age") val age: Int,
    @SerializedName("roles") val roles: List<String>
)
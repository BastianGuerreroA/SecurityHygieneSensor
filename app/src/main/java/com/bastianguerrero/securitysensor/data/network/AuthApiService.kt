package com.bastianguerrero.securitysensor.data.network

import com.bastianguerrero.securitysensor.data.model.LoginResponse
import com.bastianguerrero.securitysensor.data.model.TokenRemainingResponse
import com.bastianguerrero.securitysensor.data.model.WhoAmIResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Definición de endpoints de Retrofit para el servicio de autenticación LSG (LSG-Auth).
 */
interface AuthApiService {

    /**
     * Autentica a un usuario mediante credenciales OAuth2 con grant type 'password'.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña de acceso.
     * @param grantType Tipo de concesión OAuth2 (por defecto "password").
     * @return [Response] con [LoginResponse] que contiene el token JWT de acceso.
     */
    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Response<LoginResponse>

    /**
     * Obtiene la identidad y el perfil del usuario asociado al token activo.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @return [Response] con [WhoAmIResponse] conteniendo id_players, nombre, email y roles.
     */
    @GET("whoami")
    suspend fun whoami(
        @Header("Authorization") authHeader: String
    ): Response<WhoAmIResponse>

    /**
     * Consulta el tiempo restante de vigencia del token JWT.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @return [Response] con [TokenRemainingResponse] indicando los segundos restantes.
     */
    @GET("token/remaining")
    suspend fun getTokenRemaining(
        @Header("Authorization") authHeader: String
    ): Response<TokenRemainingResponse>

    /**
     * Renueva el token JWT activo utilizando el endpoint de refresco.
     *
     * @param authHeader Encabezado Authorization con formato "Bearer <token>".
     * @return [Response] con un nuevo [LoginResponse] conteniendo el token renovado.
     */
    @POST("token/refresh")
    suspend fun refreshToken(
        @Header("Authorization") authHeader: String
    ): Response<LoginResponse>
}
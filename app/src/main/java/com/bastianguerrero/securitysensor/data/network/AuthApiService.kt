package com.bastianguerrero.securitysensor.data.network

import com.bastianguerrero.securitysensor.data.model.LoginResponse
import com.bastianguerrero.securitysensor.data.model.WhoAmIResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password"
    ): Response<LoginResponse>

    @GET("whoami")
    suspend fun whoami(
        @Header("Authorization") authHeader: String
    ): Response<WhoAmIResponse>
}
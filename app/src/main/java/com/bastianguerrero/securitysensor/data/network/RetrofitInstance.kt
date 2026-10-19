package com.bastianguerrero.securitysensor.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Proveedor singleton de instancias de Retrofit configuradas para los servicios de LSG.
 *
 * Centraliza la inicialización de [authApi] (para autenticación y tokens)
 * y [coreApi] (para interacción con la API de juegos, sensores y puntajes).
 */
object RetrofitInstance {

    private const val BASE_URL_AUTH = "https://lsg.diinf.usach.cl/lsg-auth/"
    private const val BASE_URL_CORE = "https://lsg.diinf.usach.cl/lsg-core-api/"

    /** Cliente de la API de autenticación y tokens de sesión. */
    val authApi: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_AUTH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    /** Cliente de la API central de LSG para la gestión de sensores y gamificación. */
    val coreApi: LsgApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CORE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LsgApiService::class.java)
    }
}
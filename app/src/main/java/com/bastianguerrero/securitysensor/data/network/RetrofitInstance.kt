package com.bastianguerrero.securitysensor.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL_AUTH = "https://lsg.diinf.usach.cl/lsg-auth/"
    private const val BASE_URL_CORE = "https://lsg.diinf.usach.cl/lsg-core-api/"

    val authApi: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_AUTH)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    val coreApi: LsgApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_CORE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LsgApiService::class.java)
    }
}
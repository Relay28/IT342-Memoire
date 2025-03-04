package com.example.memoire.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.10:8080/"

    private val gson = GsonBuilder()
        .setLenient() // ✅ Allows lenient parsing for JSON responses
        .create()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson)) // 🔹 Use lenient Gson
            .build()
            .create(ApiService::class.java)
    }
}

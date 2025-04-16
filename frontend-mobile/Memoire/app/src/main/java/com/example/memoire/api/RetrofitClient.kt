package com.example.memoire.api

import android.content.Context
import com.example.memoire.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient

object RetrofitClient {
    public const val BASE_URL = "http://192.168.1.8:8080/"

    private lateinit var sessionManager: SessionManager

    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }
    fun getBaseUrl(): String {
        return BASE_URL
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = sessionManager.getUserSession()["token"]
                val request = if (token!=null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .build()
    }
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient() // Enable lenient JSON parsing
            .create()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
    val capsuleContentInstance: CapsuleContentService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CapsuleContentService::class.java)
    }
}

package com.example.memoire.api

import android.content.Context
import com.example.memoire.models.LocalDateTimeDeserializer
import com.example.memoire.utils.SessionManager
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.w3c.dom.Comment
import java.time.LocalDateTime

object RetrofitClient {
    const val BASE_URL = "https://memoire-it342.as.r.appspot.com/"

    private lateinit var sessionManager: SessionManager
    fun getAuthToken(): String {
        return (sessionManager.getUserSession()["token"] ?: throw IllegalStateException("Auth token not set")).toString()
    }

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
            .registerTypeAdapter(LocalDateTime::class.java, LocalDateTimeDeserializer())
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
    val commentInstance: ApiCommentService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiCommentService::class.java)
    }
    val friendInstance: FriendshipApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FriendshipApiService::class.java)
    }
}

package com.example.memoire.api

import com.example.memoire.models.AuthenticationRequest
import com.example.memoire.models.AuthenticationResponse
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.RegisterRequest
import com.example.memoire.models.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.*

interface ApiService {
    @GET("/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long
    ): Response<User>

    @GET("api/users/searchByName")
    suspend fun searchUsersByName(@Query("name") name: String): Response<List<User>>

    @GET("api/profiles/view/{userId}")
    fun getPublicProfile(@Path("userId") userId: Long): Call<ProfileDTO>

    @GET("api/profiles/me")
    fun getOwnProfile(@Header("Authorization") authToken: String): Call<ProfileDTO>

    @PUT("api/users/updateUser")
    suspend fun updateUser(
        @Body profile: ProfileDTO
    ): Response<ProfileDTO>


    @PATCH("api/users/{id}/disable")
    suspend fun disableUser(@Path("id") id: Long): Response<String>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthenticationResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: AuthenticationRequest): Response<AuthenticationResponse>

    @POST("api/auth/verify-token")
    @FormUrlEncoded
    fun verifyGoogleToken(@Field("idToken") idToken: String): Call<Map<String, String>>
}

data class AuthenticationRequest(val username: String, val password: String)
data class GoogleAuthRequest(val idToken: String)
data class AuthResponse(val accessToken: String, val userId: String)

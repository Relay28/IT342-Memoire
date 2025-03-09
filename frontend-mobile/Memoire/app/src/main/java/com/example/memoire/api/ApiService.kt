package com.example.memoire.api

import com.example.memoire.models.AuthenticationRequest
import com.example.memoire.models.AuthenticationResponse
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

interface ApiService {
    @GET("/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>

    // 🔹 Create User
    @POST("/users/createUser")
    suspend fun createUser(@Body user: User): Response<User>

    // 🔹 Update User
    @PUT("/users/updateUser/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body user: User): Response<User>

    // 🔹 Disable User
    @PATCH("api/users/{id}/disable")
    suspend fun disableUser(@Path("id") id: Long): Response<String>

    // 🔹 Register User
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthenticationResponse>

    // 🔹 Login User
    @POST("api/auth/login")
    suspend fun login(@Body request: AuthenticationRequest): Response<AuthenticationResponse>

    @POST("api/auth/verify-token")
    @FormUrlEncoded  // ✅ Ensures it's sent as application/x-www-form-urlencoded
    fun verifyGoogleToken(@Field("idToken") idToken: String): Call<Map<String, String>>
}
data class AuthenticationRequest(val username: String, val password: String)
data class GoogleAuthRequest(val idToken: String)
data class AuthResponse(val accessToken: String, val userId: String)

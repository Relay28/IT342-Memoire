package com.example.memoire.api

import com.example.memoire.models.AuthenticationRequest
import com.example.memoire.models.AuthenticationResponse
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.RegisterRequest
import com.example.memoire.models.User
import com.example.memoire.models.UserEntity
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Long
    ): Response<User>

    // Current user profile endpoint
    @GET("api/users")
    fun getCurrentUser(@Header("Authorization") authToken: String): Call<UserEntity>

    // Get profile image endpoint
    @GET("api/users/profile-picture")
    @Streaming
    fun getProfilePicture(@Header("Authorization") authToken: String): Call<ResponseBody>

    // Profile endpoint for accessing other users' profiles
    @GET("api/profiles/view/{userId}")
    fun getPublicProfile(@Path("userId") userId: Long): Call<ProfileDTO>

    // Own profile endpoint
    @GET("api/profiles/me")
    fun getOwnProfile(@Header("Authorization") authToken: String): Call<ProfileDTO>

    // Update user details
    @PUT("api/users")
    suspend fun updateUser(
        @Header("Authorization") authToken: String,
        @Body profile: ProfileDTO
    ): Response<UserEntity>

    // Upload profile picture endpoint
    @Multipart
    @PUT("api/users/profile-picture")
    suspend fun uploadProfileImage(
        @Header("Authorization") authToken: String,
        @Part profileImg: MultipartBody.Part
    ): Response<Map<String, Object>>

    // Disable user account
    @PATCH("api/users/disable")
    suspend fun disableAccount(@Header("Authorization") authToken: String): Response<String>

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

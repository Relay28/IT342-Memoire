package com.example.memoire.api

import com.example.memoire.models.AuthenticationRequest
import com.example.memoire.models.AuthenticationResponse
import com.example.memoire.models.LockRequest
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.RegisterRequest
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.models.UserEntity
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Current user profile endpoint
    @GET("api/users")
    fun getCurrentUser(@Header("Authorization") authToken: String): Call<UserEntity>

    // Get profile image endpoint
    @GET("api/users/profile-picture")
    @Streaming
    fun getProfilePicture(): Call<ResponseBody>

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



    // TimeCapsules
    @GET("api/timecapsules/user")
    fun getUserTimeCapsules(): Call<List<TimeCapsuleDTO>>

    @GET("api/timecapsules/{id}")
    fun getTimeCapsule(@Path("id") id: Long): Call<TimeCapsuleDTO>

    @GET("api/timecapsules/status/published")
    fun getPublishedTimeCapsules(): Call<List<TimeCapsuleDTO>>

    @GET("api/timecapsules/status/closed")
    fun getClosedTimeCapsules(): Call<List<TimeCapsuleDTO>>

    @GET("api/timecapsules/status/unpublished")
    fun getUnpublishedTimeCapsules(): Call<List<TimeCapsuleDTO>>

    @GET("api/timecapsules/status/archived")
    fun getArchivedTimeCapsules(): Call<List<TimeCapsuleDTO>>

    @POST("api/timecapsules")
    fun createTimeCapsule(@Body timeCapsuleDTO: TimeCapsuleDTO): Call<TimeCapsuleDTO>

    @PUT("api/timecapsules/{id}")
    fun updateTimeCapsule(
        @Path("id") id: Long,
        @Body timeCapsuleDTO: TimeCapsuleDTO
    ): Call<TimeCapsuleDTO>

    @DELETE("api/timecapsules/{id}")
    fun deleteTimeCapsule(@Path("id") id: Long): Call<Void>

    @PATCH("api/timecapsules/{id}/lock")
    fun lockTimeCapsule(
        @Path("id") id: Long,
        @Body request: LockRequest
    ): Call<Void>

    @PATCH("api/timecapsules/{id}/unlock")
    fun unlockTimeCapsule(@Path("id") id: Long): Call<Void>
}
data class AuthenticationRequest(val username: String, val password: String)
data class GoogleAuthRequest(val idToken: String)
data class AuthResponse(val accessToken: String, val userId: String)

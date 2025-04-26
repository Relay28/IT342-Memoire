package com.example.memoire.api

import com.example.memoire.models.AuthenticationRequest
import com.example.memoire.models.AuthenticationResponse
import com.example.memoire.models.CapsuleAccessDTO
import com.example.memoire.models.CountdownDTO
import com.example.memoire.models.GrantAccessRequest
import com.example.memoire.models.LockRequest
import com.example.memoire.models.NotificationDTO
import com.example.memoire.models.NotificationEntity
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.RegisterRequest
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.models.UpdateRoleRequest
import com.example.memoire.models.UserEntity
import com.example.memoire.models.UserSearchDTO
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


    @GET("api/timecapsules/status/closed")
    fun getClosedTimeCapsules(): Call<List<TimeCapsuleDTO>>
    @GET("/api/capsules/{id}/countdown")
    fun getCountdown(@Path("id") id: Long): Call<CountdownDTO>

    //Notif Endpoints
    // Add these to your existing ApiService interface
    @GET("api/notifications")
    fun getNotifications(
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Call<List<NotificationEntity>>

    @PATCH("api/notifications/{id}/read")
    fun markNotificationAsRead(
        @Path("id") id: Long
    ): Call<Void>

    @PATCH("api/notifications/read-all")
    fun markAllNotificationsAsRead(
    ): Call<Void>

    @GET("api/notifications/unread-count")
    fun getUnreadNotificationCount(): Call<Map<String, Long>>

    @POST("api/fcm/update-token")
    suspend fun updateFcmToken(
        @Query("userId") userId: Long,
        @Query("fcmToken") fcmToken: String
    ): Response<Void>

    @POST("api/notifications/register-token")
    suspend fun registerFcmToken(
        @Body request: Map<String, String>
    ): Response<Void>




    // Add this data class

    // Add these to your ApiService interface
    @POST("api/capsule-access")
    fun grantAccess(@Body request: GrantAccessRequest): Call<CapsuleAccessDTO>

    @PUT("api/capsule-access/{accessId}/role")
    fun updateAccessRole(
        @Path("accessId") accessId: Long,
        @Body request: UpdateRoleRequest
    ): Call<CapsuleAccessDTO>

    @DELETE("api/capsule-access/{accessId}")
    fun removeAccess(@Path("accessId") accessId: Long): Call<Void>

    @GET("api/capsule-access/capsule/{capsuleId}")
    fun getCapsuleAccesses(@Path("capsuleId") capsuleId: Long): Call<List<CapsuleAccessDTO>>

    @GET("api/capsule-access/user/{userId}")
    fun getUserAccesses(@Path("userId") userId: Long): Call<List<CapsuleAccessDTO>>

    @GET("api/capsule-access/check")
    fun checkAccess(
        @Query("capsuleId") capsuleId: Long,
        @Query("role") role: String
    ): Call<Boolean>

    @GET("api/profiles/search")
    fun searchProfiles(
        @Query("query") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Call<Map<String, Any>>
    //CapsuleAccess


        @GET("api/profiles/search")
        fun searchUsersForGrantAccess(
            @Query("query") query: String,
            @Query("page") page: Int = 0,
            @Query("size") size: Int = 10
        ): Call<Map<String, Any>>

    @POST("api/auth/logout")
    fun logout(@Header("Authorization") authToken: String): Call<ResponseBody>

}
data class AuthenticationRequest(val username: String, val password: String)
data class GoogleAuthRequest(val idToken: String)
data class AuthResponse(val accessToken: String, val userId: String)

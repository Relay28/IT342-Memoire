package com.example.memoire.api


import com.example.memoire.models.CapsuleContentEntity
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface CapsuleContentService {
    // Upload content to a time capsule
    @Multipart
    @POST("api/capsule-content/{capsuleId}/upload")
    suspend fun uploadContent(
        @Path("capsuleId") capsuleId: Long,
        @Part file: MultipartBody.Part
    ): Response<CapsuleContentEntity>

    // Download content by ID
    @GET("api/capsule-content/{id}/download")
    @Streaming
    suspend fun downloadContent(
        @Path("id") id: Long
    ): Response<ResponseBody>

    // Delete content by ID
    @DELETE("api/capsule-content/{id}")
    suspend fun deleteContent(
        @Path("id") id: Long
    ): Response<Void>

    // Get all contents for a specific capsule
    @GET("api/capsule-content/{capsuleId}")
    suspend fun getContentsByCapsule(
        @Path("capsuleId") capsuleId: Long
    ): Response<List<CapsuleContentEntity>>

    // Get content metadata by ID
    @GET("api/capsule-content/{id}/metadata")
    suspend fun getContentMetadata(
        @Path("id") id: Long
    ): Response<CapsuleContentEntity>


}

// Data classes for request/response models


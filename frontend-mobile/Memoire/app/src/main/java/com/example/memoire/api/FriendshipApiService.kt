package com.example.memoire.api

import com.example.memoire.models.UserEntity
import retrofit2.Response
import retrofit2.http.*

data class FriendshipEntity(
    val id: Long,
    val user: UserEntity,
    val friend: UserEntity,
    val friendId: Long,
    val userId: Long,
    val status: String,
    val createdAt: String
)

data class FriendshipRequest(
    val friendId: Long
)

interface FriendshipApiService {


    @GET("/api/friendships/friends/count/{userId}")
    suspend fun getUserFriendsCount(@Path("userId") userId: Long): Response<Long>

    @PUT("/api/friendships/{id}/accept")
    suspend fun acceptFriendship(@Path("id") id: Long): Response<FriendshipEntity>
    @GET("/api/friendships/friends")
    suspend fun getFriendsList(): Response<List<UserEntity>>

    @POST("/api/friendships/create")
    suspend fun createFriendship(@Body request: FriendshipRequest): Response<FriendshipEntity>

    @GET("/api/friendships/areFriends/{friendId}")
    suspend fun areFriends(@Path("friendId") friendId: Long): Response<Boolean>

    @GET("/api/friendships/{id}")
    suspend fun getFriendshipById(@Path("id") id: Long): Response<FriendshipEntity>



    @DELETE("/api/friendships/{id}")
    suspend fun deleteFriendship(@Path("id") id: Long): Response<Void>

    @GET("/api/friendships/hasPendingRequest/{friendId}")
    suspend fun hasPendingRequest(@Path("friendId") friendId: Long): Response<Boolean>

    @DELETE("/api/friendships/cancel/{friendId}")
    suspend fun cancelRequest(@Path("friendId") friendId: Long): Response<Void>

    @GET("/api/friendships/isReceiver/{friendId}")
    suspend fun isReceiver(@Path("friendId") friendId: Long): Response<Boolean>

    @GET("/api/friendships/findByUsers/{friendId}")
    suspend fun findByUsers(@Path("friendId") friendId: Long): Response<FriendshipEntity>

    // Custom endpoint to get friend requests received
    @GET("/api/friendships/requests/received")
    suspend fun getReceivedFriendRequests(): Response<List<FriendshipEntity>>
}
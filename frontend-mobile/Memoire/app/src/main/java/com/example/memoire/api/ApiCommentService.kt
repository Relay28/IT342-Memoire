package com.example.memoire.api

import CommentEntity
import CommentReactionEntity
import CommentRequest
import ReactionRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiCommentService {
    @POST("api/comments/capsule/{capsuleId}")
    fun createComment(
        @Path("capsuleId") capsuleId: Long,
        @Body commentRequest: CommentRequest,
    ): Call<CommentEntity>

    @PUT("api/comments/{commentId}")
    fun updateComment(
        @Path("commentId") commentId: Long,
        @Body commentRequest: CommentRequest,
    ): Call<CommentEntity>

    @DELETE("api/comments/{commentId}")
    fun deleteComment(
        @Path("commentId") commentId: Long,
    ): Call<Void>

    @GET("api/comments/{commentId}")
    fun getCommentById(
        @Path("commentId") commentId: Long,
    ): Call<CommentEntity>

    @GET("api/comments/capsule/{capsuleId}")
    fun getCommentsByCapsule(
        @Path("capsuleId") capsuleId: Long
    ): Call<List<CommentEntity>>


    // -------- Comment Reactions --------

    @PUT("api/comment-reactions/{reactionId}")
    fun updateReaction(
        @Path("reactionId") reactionId: Long,
        @Body reactionRequest: ReactionRequest,
    ): Call<CommentReactionEntity>

    @DELETE("api/comment-reactions/{reactionId}")
    fun deleteReaction(
        @Path("reactionId") reactionId: Long,
    ): Call<Void>

    @GET("api/comment-reactions/{reactionId}")
    fun getReactionById(
        @Path("reactionId") reactionId: Long
    ): Call<CommentReactionEntity>

    @GET("api/comment-reactions/getReaction/comment/{commentId}")
    fun getReactionsByCommentId(
        @Path("commentId") commentId: Long
    ): Call<List<CommentReactionEntity>>

    @POST("api/comment-reactions/comment/{commentId}")
    fun addReaction(
        @Path("commentId") commentId: Long,
        @Body reactionRequest: ReactionRequest
    ): Call<Int>

    @GET("api/comment-reactions/comment/{commentId}/count")
    fun getReactionCountByCommentId(
        @Path("commentId") commentId: Long
    ): Call<Int>

    @GET("api/comment-reactions/comment/{commentId}/is-reacted")
    fun isReacted(
        @Path("commentId") commentId: Long
    ): Call<Boolean>
}
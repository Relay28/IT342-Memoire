package com.example.memoire.models

import com.google.gson.annotations.SerializedName

data class UserSearchDTO(
    val id: Long,
    val username: String,
    val name: String,
    val email: String,
    @SerializedName("profilePicture")
    val profilePicture: String?
)

data class GrantAccessRequest(
    val capsuleId: Long,
    val userId: Long,
    val role: String
)

data class UserSearchResult(
    val userId: Long,
    val username: String,
    val email: String,
    val profilePicture: String?
)

data class UserSearchResponse(
    val results: List<UserSearchResult>,
    val page: Int,
    val size: Int
)
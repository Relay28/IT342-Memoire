package com.example.memoire.models


data class User(
    val id: Long? = null,
    val username: String,
    val email: String,
    val password: String?,
    val profilePicture: String?,
    val isActive: Boolean,
    val role: String,
    val isOauthUser: Boolean,
    val createdAt: String?
)


data class ProfileDTO(
    val userId: Long?,
    val username: String,
    val email: String,
    val profilePicture: String?,
    val biography: String?,
    val createdAt: String?,
    val role: String,
    val oauthUser: Boolean
)


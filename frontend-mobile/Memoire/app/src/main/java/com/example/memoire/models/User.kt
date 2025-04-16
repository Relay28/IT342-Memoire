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





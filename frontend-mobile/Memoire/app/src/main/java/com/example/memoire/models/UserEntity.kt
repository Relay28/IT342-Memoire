package com.example.memoire.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class UserEntity(
    val id: Long,
    val username: String,
    val email: String,
    val name: String?,
    @SerializedName("profilePicture")
    val profilePicture: String?,
    val biography: String?,
    @SerializedName("isActive")
    val isActive: Boolean,
    val role: String,
    @SerializedName("createdAt")
    val createdAt: Date? = null
)
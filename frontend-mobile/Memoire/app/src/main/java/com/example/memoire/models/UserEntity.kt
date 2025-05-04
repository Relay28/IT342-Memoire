package com.example.memoire.models

import android.util.Base64
import com.google.gson.annotations.SerializedName
import java.util.Date

data class UserEntity(
    val id: Long,
    val username: String,
    val email: String,
    val name: String?,
    val profilePictureData: String?,
    val biography: String?,
    @SerializedName("isActive")
    val isActive: Boolean,
    val role: String,
    @SerializedName("createdAt")
    val createdAt: Date? = null,
    val friendshipsAsUser: String? = null
)

data class UserDTO(
    val id: Long,
    val username: String,
    val email: String,
    val name: String?,
    @SerializedName("profilePicture")
    val profilePicture: String?,
    val biography: String?,
    @SerializedName("isActive")
    val active: Boolean,
    val role: String,
    @SerializedName("createdAt")
    val createdAt: Date? = null,
    val oauthUser: Boolean,
){
    // Add a function to get ByteArray from the base64 String
    fun getProfilePictureBytes(): ByteArray? {
        return if (profilePicture != null) {
            try {
                Base64.decode(profilePicture, Base64.DEFAULT)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }
}
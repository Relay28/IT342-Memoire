package com.example.memoire.models

import java.util.Date


data class ProfileDTO(
    val id: Long? = null,
    val username: String = "",
    val name: String? = null,
    val email: String? = null,
    val biography: String? = null,
    val profilePicture: String? = null,
    val isActive: Boolean = true
)


data class ProfileDTO2(
    val userId: Long? = null,
    val username: String = "",
    val name: String? = null,
    val email: String? = null,
    val biography: String? = null,
    val profilePicture: String? = null,
    val isActive: Boolean = true
)
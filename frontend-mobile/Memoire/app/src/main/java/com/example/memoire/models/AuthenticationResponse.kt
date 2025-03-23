package com.example.memoire.models

data class AuthenticationResponse(
    val token: String,
    val userId: Long,
    val username: String,
    val email: String
)

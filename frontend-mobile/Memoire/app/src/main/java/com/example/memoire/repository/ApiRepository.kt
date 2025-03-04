package com.example.memoire.repository

import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.User
import retrofit2.Response

class ApiRepository {

    // 🔹 Fetch user by ID
    suspend fun getUserById(id: Long): Response<User> {
        return RetrofitClient.instance.getUserById(id)
    }

    // 🔹 Login user
    suspend fun login(email: String, password: String) =
        RetrofitClient.instance.login(com.example.memoire.models.AuthenticationRequest(email, password))

    // 🔹 Register user
    suspend fun register(request: com.example.memoire.models.RegisterRequest) =
        RetrofitClient.instance.register(request)
}

package com.example.memoire.repository

import com.example.memoire.api.RetrofitClient
import retrofit2.Response

class ApiRepository {



    // 🔹 Login user
    suspend fun login(email: String, password: String) =
        RetrofitClient.instance.login(com.example.memoire.models.AuthenticationRequest(email, password))

    // 🔹 Register user
    suspend fun register(request: com.example.memoire.models.RegisterRequest) =
        RetrofitClient.instance.register(request)
}

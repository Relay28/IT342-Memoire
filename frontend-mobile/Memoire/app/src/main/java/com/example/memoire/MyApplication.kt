package com.example.memoire

import android.app.Application
import com.example.memoire.api.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this) // 🔹 Initialize Retrofit with context
    }
}

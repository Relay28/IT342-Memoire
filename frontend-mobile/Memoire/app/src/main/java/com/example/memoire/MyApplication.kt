package com.example.memoire

import android.app.Application
import com.example.memoire.api.RetrofitClient
import com.example.memoire.utils.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this) // 🔹 Initialize Retrofit with context
        FirebaseApp.initializeApp(this)

        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Subscribe to topics if needed
        FirebaseMessaging.getInstance().subscribeToTopic("general")
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    // Handle subscription failure
                }
            }
    }
}

package com.example.memoire.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveLoginData(token: String, userId: Long, username: String, email: String, expiresIn: Long = DEFAULT_EXPIRATION) {
        editor.apply {
            putString("token", token)
            putLong("userId", userId)
            putString("username", username)
            putString("email", email)
            putLong(KEY_TOKEN_EXPIRATION, System.currentTimeMillis() + expiresIn)
            apply()
        }
    }
    fun isTokenValid(): Boolean {
        if (!isLoggedIn()) return false
        val expirationTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRATION, 0)
        return System.currentTimeMillis() < expirationTime
    }
    fun getTokenExpirationTime(): Long {
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRATION, 0)
    }

    // Add these two new methods for FCM token
    fun saveFcmToken(token: String) {
        editor.putString("fcm_token", token).apply()
    }

    fun getFcmToken(): String? {
        return sharedPreferences.getString("fcm_token", null)
    }

    fun getUserSession(): Map<String, Any?> {
        return mapOf(
            "token" to sharedPreferences.getString("token", null),
            "userId" to sharedPreferences.getLong("userId", -1),
            "username" to sharedPreferences.getString("username", null),
            "email" to sharedPreferences.getString("email", null),
            "fcm_token" to sharedPreferences.getString("fcm_token", null) // Added FCM token to session map
        )
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getString("token", null) != null
    }
    companion object {
        private const val KEY_TOKEN_EXPIRATION = "token_expiration"
        private const val DEFAULT_EXPIRATION = 86400000L // 24 hours
    }

    fun logoutUser() {
        // Keep the FCM token when logging out since it's device-specific
        val fcmToken = getFcmToken()
        editor.clear()
        if (fcmToken != null) {
            editor.putString("fcm_token", fcmToken)
        }
        editor.apply()
    }
}
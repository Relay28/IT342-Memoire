package com.example.memoire.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    fun saveLoginData(token: String, userId: Int, username: String, email: String) {
        editor.putString("token", token)
        editor.putInt("userId", userId)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.apply() // Saves asynchronously
    }

    fun getUserSession(): Map<String, Any?> {
        return mapOf(
            "token" to sharedPreferences.getString("token", null),
            "userId" to sharedPreferences.getInt("userId", -1),
            "username" to sharedPreferences.getString("username", null),
            "email" to sharedPreferences.getString("email", null)
        )
    }



    fun isLoggedIn(): Boolean {
        return sharedPreferences.getString("token", null) != null
    }

    fun logoutUser() {
        editor.clear()
        editor.apply()
    }
}

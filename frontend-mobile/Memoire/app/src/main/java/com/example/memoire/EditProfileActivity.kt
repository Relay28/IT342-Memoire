package com.example.memoire

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.memoire.api.RetrofitClient
import com.example.memoire.com.example.memoire.HomeActivity
import com.example.memoire.models.ProfileDTO
import com.example.memoire.utils.SessionManager

class EditProfileActivity : AppCompatActivity() {
    private lateinit var currentProfile: ProfileDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editProfileView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editName = findViewById<TextInputEditText>(R.id.et_username)
        val editBio = findViewById<TextInputEditText>(R.id.et_bio)
        val saveButton = findViewById<TextView>(R.id.btn_save)

        // Fetch the current profile data and populate the fields
        fetchCurrentProfile(editName, editBio)

        // Back button functionality
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish();
        }

        // Save button click listener
        saveButton.setOnClickListener {
            val newUsername = editName.text.toString()
            val newBio = editBio.text.toString()

            // Update only the username and biography fields
            val updatedProfile = currentProfile.copy(
                username = newUsername,
                biography = newBio
            )

            // Call the updateUser API
            updateUserProfile(updatedProfile)
        }
    }

    private fun fetchCurrentProfile(editName: TextInputEditText, editBio: TextInputEditText) {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"]
        if (token == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch the current profile data
        RetrofitClient.instance.getOwnProfile("Bearer $token").enqueue(object : Callback<ProfileDTO> {
            override fun onResponse(call: Call<ProfileDTO>, response: Response<ProfileDTO>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        currentProfile = profile // Save the current profile
                        editName.setText(profile.username)
                        editBio.setText(profile.biography)
                    }
                } else {
                    Toast.makeText(this@EditProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfileActivity", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ProfileDTO>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditProfileActivity", "Network error", t)
            }
        })
    }

    private fun updateUserProfile(updatedProfile: ProfileDTO) {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"]
        if (token == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Call the updateUser API
        GlobalScope.launch {
            try {
                val response = RetrofitClient.instance.updateUser(updatedProfile)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                         val intent = Intent(this@EditProfileActivity,ProfileActivity::class.java)
                        startActivity(intent)
                        finish();
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                        Log.e("EditProfileActivity", "Error: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@EditProfileActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfileActivity", "Network error", e)
                }
            }
        }
    }
}
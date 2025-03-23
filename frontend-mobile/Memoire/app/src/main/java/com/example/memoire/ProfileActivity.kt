package com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.memoire.api.RetrofitClient
import com.example.memoire.com.example.memoire.HomeActivity
import com.example.memoire.models.ProfileDTO
import com.example.memoire.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val sessionManager = SessionManager(this)
        val userSession = sessionManager.getUserSession()

        // Find UI elements
        val tvUsername = findViewById<TextView>(R.id.tv_username_display)
        val tvFullName = findViewById<TextView>(R.id.tv_full_name)
        val tvEmail = findViewById<TextView>(R.id.tv_bio) // Using bio field for email
        val ivProfile = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.iv_profile)

        // Back button functionality
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish();
        }

        // Edit profile button functionality
        findViewById<AppCompatButton>(R.id.btn_edit_profile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Fetch and display profile data
        fetchUserProfile(tvUsername, tvFullName, tvEmail, ivProfile, sessionManager)
    }

    private fun fetchUserProfile(
        tvUsername: TextView,
        tvFullName: TextView,
        tvEmail: TextView,
        ivProfile: de.hdodenhof.circleimageview.CircleImageView,
        sessionManager: SessionManager
    ) {
        val token = sessionManager.getUserSession()["token"]
        if (token == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Call the getOwnProfile endpoint
        RetrofitClient.instance.getOwnProfile("Bearer $token").enqueue(object : Callback<ProfileDTO> {
            override fun onResponse(call: Call<ProfileDTO>, response: Response<ProfileDTO>) {
                if (response.isSuccessful) {
                    val profile = response.body()
                    if (profile != null) {
                        tvUsername.text = profile.username
                        tvFullName.text = profile.username // Adjust if full name exists
                        tvEmail.text = profile.biography?: ""

                        if (!profile.profilePicture.isNullOrEmpty()) {
                            Glide.with(this@ProfileActivity)
                                .load(profile.profilePicture)
                                .placeholder(R.drawable.ic_placeholder) // Default profile image
                                .into(ivProfile)
                        }
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileActivity", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ProfileDTO>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProfileActivity", "Network error", t)
            }
        })
    }
}
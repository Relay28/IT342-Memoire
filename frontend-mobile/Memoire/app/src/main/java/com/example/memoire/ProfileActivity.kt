package com.example.memoire

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.memoire.api.RetrofitClient
import com.example.memoire.com.example.memoire.HomeActivity
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.UserEntity
import com.example.memoire.utils.SessionManager
import com.google.android.material.progressindicator.CircularProgressIndicator
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    private val API_BASE_URL = "http://your-api-base-url.com/" // Define your API base URL here

    private lateinit var tvName: TextView
    private lateinit var Username: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvBio: TextView
    private lateinit var ivProfile: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find UI elements
        tvName = findViewById(R.id.tv_username_display)

        Username = findViewById(R.id.tv_username)
        tvFullName = findViewById(R.id.tv_full_name)
        tvBio = findViewById(R.id.tv_bio)
        ivProfile = findViewById(R.id.iv_profile)
        progressIndicator = findViewById(R.id.progress_indicator)

        // Back button functionality
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Edit profile button functionality
        findViewById<AppCompatButton>(R.id.btn_edit_profile).setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Fetch and display profile data
        fetchUserProfile()
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data when coming back from EditProfileActivity
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"].toString()

        showLoading(true)

        // First get the current user details
        RetrofitClient.instance.getCurrentUser("Bearer $token").enqueue(object : Callback<UserEntity> {
            override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {

                        displayUserData(user)
                        loadProfileImage(token)
                    }
                } else {
                    showLoading(false)
                    handleApiError(response.code(), "Failed to fetch profile")
                }

            }

            override fun onFailure(call: Call<UserEntity>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "Network error", t)
                Toast.makeText(this@ProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayUserData(user: UserEntity) {
        tvName.text = user.name?: user.username
        Username.text = user.username
        tvFullName.text = user.username
        tvBio.text = user.biography ?: getString(R.string.noBio)
        showLoading(false)
    }

    private fun loadProfileImage(token: String) {
        try {
            // Create a direct implementation to handle the image loading with proper error handling
            RetrofitClient.instance.getProfilePicture().enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        try {

                            // Process the image data
                            val inputStream = response.body()?.byteStream()
                            Log.d(TAG, "Content-Type: ${response.headers()["Content-Type"]}")

                            if (inputStream != null) {
                                // Convert to bitmap to ensure it's a valid image
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                if (bitmap != null) {
                                    // Update UI on main thread
                                    runOnUiThread {
                                        ivProfile.setImageBitmap(bitmap)
                                    }
                                } else {
                                    Log.e(TAG, "Failed to decode image from server")
                                    loadDefaultImage()
                                }
                            } else {
                                Log.e(TAG, "Empty response body")
                                loadDefaultImage()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing image data: ${e.message}")
                            loadDefaultImage()
                        }
                    } else {
                        // Log detailed error information
                        Log.e(TAG, "Server error: ${response.code()} - ${response.message()}")
                        try {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "Error body: $errorBody")
                        } catch (e: Exception) {
                            Log.e(TAG, "Couldn't read error body: ${e.message}")
                        }
                        loadDefaultImage()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "Network error: ${t.message}", t)
                    loadDefaultImage()
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Exception in loadProfileImage: ${e.message}", e)
            loadDefaultImage()
        }
    }

    private fun loadDefaultImage() {
        runOnUiThread {
            ivProfile.setImageResource(R.drawable.default_profile)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun handleApiError(code: Int, defaultMessage: String) {
        val message = when (code) {
            401 -> "Unauthorized access. Please log in again."
            403 -> "You don't have permission to perform this action."
            404 -> "User not found."
            else -> "$defaultMessage (Error $code)"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.e(TAG, message)
    }
}
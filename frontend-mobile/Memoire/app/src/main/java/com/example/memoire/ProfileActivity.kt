package com.example.memoire

import CapsuleGridAdapter
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.GridView
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.models.UserDTO

import com.example.memoire.models.UserEntity
import com.example.memoire.utils.SessionManager
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : AppCompatActivity() {
    private val TAG = "ProfileActivity"
    private lateinit var sessionManager: SessionManager
    private lateinit var tvName: TextView
    private lateinit var Username: TextView
    private lateinit var tvFullName: TextView
    private lateinit var tvBio: TextView
    private lateinit var ivProfile: de.hdodenhof.circleimageview.CircleImageView
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var gridView: GridView
    private lateinit var capsuleAdapter: CapsuleGridAdapter


    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sessionManager = SessionManager(this)
        // Find UI elements
        tvName = findViewById(R.id.tv_username_display)

        Username = findViewById(R.id.tv_username)
        tvFullName = findViewById(R.id.tv_username_display)
        tvBio = findViewById(R.id.tv_bio)
        ivProfile = findViewById(R.id.iv_profile)
        progressIndicator = findViewById(R.id.progress_indicator)

        // Back button functionality
        setupCapsuleGrid()
        findViewById<ImageView>(R.id.btn_back).setOnClickListener {
            startActivity(Intent(this, MainContainerActivity::class.java))
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
       // Toast.makeText(this@ProfileActivity, token, Toast.LENGTH_SHORT).show()
        val s = sessionManager.getUserSession()["userId"]
        showLoading(true)

        // First get the current user details
        RetrofitClient.instance.getCurrentUser().enqueue(object : Callback<UserDTO> {
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                if (response.isSuccessful) {
                    val user = response.body()


                    if (user != null) {


                        displayUserData(user)
                        loadProfileImage(user)
                    }
                } else {
                    showLoading(false)
                    handleApiError(response.code(), "Failed to fetch profile")
                }

            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "Network error", t)
                Toast.makeText(this@ProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun showMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

        popup.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_logout -> {
                    logoutUser()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun logoutUser() {
        showLoading(true)
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"].toString()

        RetrofitClient.instance.logout().enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                showLoading(false)
                if (response.isSuccessful) {
                    // Clear user session and navigate to login
                    sessionManager.logoutUser()
                    val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    handleApiError(response.code(), "Logout failed")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                showLoading(false)
                Toast.makeText(this@ProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayUserData(user: UserDTO) {
        tvName.text = user.name?: user.username
        Username.text = user.username
        tvFullName.text = user.username
        tvBio.text = user.biography ?: getString(R.string.noBio)
        showLoading(false)
    }

    private fun loadProfileImage(user: UserDTO) {
        val profilePictureBytes = user.getProfilePictureBytes()

        if (profilePictureBytes != null) {
                // Convert ByteArray to Bitmap
                val bitmap = BitmapFactory.decodeByteArray(profilePictureBytes, 0, profilePictureBytes.size)

                // Set the bitmap to your CircleImageView
                ivProfile.setImageBitmap(bitmap)
        } else {
            // Set a default image or placeholder when no image data is available
            ivProfile.setImageResource(R.drawable.ic_placeholder)
          }

    }
    // In ProfileActivity.kt
    private fun setupCapsuleGrid() {
        gridView = findViewById(R.id.gv_capsules)
        fetchPublishedCapsules { capsulesWithContent ->

            Log.d(TAG, "GridView visibility: ${gridView.visibility}")
            capsuleAdapter = CapsuleGridAdapter(this, capsulesWithContent) { capsule ->
                val intent = Intent(this@ProfileActivity, SinglePublishedCapsuleActivity::class.java).apply {
                    putExtra("capsuleId", capsule.id)
                }
                startActivity(intent)
            }

            gridView.adapter = capsuleAdapter
            gridView.post {
                gridView.requestLayout()
            }
        }
    }


    private fun fetchPublishedCapsules(onComplete: (List<TimeCapsuleDTO>) -> Unit) {
        showLoading(true)
        Log.d(TAG, "Fetching published capsules")

        RetrofitClient.instance.getPublishedTimeCapsules().enqueue(object : Callback<List<TimeCapsuleDTO>> {
            override fun onResponse(
                call: Call<List<TimeCapsuleDTO>>,
                response: Response<List<TimeCapsuleDTO>>
            ) {
                Log.d(TAG, "Response received: ${response.isSuccessful}")
                if (response.isSuccessful) {
                    val capsules = response.body() ?: emptyList()
                    Log.d(TAG, "Capsules received: ${capsules.size}")
                    capsules.forEach { Log.d(TAG, "Capsule: $it") }
                    fetchContentsForCapsules(capsules, onComplete)
                } else {
                    Log.e(TAG, "Failed to fetch capsules: ${response.code()} - ${response.message()}")
                    showLoading(false)
                    handleApiError(response.code(), "Failed to fetch capsules")
                    onComplete(emptyList())
                }
            }

            override fun onFailure(call: Call<List<TimeCapsuleDTO>>, t: Throwable) {
                Log.e(TAG, "Failed to fetch capsules", t)
                showLoading(false)
                Toast.makeText(
                    this@ProfileActivity,
                    "Failed to load capsules: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                onComplete(emptyList())
            }
        })
    }

    private fun fetchContentsForCapsules(
        capsules: List<TimeCapsuleDTO>,
        onComplete: (List<TimeCapsuleDTO>) -> Unit
    ) {
        var completedRequests = 0
        val totalCapsules = capsules.size
        val capsulesWithContent = mutableListOf<TimeCapsuleDTO>()

        if (capsules.isEmpty()) {
            Log.d(TAG, "No capsules to fetch content for")
            showLoading(false)
            onComplete(emptyList())
            return
        }

        for (capsule in capsules) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    capsule.id?.let { id ->
                        Log.d(TAG, "Fetching contents for capsule ID: $id")
                        val response = RetrofitClient.capsuleContentInstance.getContentsByCapsule(id)
                        withContext(Dispatchers.Main) {
                            completedRequests++
                            if (response.isSuccessful) {
                                val contents = response.body() ?: emptyList()
                                Log.d(TAG, "Contents for capsule ID $id: $contents")
                                val capsuleWithContent = capsule.copy(contents = contents)
                                capsulesWithContent.add(capsuleWithContent)
                            } else {
                                Log.e(TAG, "Failed to fetch contents for capsule ID $id: ${response.code()} - ${response.message()}")
                            }

                            if (completedRequests == totalCapsules) {
                                showLoading(false)
                                Log.d(TAG, "Final capsulesWithContent: $capsulesWithContent")
                                onComplete(capsulesWithContent)

                                // Update adapter with the fetched data
                                capsuleAdapter.updateData(capsulesWithContent)
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        completedRequests++
                        Log.e(TAG, "Error fetching contents for capsule ID ${capsule.id}", e)

                        if (completedRequests == totalCapsules) {
                            showLoading(false)
                            onComplete(capsulesWithContent)

                            // Update adapter with the fetched data
                            capsuleAdapter.updateData(capsulesWithContent)
                        }
                    }
                }
            }
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
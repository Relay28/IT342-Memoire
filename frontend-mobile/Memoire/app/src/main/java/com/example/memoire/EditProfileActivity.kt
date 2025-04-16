package com.example.memoire

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.ProfileDTO
import com.example.memoire.models.UserEntity
import com.example.memoire.utils.SessionManager
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var currentUser: UserEntity
    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var usernameField: TextInputEditText
    private lateinit var nameField: TextInputEditText
    private lateinit var bioField: TextInputEditText
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var saveButton: TextView

    private val REQUEST_STORAGE_PERMISSION = 100
    private val TAG = "EditProfileActivity"
    private val API_BASE_URL = "http://your-api-base-url.com/" // Define the API base URL here or get it from a config

    // Activity result launcher for image picking
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                profileImageView.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editProfileView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize UI components
        usernameField = findViewById(R.id.et_username)
        nameField = findViewById(R.id.et_name)
        bioField = findViewById(R.id.et_bio)
        saveButton = findViewById(R.id.btn_save)
        profileImageView = findViewById(R.id.iv_profile)
        progressIndicator = findViewById(R.id.progress_indicator)

        val changePhotoText = findViewById<TextView>(R.id.tv_change_photo)
        val editOverlay = findViewById<android.widget.FrameLayout>(R.id.iv_edit_overlay)

        // Fetch the current user data and populate the fields
        fetchCurrentUser()

        // Set click listener for image selection via text
        changePhotoText.setOnClickListener {
            if (checkStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }

        // Set click listener for image selection via overlay
        editOverlay.setOnClickListener {
            if (checkStoragePermission()) {
                openImagePicker()
            } else {
                requestStoragePermission()
            }
        }

        // Back button functionality
        findViewById<android.widget.ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // Save button click listener
        saveButton.setOnClickListener {
            // Show loading indicator
            showLoading(true)

            // Check if user attempted to change username
            if (usernameField.text.toString() != currentUser.username) {
                // Show an error message
                Toast.makeText(this, "Username cannot be changed", Toast.LENGTH_SHORT).show()
                // Reset the username field to current value
                usernameField.setText(currentUser.username)
                showLoading(false)
                return@setOnClickListener
            }

            // Create updated profile object
            val updatedProfile = ProfileDTO(
                id = currentUser.id,
                username = currentUser.username,
                name = nameField.text.toString(),
                email = currentUser.email,
                biography = bioField.text.toString()
            )

            // Handle profile update with or without image
            if (selectedImageUri != null) {
                uploadProfileImage(updatedProfile)
            } else {
                updateUserProfile(updatedProfile)
            }
        }
    }

    private fun fetchCurrentUser() {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"]
        if (token == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading(true)

        // Use Retrofit client to fetch current user data
        RetrofitClient.instance.getCurrentUser("Bearer $token").enqueue(object : Callback<UserEntity> {
            override fun onResponse(call: Call<UserEntity>, response: Response<UserEntity>) {
                showLoading(false)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        currentUser = user
                        populateUserData(user)
                    }
                } else {
                    handleApiError(response.code(), "Failed to fetch profile")
                }
            }

            override fun onFailure(call: Call<UserEntity>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "Network error", t)
                Toast.makeText(this@EditProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateUserData(user: UserEntity) {
        // Set fields
        usernameField.setText(user.username)
        // Make username field non-editable
        usernameField.isEnabled = false
        usernameField.setTextColor(getColor(R.color.gray))

        nameField.setText(user.name ?: "")
        bioField.setText(user.biography ?: "")

        // Load profile image if available
        user.profilePicture?.let { imageUrl ->
            if (imageUrl.isNotEmpty()) {
                loadProfileImage()
            }
        }
    }

    private fun loadProfileImage() {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"] ?: return

        try {
            // Create a GlideUrl with headers for authorization
            val glideUrl = GlideUrl(
                "$API_BASE_URL/api/users/profile-picture",
                LazyHeaders.Builder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            )

            // Use Glide to load the image with the header
            Glide.with(this)
                .load(glideUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(profileImageView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image", e)
        }
    }

    private fun updateUserProfile(profile: ProfileDTO) {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"]
        if (token == null) {
            showLoading(false)
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use Retrofit client to update user profile
                val response = RetrofitClient.instance.updateUser("Bearer $token", profile)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        handleApiError(response.code(), "Failed to update profile")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Log.e(TAG, "Network error", e)
                    Toast.makeText(this@EditProfileActivity, "Network Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadProfileImage(profile: ProfileDTO) {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getUserSession()["token"]
        if (token == null) {
            showLoading(false)
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Get the file from the URI
            val imageFile = getFileFromUri(selectedImageUri!!)
            if (imageFile == null) {
                showLoading(false)
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                return
            }

            // Create request body
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("profileImg", imageFile.name, requestFile)

            // Upload the image using Retrofit client
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.instance.uploadProfileImage("Bearer $token", imagePart)

                    // After image upload, update the rest of the profile
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            updateUserProfile(profile)
                        } else {
                            showLoading(false)
                            handleApiError(response.code(), "Failed to upload image")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Log.e(TAG, "Image upload error", e)
                        Toast.makeText(this@EditProfileActivity, "Image upload error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            showLoading(false)
            Log.e(TAG, "Error processing image", e)
            Toast.makeText(this, "Error processing image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")

            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024) // 4k buffer
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }

            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file", e)
            return null
        }
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_STORAGE_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            Toast.makeText(
                this,
                "Permission required to select profile image",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        saveButton.isEnabled = !isLoading
    }

    private fun handleApiError(code: Int, defaultMessage: String) {
        val message = when (code) {
            401 -> "Unauthorized access. Please log in again."
            403 -> "You don't have permission to perform this action."
            404 -> "User not found."
            409 -> "Username or email already exists."
            422 -> "Invalid data provided."
            else -> "$defaultMessage (Error $code)"
        }

        Snackbar.make(findViewById(R.id.editProfileView), message, Snackbar.LENGTH_LONG).show()
    }
}
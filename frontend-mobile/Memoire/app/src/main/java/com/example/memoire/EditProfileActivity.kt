package com.example.memoire

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
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
import com.example.memoire.models.UserDTO
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class EditProfileActivity : AppCompatActivity() {
    private lateinit var currentUser: UserDTO
    private lateinit var profileImageView: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var usernameField: TextInputEditText
    private lateinit var nameField: TextInputEditText
    private lateinit var bioField: TextInputEditText
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var saveButton: TextView
    private lateinit var sessionManager: SessionManager

    private val REQUEST_READ_EXTERNAL_STORAGE = 100
    private val REQUEST_CAMERA = 101
    private val REQUEST_IMAGE_CAPTURE = 102
    private val TAG = "EditProfileActivity"
    private val API_BASE_URL = RetrofitClient.BASE_URL

    // Activity result launcher for image picking
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                loadSelectedImage(uri)
            }
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let { bitmap ->
                selectedImageUri = getImageUriFromBitmap(bitmap)
                profileImageView.setImageBitmap(bitmap)
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

        sessionManager = SessionManager(this)
        initializeViews()
        fetchCurrentUser()
    }

    private fun initializeViews() {
        usernameField = findViewById(R.id.et_username)
        nameField = findViewById(R.id.et_name)
        bioField = findViewById(R.id.et_bio)
        saveButton = findViewById(R.id.btn_save)
        profileImageView = findViewById(R.id.iv_profile)
        progressIndicator = findViewById(R.id.progress_indicator)

        val changePhotoText = findViewById<TextView>(R.id.tv_change_photo)
        val editOverlay = findViewById<android.widget.FrameLayout>(R.id.iv_edit_overlay)

        // Single click listener for both change photo options
        val imageSelectionListener = View.OnClickListener {
            if (checkStoragePermission()) {
                showImageSourceDialog()
            } else {
                requestStoragePermission()
            }
        }

        changePhotoText.setOnClickListener(imageSelectionListener)
        editOverlay.setOnClickListener(imageSelectionListener)

        findViewById<android.widget.ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        saveButton.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun saveProfileChanges() {
        showLoading(true)

        if (usernameField.text.toString() != currentUser.username) {
            Toast.makeText(this, "Username cannot be changed", Toast.LENGTH_SHORT).show()
            usernameField.setText(currentUser.username)
            showLoading(false)
            return
        }

        val updatedProfile = ProfileDTO(
            id = currentUser.id,
            username = currentUser.username,
            name = nameField.text.toString(),
            email = currentUser.email,
            biography = bioField.text.toString()
        )

        if (selectedImageUri != null) {
            uploadProfileImage(updatedProfile)
        } else {
            updateUserProfile(updatedProfile)
        }
    }

    private fun fetchCurrentUser() {
        val token = sessionManager.getUserSession()["token"] ?: run {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        showLoading(true)

        RetrofitClient.instance.getCurrentUser().enqueue(object : Callback<UserDTO> {
            override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                showLoading(false)

                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        currentUser = user
                        populateUserData(user)
                    }
                } else {
                    handleApiError(response.code(), "Failed to fetch profile")
                }
            }

            override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "Network error", t)
                Toast.makeText(this@EditProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun populateUserData(user: UserDTO) {
        usernameField.setText(user.username)
        usernameField.isEnabled = false
        usernameField.setTextColor(getColor(R.color.gray))

        nameField.setText(user.name ?: "")
        bioField.setText(user.biography ?: "")

        loadProfileImage(user)
    }

    private fun loadProfileImage(user: UserDTO) {
        try {

            Glide.with(this)
                .load(user.getProfilePictureBytes())
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(profileImageView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading profile image", e)
        }
    }

    private fun loadSelectedImage(uri: Uri) {
        try {
            Glide.with(this)
                .load(uri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(profileImageView)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading selected image", e)
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserProfile(profile: ProfileDTO) {
        val token = sessionManager.getUserSession()["token"] ?: run {
            showLoading(false)
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.updateUser(profile)
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
        val token = sessionManager.getUserSession()["token"] ?: run {
            showLoading(false)
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        selectedImageUri?.let { uri ->
            try {
                val imageFile = getFileFromUri(uri) ?: run {
                    showLoading(false)
                    Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
                    return
                }

                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("profileImg", imageFile.name, requestFile)

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.instance.uploadProfileImage("Bearer $token", imagePart)
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
        } ?: run {
            showLoading(false)
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")

            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to file", e)
            null
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Profile", null)
        return Uri.parse(path)
    }

    // Permission handling methods
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImageSourceDialog()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                        )
                    ) {
                        showPermissionExplanationDialog()
                    } else {
                        showPermissionPermanentlyDeniedDialog()
                    }
                }
            }
            REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }
        }
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change Profile Photo")
            .setItems(arrayOf("Take Photo", "Choose from Gallery")) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA)
        } else {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.resolveActivity(packageManager)?.let {
                takePictureLauncher.launch(takePictureIntent)
            } ?: Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Needed")
            .setMessage("This permission is required to change your profile picture")
            .setPositiveButton("Grant") { _, _ -> requestStoragePermission() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionPermanentlyDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Please enable permission in app settings to change your profile picture")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
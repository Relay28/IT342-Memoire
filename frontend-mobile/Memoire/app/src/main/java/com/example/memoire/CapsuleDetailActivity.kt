package com.example.memoire

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.memoire.api.CapsuleContentService
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CapsuleContentEntity
import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.utils.SessionManager
import com.example.memoire.websocket.CapsuleContentStompService
import com.example.memoire.websocket.CapsuleContentWebSocketListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
import java.io.InputStream
import java.util.UUID

class CapsuleDetailActivity : AppCompatActivity(), CapsuleContentWebSocketListener {
    private lateinit var capsuleId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var webSocketService: CapsuleContentStompService
    private lateinit var apiService: CapsuleContentService

    private val contentList = mutableListOf<CapsuleContentEntity>()
    private var currentUploadingFile: Uri? = null
    private var isNewCapsule = false

    private var isOwner = false
    private lateinit var sessionManager: SessionManager

    private val filePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                currentUploadingFile = it
                uploadFile()

            }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_capsule_detail)

        // Initialize services
        RetrofitClient.init(applicationContext)
        apiService = RetrofitClient.capsuleContentInstance
        webSocketService = CapsuleContentStompService(this, this)
        isNewCapsule = intent.getBooleanExtra("isNewCapsule", false)
        sessionManager = SessionManager(this)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isNewCapsule) {
                    checkAndDeleteIfEmpty {
                        // After checking/deletion, finish the activity
                        this@CapsuleDetailActivity.finish()
                    }
                } else {
                    // If not a new capsule, just finish normally
                    this@CapsuleDetailActivity.finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        // Get capsule ID from intent
        capsuleId = intent.getStringExtra("capsuleId") ?: run {
            Toast.makeText(this, "No capsule ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return

        }

        // Setup UI
        progressBar = findViewById(R.id.progressBar)
        titleTextView = findViewById(R.id.capsuleTitle)
        descriptionTextView = findViewById(R.id.capsuleDescription)
        recyclerView = findViewById(R.id.contentRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        adapter = ContentAdapter(contentList)
        recyclerView.adapter = adapter

        // Setup back button
        findViewById<ImageView>(R.id.backButton).setOnClickListener {

            if (isNewCapsule) {
                checkAndDeleteIfEmpty {
                    // After checking/deletion, finish the activity
                    this@CapsuleDetailActivity.finish()
                }
            } else {
                // If not a new capsule, just finish normally
                this@CapsuleDetailActivity.finish()
            }
        }

        fun onBackPressed() {
            // Check if this is a new capsule and if it should be deleted
            if (isNewCapsule) {
                lifecycleScope.launch {
                    val capsule = RetrofitClient.instance.getTimeCapsule(capsuleId.toLong()).execute().body()

                    // Check if the capsule is still "Untitled" with empty description and no contents
                    if (capsule != null &&
                        capsule.title == "Untitled" &&
                        (capsule.description.isNullOrEmpty() || capsule.description == "") &&
                        (capsule.contents == null || capsule.contents!!.isEmpty())) {

                        // Delete the empty capsule
                        try {
                            RetrofitClient.instance.deleteTimeCapsule(capsuleId.toLong()).execute()
                            // Optional Toast message
                            // Toast.makeText(this@CapsuleDetailActivity, "Empty capsule deleted", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            // Handle any errors silently or log them
                        }
                    }

                    // Navigate back
                    super@CapsuleDetailActivity.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        }

        // Setup FAB for uploading
        findViewById<FloatingActionButton>(R.id.fabUpload).setOnClickListener {
            filePicker.launch("*/*")
        }

        // Connect to WebSocket
        webSocketService.connect(capsuleId.toLong())

        // Load capsule details
        loadCapsuleDetails()

        // Load initial content
        loadInitialContent()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkAndDeleteIfEmpty(onComplete: () -> Unit) {
        if (isNewCapsule) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val response = RetrofitClient.instance.getTimeCapsule(capsuleId.toLong()).execute()

                    if (response.isSuccessful && response.body() != null) {
                        val capsule = response.body()!!

                        // Check if the capsule should be deleted
                        if (capsule.title == "Untitled" &&
                            (capsule.description.isNullOrEmpty() || capsule.description == "") &&
                            (capsule.contents == null || capsule.contents!!.isEmpty())) {

                            // Delete the empty capsule
                            RetrofitClient.instance.deleteTimeCapsule(capsuleId.toLong()).execute()
                        }
                    }

                    withContext(Dispatchers.Main) {
                        onComplete()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        // Even if there's an error, we should still complete the navigation
                        onComplete()
                    }
                }
            }
        } else {
            onComplete()
        }
    }
    private fun loadCapsuleDetails() {
        progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.getTimeCapsule(capsuleId.toLong()).enqueue(object : Callback<TimeCapsuleDTO> {
            override fun onResponse(call: Call<TimeCapsuleDTO>, response: Response<TimeCapsuleDTO>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val capsule = response.body()
                    Log.d("CAPSULE CONTENT ", capsule.toString())

                    titleTextView.text = capsule?.title ?: "Untitled"
                    descriptionTextView.text = capsule?.description ?: ""

                    // Check if current user is the owner
                    val currentUserId = sessionManager.getUserSession()["userId"]
                    isOwner = currentUserId == capsule?.createdById?.toLong()

                    // Set up views based on ownership
                    if (isOwner) {
                        // For owners, make views editable and set up focus change listeners
                        setupEditableViewsWithAutoSave()
                    } else {
                        // For non-owners, make views non-editable
                        setupNonEditableViews()
                    }
                } else {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Failed to load capsule details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TimeCapsuleDTO>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@CapsuleDetailActivity,
                    "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupEditableViewsWithAutoSave() {
        // Make views editable
        titleTextView.isFocusable = true
        titleTextView.isFocusableInTouchMode = true
        titleTextView.isClickable = true
        titleTextView.isLongClickable = true

        descriptionTextView.isFocusable = true
        descriptionTextView.isFocusableInTouchMode = true
        descriptionTextView.isClickable = true
        descriptionTextView.isLongClickable = true

        // Add focus change listeners for auto-save
        titleTextView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveCapsuleDetails()
            }
        }

        descriptionTextView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveCapsuleDetails()
            }
        }

        // Also save when user presses done/enter on keyboard
        titleTextView.setOnEditorActionListener { _, _, _ ->
            titleTextView.clearFocus()
            true
        }
    }

    private fun setupNonEditableViews() {
        // Make views non-editable
        titleTextView.isFocusable = false
        titleTextView.isFocusableInTouchMode = false
        titleTextView.isClickable = false
        titleTextView.isLongClickable = false

        descriptionTextView.isFocusable = false
        descriptionTextView.isFocusableInTouchMode = false
        descriptionTextView.isClickable = false
        descriptionTextView.isLongClickable = false
    }


    private var lastSaveTime = 0L
    private val SAVE_DEBOUNCE_TIME = 1000L // 1 second

    private fun saveCapsuleDetails() {
        if (!isOwner) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSaveTime < SAVE_DEBOUNCE_TIME) {
            return // Skip if we just saved recently
        }
        lastSaveTime = currentTime

        val newTitle = titleTextView.text.toString()
        val newDescription = descriptionTextView.text.toString()

        val updatedCapsule = TimeCapsuleDTO(
            id = capsuleId.toLong(),
            title = newTitle,
            description = newDescription
        )

        progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.updateTimeCapsule(capsuleId.toLong(), updatedCapsule).enqueue(object : Callback<TimeCapsuleDTO> {
            override fun onResponse(call: Call<TimeCapsuleDTO>, response: Response<TimeCapsuleDTO>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    // Show subtle feedback that changes were saved
                    titleTextView.clearFocus()
                    descriptionTextView.clearFocus()
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Changes saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Failed to save changes", Toast.LENGTH_SHORT).show()
                    // Revert changes if update failed
                    loadCapsuleDetails()
                }
            }

            override fun onFailure(call: Call<TimeCapsuleDTO>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@CapsuleDetailActivity,
                    "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                // Revert changes if update failed
                loadCapsuleDetails()
            }
        })
    }

    private fun loadInitialContent() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.getContentsByCapsule(capsuleId.toLong())
                Toast.makeText(this@CapsuleDetailActivity,
                    response.toString(), Toast.LENGTH_SHORT).show()
                if (response.isSuccessful) {
                    contentList.clear()
                    response.body()?.let { contentList.addAll(it) }
                    adapter.notifyDataSetChanged()
                    updateEmptyState()
                } else {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Failed to load content", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CapsuleDetailActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun uploadFile() {
        currentUploadingFile?.let { uri ->
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    val file = File(cacheDir, "upload_${UUID.randomUUID()}")
                    inputStream?.use { it.copyTo(file.outputStream()) }

                    val requestFile = file.asRequestBody(
                        contentResolver.getType(uri)?.toMediaTypeOrNull()
                    )
                    val filePart = MultipartBody.Part.createFormData(
                        "file", file.name, requestFile
                    )

                    val response = apiService.uploadContent(capsuleId.toLong(), filePart)
                    if (response.isSuccessful) {
                        // WebSocket will handle the update notification
                        Toast.makeText(this@CapsuleDetailActivity,
                            "Upload started", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CapsuleDetailActivity,
                            "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    progressBar.visibility = View.GONE
                    currentUploadingFile = null
                }
            }
        }
    }

    // WebSocket Listener Methods
    override fun onConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected to real-time updates", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisconnected(reason: String) {
        runOnUiThread {
            Toast.makeText(this, "Disconnected: $reason", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInitialContentReceived(contents: List<CapsuleContentEntity>) {
        runOnUiThread {
            contentList.clear()
            contentList.addAll(contents)
            adapter.notifyDataSetChanged()
            updateEmptyState()
        }
    }

    override fun onContentUpdated(content: CapsuleContentEntity, action: String) {
        runOnUiThread {
            when (action) {
                "add" -> {
                    contentList.add(content)
                    adapter.notifyItemInserted(contentList.size - 1)
                }
                "update" -> {
                    val index = contentList.indexOfFirst { it.id == content.id }
                    if (index >= 0) {
                        contentList[index] = content
                        adapter.notifyItemChanged(index)
                    }
                }
            }
            updateEmptyState()
        }
    }

    override fun onContentDeleted(contentId: Long) {
        runOnUiThread {
            val index = contentList.indexOfFirst { it.id == contentId }
            if (index >= 0) {
                contentList.removeAt(index)
                adapter.notifyItemRemoved(index)
                updateEmptyState()
            }
        }
    }

    private fun updateEmptyState() {
        findViewById<TextView>(R.id.emptyStateText).isVisible = contentList.isEmpty()
        recyclerView.isVisible = contentList.isNotEmpty()
    }

    override fun onDestroy() {
        if (isNewCapsule) {
            checkAndDeleteIfEmpty {}
        }
        webSocketService.disconnect()
        super.onDestroy()
    }
    inner class ContentAdapter(private val contents: List<CapsuleContentEntity>) :
        RecyclerView.Adapter<ContentAdapter.ContentViewHolder>() {

        inner class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.contentThumbnail)
            val titleView: TextView = view.findViewById(R.id.contentTitle)
            val deleteBtn: ImageButton = view.findViewById(R.id.deleteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_content, parent, false)
            return ContentViewHolder(view)
        }

        override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
            val content = contents[position]

            // Set default file icon initially
            holder.imageView.setImageResource(R.drawable.ic_file)

            // Set a title if available
            holder.titleView.text = content.uploadedBy ?: "File ${position + 1}"

            // Show/hide delete button based on permissions
            holder.deleteBtn.visibility = if (isOwner) View.VISIBLE else View.GONE

            when {
                content.contentType?.startsWith("image/") == true -> {
                    // Show loading indicator if needed
                    holder.imageView.setImageResource(R.drawable.loading)

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            // Use your existing downloadContent API to get the byte[] data
                            val response = apiService.downloadContent(content.id!!)
                            if (response.isSuccessful) {
                                // The response body now contains the raw byte[] data from the database
                                val byteStream = response.body()?.byteStream()
                                val bitmap = BitmapFactory.decodeStream(byteStream)

                                withContext(Dispatchers.Main) {
                                    if (bitmap != null) {
                                        holder.imageView.setImageBitmap(bitmap)
                                    } else {
                                        // If we couldn't decode as bitmap, show generic image icon
                                        holder.imageView.setImageResource(R.drawable.ic_image)
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    holder.imageView.setImageResource(R.drawable.ic_file)
                                    Toast.makeText(this@CapsuleDetailActivity,
                                        "Failed to load image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                holder.imageView.setImageResource(R.drawable.ic_file)
                                Log.e("ContentAdapter", "Error loading image", e)
                            }
                        }
                    }
                }
                content.contentType?.startsWith("video/") == true -> {
                    holder.imageView.setImageResource(R.drawable.ic_video)
                }
                content.contentType?.startsWith("audio/") == true -> {
                    holder.imageView.setImageResource(R.drawable.ic_audio)
                }
                else -> {
                    holder.imageView.setImageResource(R.drawable.ic_file)
                }
            }

            holder.itemView.setOnClickListener {
                openContent(content)
            }

            holder.deleteBtn.setOnClickListener {
                showDeleteConfirmation(content)
            }
        }


        override fun getItemCount() = contents.size
    }

    private fun showDeleteConfirmation(content: CapsuleContentEntity) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Content")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteContent(content.id!!)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun deleteContent(contentId: Long) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.deleteContent(contentId)
                if (response.isSuccessful) {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Content deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Failed to delete content", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CapsuleDetailActivity,
                    "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun openContent(content: CapsuleContentEntity) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.downloadContent(content.id!!)
                if (response.isSuccessful) {
                    // For all file types, save to cache first
                    val fileName = content.contentType ?: "file_${content.id}.${getExtensionFromMimeType(content.contentType)}"
                    val file = File(cacheDir, fileName)

                    response.body()?.byteStream()?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Create content URI for the file
                    val contentUri = androidx.core.content.FileProvider.getUriForFile(
                        this@CapsuleDetailActivity,
                        "${packageName}.fileprovider",
                        file
                    )

                    withContext(Dispatchers.Main) {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(contentUri, content.contentType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(this@CapsuleDetailActivity,
                                "No app found to open this file type", Toast.LENGTH_LONG).show()
                        }
                        progressBar.visibility = View.GONE
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CapsuleDetailActivity,
                            "Failed to download file", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    // Helper function to get a file extension from MIME type
    private fun getExtensionFromMimeType(mimeType: String?): String {
        return when {
            mimeType == null -> "bin"
            mimeType.startsWith("image/jpeg") -> "jpg"
            mimeType.startsWith("image/png") -> "png"
            mimeType.startsWith("image/gif") -> "gif"
            mimeType.startsWith("video/mp4") -> "mp4"
            mimeType.startsWith("audio/mpeg") -> "mp3"
            mimeType.startsWith("text/plain") -> "txt"
            mimeType.startsWith("application/pdf") -> "pdf"
            else -> "bin"
        }
    }

    private fun getMimeType(url: String): String {
        val extension = url.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }
}
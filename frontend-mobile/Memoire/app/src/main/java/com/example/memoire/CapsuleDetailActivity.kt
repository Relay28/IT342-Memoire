package com.example.memoire

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.example.memoire.websocket.CapsuleContentWebSocketService
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

class CapsuleDetailActivity : AppCompatActivity(), CapsuleContentWebSocketService.CapsuleContentWebSocketListener {
    private lateinit var capsuleId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContentAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var webSocketService: CapsuleContentWebSocketService
    private lateinit var apiService: CapsuleContentService

    private val contentList = mutableListOf<CapsuleContentEntity>()
    private var currentUploadingFile: Uri? = null

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
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
        webSocketService = CapsuleContentWebSocketService(SessionManager(this))
        webSocketService.listener = this

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
            finish()
        }

        // Setup FAB for uploading
        findViewById<FloatingActionButton>(R.id.fabUpload).setOnClickListener {
            filePicker.launch("*/*")
        }

        // Connect to WebSocket
        webSocketService.connectToCapsule(capsuleId.toLong())

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

    private fun loadCapsuleDetails() {
        progressBar.visibility = View.VISIBLE
        RetrofitClient.instance.getTimeCapsule(capsuleId.toLong()).enqueue(object : Callback<TimeCapsuleDTO> {
            override fun onResponse(call: Call<TimeCapsuleDTO>, response: Response<TimeCapsuleDTO>) {
                if (response.isSuccessful) {
                    val capsule = response.body()
                    titleTextView.text = capsule?.title ?: "Unnamed Capsule"
                    descriptionTextView.text = capsule?.description ?: "No description"
                } else {
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Failed to load capsule details", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TimeCapsuleDTO>, t: Throwable) {
                Toast.makeText(this@CapsuleDetailActivity,
                    "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadInitialContent() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = apiService.getContentsByCapsule(capsuleId.toLong())
                if (response.isSuccessful) {
                    contentList.clear()
                    response.body()?.let { contentList.addAll(it) }
                    adapter.notifyDataSetChanged()

                    // Show empty state if no content
                    findViewById<TextView>(R.id.emptyStateText).isVisible = contentList.isEmpty()
                    recyclerView.isVisible = contentList.isNotEmpty()
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
                        response.body()?.let {
                            contentList.add(it)
                            adapter.notifyItemInserted(contentList.size - 1)

                            // Hide empty state if content was added
                            findViewById<TextView>(R.id.emptyStateText).isVisible = false
                            recyclerView.isVisible = true
                        }
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
    override fun onInitialContentsReceived(contents: List<CapsuleContentEntity>) {
        runOnUiThread {
            contentList.clear()
            contentList.addAll(contents)
            adapter.notifyDataSetChanged()

            // Update empty state
            findViewById<TextView>(R.id.emptyStateText).isVisible = contentList.isEmpty()
            recyclerView.isVisible = contentList.isNotEmpty()
        }
    }

    override fun onContentUpdated(content: CapsuleContentEntity) {
        runOnUiThread {
            val index = contentList.indexOfFirst { it.id == content.id }
            if (index >= 0) {
                contentList[index] = content
                adapter.notifyItemChanged(index)
            } else {
                contentList.add(content)
                adapter.notifyItemInserted(contentList.size - 1)

                // Hide empty state if content was added
                findViewById<TextView>(R.id.emptyStateText).isVisible = false
                recyclerView.isVisible = true
            }
        }
    }

    override fun onContentDeleted(contentId: Long) {
        runOnUiThread {
            val index = contentList.indexOfFirst { it.id == contentId }
            if (index >= 0) {
                contentList.removeAt(index)
                adapter.notifyItemRemoved(index)

                // Show empty state if no content left
                findViewById<TextView>(R.id.emptyStateText).isVisible = contentList.isEmpty()
                recyclerView.isVisible = contentList.isNotEmpty()
            }
        }
    }

    override fun onConnectionEstablished() {
        runOnUiThread {
            Toast.makeText(this, "Connected to real-time updates", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionFailed(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Connection failed: $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onConnectionClosed() {
        runOnUiThread {
            Toast.makeText(this, "Disconnected from updates", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserListUpdated(users: List<String>) {
        // Not implemented in this activity
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketService.disconnect()
    }

    // RecyclerView Adapter
    //kotlin// RecyclerView Adapter
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
            // Set content title (filename)
            holder.titleView.text = content.filePath?.substringAfterLast('/') ?: "Unknown"

            when {
                content.contentType?.startsWith("image/") == true -> {
                    // Instead of using Glide directly, load the image through our API service
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val response = apiService.downloadContent(content.id!!.toLong())
                            if (response.isSuccessful) {
                                // Convert the response to a bitmap
                                val bitmap = BitmapFactory.decodeStream(response.body()?.byteStream())

                                // Update UI on the main thread
                                withContext(Dispatchers.Main) {
                                    holder.imageView.setImageBitmap(bitmap)
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    holder.imageView.setImageResource(R.drawable.ic_file)
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                holder.imageView.setImageResource(R.drawable.ic_file)
                            }
                        }
                    }
                }
                content.contentType?.startsWith("video/") == true -> {
                    holder.imageView.setImageResource(R.drawable.ic_video)
                }
                else -> {
                    holder.imageView.setImageResource(R.drawable.ic_file)
                }
            }

            // Set click listener to open content
            holder.itemView.setOnClickListener {
                openContent(content)
            }

            // Set delete button listener
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
                deleteContent(content.id!!.toLong())
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
                    // If WebSocket doesn't handle it properly, manually remove from list
                    val index = contentList.indexOfFirst { it.id == contentId }
                    if (index >= 0) {
                        contentList.removeAt(index)
                        adapter.notifyItemRemoved(index)

                        // Show empty state if no content left
                        findViewById<TextView>(R.id.emptyStateText).isVisible = contentList.isEmpty()
                        recyclerView.isVisible = contentList.isNotEmpty()
                    }
                    Toast.makeText(this@CapsuleDetailActivity,
                        "Content deleted successfully", Toast.LENGTH_SHORT).show()
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
        val intent = Intent(Intent.ACTION_VIEW).apply {
            val url = "${RetrofitClient.BASE_URL}api/capsule-content/${content.id}/download"
            val mimeType = content.contentType ?: getMimeType(url)
            setDataAndType(Uri.parse(url), mimeType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)
    }

    private fun getMimeType(url: String): String {
        val extension = url.substringAfterLast('.', "")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "*/*"
    }


}
package com.example.memoire.com.example.memoire

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.BaseActivity
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.ProfileActivity
import com.example.memoire.R
import com.example.memoire.adapters.NotificationAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.NotificationDTO
import com.example.memoire.models.NotificationEntity
import com.example.memoire.utils.SessionManager
import com.example.memoire.websocket.NotificationWebSocketListener
import com.example.memoire.websocket.NotificationWebsocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActivity : BaseActivity(), NotificationWebSocketListener {

    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var websocketService: NotificationWebsocketService
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)
        RetrofitClient.init(applicationContext)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_notification)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.notificationsRecyclerView)
        notificationAdapter = NotificationAdapter(mutableListOf()) { notification ->
            // Handle notification click
            markNotificationAsRead(notification)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = notificationAdapter

        // Initialize WebSocket service
        websocketService = NotificationWebsocketService(this, this)
        websocketService.connect()

        // Load initial notifications
        loadInitialNotifications()
        setupHeaderActions()
        setupBottomNavigation(R.id.navigation_tags)

        val profile = findViewById<ImageView>(R.id.prof)
        profile.setOnClickListener {
            val intent = Intent(this@NotificationActivity, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

        val notificationBtn = findViewById<ImageView>(R.id.ivNotification)
        notificationBtn.setOnClickListener {
            val intent = Intent(this@NotificationActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val searchbtn = findViewById<ImageView>(R.id.ivSearch)
        searchbtn.setOnClickListener {
            val intent = Intent(this@NotificationActivity, SearchActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun loadInitialNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getNotifications(false).execute()
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    Log.d("NotificationActivity", "Loaded notifications: $notifications")
                    withContext(Dispatchers.Main) {
                        notificationAdapter.updateNotifications(notifications)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("NotificationActivity",
                        "Error: ${response.code()} - $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@NotificationActivity,
                            "Failed to load notifications",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationActivity", "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@NotificationActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun markNotificationAsRead(notification: NotificationEntity) {
        if (!notification.isRead) {
            websocketService.markNotificationAsRead(notification.id)
            val position = notificationAdapter.notifications.indexOfFirst { it.id == notification.id }
            if (position != -1) {
                notificationAdapter.markNotificationAsRead(position)
            }
        }
        // Handle navigation based on notification type
        handleNotificationNavigation(notification)
    }

    private fun handleNotificationNavigation(notification: NotificationEntity) {
        when (notification.itemType) {
            "TimeCapsule" -> {
                // Navigate to time capsule detail
                val intent = Intent(this, CapsuleDetailActivity::class.java).apply {
                    putExtra("timeCapsuleId", notification.relatedItemId)
                }
                startActivity(intent)
            }
            // Add other cases as needed
        }
    }



    override fun onDestroy() {
        super.onDestroy()
        websocketService.disconnect()
    }

    // WebSocket callbacks
    override fun onConnected() {
        runOnUiThread {
            Toast.makeText(this, "Connected to notifications", Toast.LENGTH_SHORT).show()
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

    override fun onNotificationReceived(notification: NotificationEntity) {
        runOnUiThread {
            notificationAdapter.addNotification(notification)
            // Show a toast for new notifications
            Toast.makeText(this, notification.text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNotificationCountUpdated(count: Long) {
        // Update badge count if you have one
    }

    override fun onInitialNotificationsReceived(notifications: List<NotificationEntity>) {
        runOnUiThread {
            notificationAdapter.updateNotifications(notifications)
        }
    }
}
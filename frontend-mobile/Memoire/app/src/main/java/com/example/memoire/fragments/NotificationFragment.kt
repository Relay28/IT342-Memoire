package com.example.memoire.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.CapsuleDetailActivity
import com.example.memoire.R
import com.example.memoire.adapters.NotificationAdapter
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.NotificationEntity
import com.example.memoire.websocket.NotificationWebSocketListener
import com.example.memoire.websocket.NotificationWebsocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationFragment : Fragment(), NotificationWebSocketListener {
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var websocketService: NotificationWebsocketService
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        RetrofitClient.init(requireContext())

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.notificationsRecyclerView)
        notificationAdapter = NotificationAdapter(mutableListOf()) { notification ->
            // Handle notification click
            markNotificationAsRead(notification)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = notificationAdapter

        // Initialize WebSocket service
        websocketService = NotificationWebsocketService(requireContext(), this)
        websocketService.connect()

        // Load initial notifications
        loadInitialNotifications()
    }

    private fun loadInitialNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getNotifications(false).execute()
                if (response.isSuccessful) {
                    val notifications = response.body() ?: emptyList()
                    Log.d("NotificationFragment", "Loaded notifications: $notifications")
                    withContext(Dispatchers.Main) {
                        notificationAdapter.updateNotifications(notifications)
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    Log.e("NotificationFragment",
                        "Error: ${response.code()} - $errorBody")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Failed to load notifications",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationFragment", "Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
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
                val intent = Intent(requireActivity(), CapsuleDetailActivity::class.java).apply {
                    putExtra("timeCapsuleId", notification.relatedItemId)
                }
                startActivity(intent)
            }
            // Add other cases as needed
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        websocketService.disconnect()
    }

    // WebSocket callbacks
    override fun onConnected() {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), "Connected to notifications", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisconnected(reason: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), "Disconnected: $reason", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNotificationReceived(notification: NotificationEntity) {
        activity?.runOnUiThread {
            notificationAdapter.addNotification(notification)
            // Show a toast for new notifications
            Toast.makeText(requireContext(), notification.text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNotificationCountUpdated(count: Long) {
        // Update badge count if you have one
    }

    override fun onInitialNotificationsReceived(notifications: List<NotificationEntity>) {
        activity?.runOnUiThread {
            notificationAdapter.updateNotifications(notifications)
        }
    }
}
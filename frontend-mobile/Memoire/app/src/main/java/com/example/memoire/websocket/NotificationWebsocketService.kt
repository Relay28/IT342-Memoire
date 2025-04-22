package com.example.memoire.websocket

import android.content.Context
import com.example.memoire.models.NotificationDTO
import com.example.memoire.utils.SessionManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage
import java.util.*
import kotlin.collections.ArrayList

class NotificationWebsocketService(
    private val context: Context,
    private val listener: NotificationWebSocketListener
) {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()
    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private var sessionId: String? = null
    private var username: String? = null

    fun connect() {
        if (!sessionManager.isLoggedIn()) {
            listener.onError("User not logged in")
            return
        }

        val sessionData = sessionManager.getUserSession()
        val token = sessionData["token"] as? String ?: run {
            listener.onError("No authentication token available")
            return
        }

        username = sessionData["username"] as? String

        // Create headers with authorization
        val headers = ArrayList<StompHeader>()
        headers.add(StompHeader("Authorization", "Bearer $token"))

        // Initialize STOMP client with SockJS
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "ws://192.168.1.8:8080/ws-notifications/websocket" // SockJS endpoint for notifications
        )

        stompClient?.let { client ->
            // Track connection state
            var isConnected = false

            // Subscribe to lifecycle events
            compositeDisposable.add(
                client.lifecycle()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { lifecycleEvent ->
                        when (lifecycleEvent.type) {
                            LifecycleEvent.Type.OPENED -> {
                                isConnected = true
                                listener.onConnected()
                                subscribeToChannels()
                            }
                            LifecycleEvent.Type.ERROR -> {
                                listener.onError(lifecycleEvent.exception?.message ?: "Unknown error")
                            }
                            LifecycleEvent.Type.CLOSED -> {
                                isConnected = false
                                listener.onDisconnected("Connection closed")
                            }
                            LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                                listener.onError("Server heartbeat failed")
                            }
                        }
                    }
            )

            // Connect with headers
            client.connect(headers)
        }
    }

    private fun subscribeToChannels() {
        stompClient?.let { client ->
            username?.let { uname ->
                // Subscribe to user-specific notifications
                compositeDisposable.add(
                    client.topic("/topic/notifications/$uname")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { message ->
                            handleNotificationMessage(message)
                        }
                )

                // Subscribe to notification count updates
                compositeDisposable.add(
                    client.topic("/topic/notifications/count/$uname")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { message ->
                            handleCountUpdateMessage(message)
                        }
                )

                // Send connection request to the server
                client.send("/app/notifications/connect", "")
                    .subscribe()
            }
        }
    }

    private fun handleNotificationMessage(message: StompMessage) {
        try {
            val notification = gson.fromJson(message.payload, NotificationDTO::class.java)
            listener.onNotificationReceived(notification)
        } catch (e: Exception) {
            listener.onError("Failed to parse notification: ${e.message}")
        }
    }

    private fun handleCountUpdateMessage(message: StompMessage) {
        try {
            val count = JSONObject(message.payload).getLong("count")
            listener.onNotificationCountUpdated(count)
        } catch (e: Exception) {
            listener.onError("Failed to parse count update: ${e.message}")
        }
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
        sessionId = null
        username = null
    }

    fun markNotificationAsRead(notificationId: Long) {
        stompClient?.send(
            "/app/notifications/mark-read",
            JSONObject().apply { put("notificationId", notificationId) }.toString()
        )?.subscribe()
    }

    fun markAllNotificationsAsRead() {
        stompClient?.send("/app/notifications/mark-all-read", "")?.subscribe()
    }
}

interface NotificationWebSocketListener {
    fun onConnected()
    fun onDisconnected(reason: String)
    fun onError(error: String)
    fun onNotificationReceived(notification: NotificationDTO)
    fun onNotificationCountUpdated(count: Long)
    fun onInitialNotificationsReceived(notifications: List<NotificationDTO>)
}
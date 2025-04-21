package com.example.memoire.websocket

import android.content.Context
import com.example.memoire.models.CapsuleContentEntity
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

class CapsuleContentStompService(
    private val context: Context,
    private val listener: CapsuleContentWebSocketListener
) {
    private val sessionManager = SessionManager(context)
    private val gson = Gson()
    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()
    private var sessionId: String? = null

    fun connect(capsuleId: Long) {
        if (!sessionManager.isLoggedIn()) {
            listener.onError("User not logged in")
            return
        }

        val sessionData = sessionManager.getUserSession()
        val token = sessionData["token"] as? String ?: run {
            listener.onError("No authentication token available")
            return
        }

        // Create headers with authorization
        val headers = ArrayList<StompHeader>()
        headers.add(StompHeader("Authorization", "Bearer $token"))
        headers.add(StompHeader("Capsule-ID", capsuleId.toString()))

        // Initialize STOMP client with SockJS
        stompClient = Stomp.over(
            Stomp.ConnectionProvider.OKHTTP,
            "ws://192.168.1.8:8080/ws-capsule-content/websocket" // SockJS endpoint
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
                                subscribeToChannels(capsuleId)
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

    private fun subscribeToChannels(capsuleId: Long) {
        stompClient?.let { client ->
            // Subscribe to initial content queue (user-specific)
            compositeDisposable.add(
                client.topic("/user/queue/capsule-content/initial")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { message ->
                        handleInitialContentMessage(message)
                    }
            )

            // Subscribe to content updates topic
            compositeDisposable.add(
                client.topic("/topic/capsule-content/updates/$capsuleId")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { message ->
                        handleUpdateMessage(message)
                    }
            )

            // Send connection request to the server
            val connectMessage = JSONObject().apply {
                put("capsuleId", capsuleId)
            }
            client.send(
                "/app/capsule-content/connect/$capsuleId",
                connectMessage.toString()
            ).subscribe()
        }
    }

    private fun handleInitialContentMessage(message: StompMessage) {
        try {
            // Extract session ID from the STOMP message
            sessionId = extractSessionIdFromMessage(message)

            val payload = message.payload
            val contents = gson.fromJson<List<CapsuleContentEntity>>(
                payload,
                object : TypeToken<List<CapsuleContentEntity>>() {}.type
            )
            listener.onInitialContentReceived(contents)
        } catch (e: Exception) {
            listener.onError("Failed to parse initial content: ${e.message}")
        }
    }

    private fun extractSessionIdFromMessage(message: StompMessage): String? {
        // The StompMessage doesn't directly expose headers, but we can parse them from the raw
        // For this library version, we need to get the session ID differently
        // Option 1: If your server includes it in the message payload
        // Option 2: If you need to track it separately

        // For now, we'll return null since the library version doesn't expose headers
        // You might need to modify your server to include session ID in the message payload
        return null
    }

    private fun handleUpdateMessage(message: StompMessage) {
        try {
            val payload = JSONObject(message.payload)
            when (payload.getString("action")) {
                "add", "update" -> {
                    val content = gson.fromJson(
                        payload.toString(),
                        CapsuleContentEntity::class.java
                    )
                    listener.onContentUpdated(content, payload.getString("action"))
                }
                "delete" -> {
                    listener.onContentDeleted(payload.getLong("contentId"))
                }
            }
        } catch (e: Exception) {
            listener.onError("Failed to parse update message: ${e.message}")
        }
    }

    fun disconnect() {
        compositeDisposable.clear()
        stompClient?.disconnect()
        sessionId = null
    }

    fun sendContentAction(capsuleId: Long, contentId: Long, action: String) {
        val message = JSONObject().apply {
            put("contentId", contentId)
            put("action", action)
        }
        stompClient?.send(
            "/app/capsule-content/$capsuleId/action",
            message.toString()
        )?.subscribe()
    }

    fun requestContentRefresh(capsuleId: Long) {
        stompClient?.send(
            "/app/capsule-content/$capsuleId/refresh",
            ""
        )?.subscribe()
    }
}
interface CapsuleContentWebSocketListener {
    fun onConnected()
    fun onDisconnected(reason: String)
    fun onError(error: String)
    fun onInitialContentReceived(contents: List<CapsuleContentEntity>)
    fun onContentUpdated(content: CapsuleContentEntity, action: String)
    fun onContentDeleted(contentId: Long)
}
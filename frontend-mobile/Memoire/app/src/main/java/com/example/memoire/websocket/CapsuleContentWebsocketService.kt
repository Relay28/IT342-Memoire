package com.example.memoire.websocket

import android.util.Log
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.CapsuleContentEntity
import com.example.memoire.utils.SessionManager
import com.google.gson.Gson
import okhttp3.*
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

class CapsuleContentWebSocketService(
    private val sessionManager: SessionManager,
    private val baseUrl: String = RetrofitClient.getBaseUrl()
) {
    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val reconnectDelay = 5000L // 5 seconds

    // Listener for WebSocket events
    var listener: CapsuleContentWebSocketListener? = null

    // Current capsule ID being monitored
    private var currentCapsuleId: Long? = null

    // Client with timeout settings
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    interface CapsuleContentWebSocketListener {
        fun onInitialContentsReceived(contents: List<CapsuleContentEntity>)
        fun onContentUpdated(content: CapsuleContentEntity)
        fun onContentDeleted(contentId: Long)
        fun onConnectionEstablished()
        fun onConnectionFailed(error: String)
        fun onConnectionClosed()
        fun onUserListUpdated(users: List<String>)
    }

    fun connectToCapsule(capsuleId: Long) {
        currentCapsuleId = capsuleId

        val token = sessionManager.getUserSession()["token"] ?: run {
            listener?.onConnectionFailed("No authentication token available")
            return
        }

        val wsUrl = baseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://") + "ws-capsule-content"

        val request = Request.Builder()
            .url(wsUrl)
            .header("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to capsule $capsuleId")
                reconnectAttempts = 0
                sendConnectMessage(capsuleId)
                listener?.onConnectionEstablished()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Connection closed: $reason")
                listener?.onConnectionClosed()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Connection failed: ${t.message}")
                handleReconnect()
                listener?.onConnectionFailed(t.message ?: "Unknown error")
            }
        })
    }

    private fun sendConnectMessage(capsuleId: Long) {
        val connectMessage = JSONObject().apply {
            put("type", "connect")
            put("capsuleId", capsuleId)
        }.toString()

        webSocket?.send(connectMessage)
    }

    private fun handleIncomingMessage(message: String) {
        try {
            val json = JSONObject(message)
            when (json.optString("type")) {
                "initial" -> handleInitialContents(json)
                "update" -> handleContentUpdate(json)
                "delete" -> handleContentDeletion(json)
                "user_list" -> handleUserListUpdate(json)
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing message: ${e.message}")
        }
    }

    private fun handleInitialContents(json: JSONObject) {
        val contentsJson = json.getJSONArray("contents")
        val contents = mutableListOf<CapsuleContentEntity>()

        for (i in 0 until contentsJson.length()) {
            val contentJson = contentsJson.getJSONObject(i)
            val content = Gson().fromJson(contentJson.toString(), CapsuleContentEntity::class.java)
            contents.add(content)
        }

        listener?.onInitialContentsReceived(contents)
    }

    private fun handleContentUpdate(json: JSONObject) {
        val content = Gson().fromJson(json.toString(), CapsuleContentEntity::class.java)
        listener?.onContentUpdated(content)
    }

    private fun handleContentDeletion(json: JSONObject) {
        val contentId = json.getLong("contentId")
        listener?.onContentDeleted(contentId)
    }

    private fun handleUserListUpdate(json: JSONObject) {
        val usersJson = json.getJSONArray("users")
        val users = mutableListOf<String>()

        for (i in 0 until usersJson.length()) {
            users.add(usersJson.getString(i))
        }

        listener?.onUserListUpdated(users)
    }

    private fun handleReconnect() {
        if (reconnectAttempts < maxReconnectAttempts) {
            reconnectAttempts++
            Log.d("WebSocket", "Attempting to reconnect ($reconnectAttempts/$maxReconnectAttempts)...")

            coroutineScope.launch {
                delay(reconnectDelay)
                currentCapsuleId?.let { connectToCapsule(it) }
            }
        }
    }

    // Make sure to cancel coroutines when disconnecting
    fun disconnect() {
        webSocket?.close(1000, "User initiated disconnect")
        webSocket = null
        currentCapsuleId = null
        coroutineScope.cancel() // Cancel any pending reconnect jobs
    }

    fun sendContentUpdate(content: CapsuleContentEntity) {
        val message = JSONObject().apply {
            put("type", "content_update")
            put("capsuleId", currentCapsuleId)
            put("content", JSONObject(Gson().toJson(content)))
            put("eventId", UUID.randomUUID().toString())
        }.toString()

        webSocket?.send(message)
    }

    fun sendContentDeletion(contentId: Long) {
        val message = JSONObject().apply {
            put("type", "content_delete")
            put("capsuleId", currentCapsuleId)
            put("contentId", contentId)
            put("eventId", UUID.randomUUID().toString())
        }.toString()

        webSocket?.send(message)
    }

    fun isConnected(): Boolean {
        return webSocket != null
    }
}
package com.example.memoire.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.memoire.MainActivity
import com.example.memoire.R
import com.example.memoire.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(applicationContext)
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_DEBUG", "Refreshed token: $token")
        sessionManager.saveFcmToken(token)

        // Immediately log if token is saved
        Log.d("FCM_DEBUG", "Token saved in SharedPrefs: ${sessionManager.getFcmToken()}")

        if (sessionManager.isLoggedIn()) {
            val userId = sessionManager.getUserSession()["userId"] as? Long
            Log.d("FCM_DEBUG", "Attempting to register token for user: $userId")
            userId?.let { registerFcmTokenWithServer(token, it) }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d("FCM", "Notification Body: ${notification.body}")
            sendNotification(
                notification.title ?: getString(R.string.app_name),
                notification.body ?: "",
                remoteMessage.data
            )
        }

        // Also handle data payload if present
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Data payload: ${remoteMessage.data}")
            sendNotification(
                remoteMessage.data["title"] ?: getString(R.string.app_name),
                remoteMessage.data["message"] ?: "New notification",
                remoteMessage.data
            )
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "memoir_notifications",  // Hardcoded to match everywhere
                "Memoire Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "All app notifications"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "memoir_notifications"  // Match the channel ID above
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun registerFcmTokenWithServer(token: String, userId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Implement your API call to register token
                // Example: RetrofitClient.instance.registerFcmToken(userId, token)
                Log.d("FCM", "Token registration attempt for user $userId")
            } catch (e: Exception) {
                Log.e("FCM", "Token registration failed", e)
            }
        }
    }
}
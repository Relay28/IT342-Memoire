//package com.example.memoire.services
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.core.app.NotificationManagerCompat
//import com.example.memoire.utils.NotificationUtils
//
//import dagger.hilt.android.AndroidEntryPoint
//import dagger.hilt.android.EntryPointAccessors
//import javax.inject.Inject
//
//@AndroidEntryPoint
//class NotificationActionReceiver : BroadcastReceiver() {
//
//    @Inject
//    lateinit var notificationUtils: NotificationUtils
//
//    override fun onReceive(context: Context, intent: Intent) {
//        val action = intent.getStringExtra("action")
//        val notificationId = intent.getIntExtra("notificationId", -1)
//
//        if (notificationId != -1) {
//            NotificationManagerCompat.from(context).cancel(notificationId)
//        }
//
//        when (action) {
//            "accept" -> {
//                // Handle accept action
//                val itemId = intent.getStringExtra("itemId")
//                Log.d("NotificationAction", "Accepted item: $itemId")
//            }
//            "decline" -> {
//                // Handle decline action
//                val itemId = intent.getStringExtra("itemId")
//                Log.d("NotificationAction", "Declined item: $itemId")
//            }
//        }
//
//        // You might want to send this action to your server
//        // notificationUtils.handleNotificationAction(action, intent.extras)
//    }
//}
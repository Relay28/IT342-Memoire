package com.example.memoire.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoire.R
import com.example.memoire.models.NotificationEntity
import com.example.memoire.utils.TimeUtils
import java.time.LocalDateTime

class NotificationAdapter(
    val notifications: MutableList<NotificationEntity>,
    private val onNotificationClick: (NotificationEntity) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
        holder.itemView.setOnClickListener { onNotificationClick(notification) }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<NotificationEntity>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()
    }

    fun addNotification(notification: NotificationEntity) {
        notifications.add(0, notification)
        notifyItemInserted(0)
    }

    fun markNotificationAsRead(position: Int) {
        notifications[position].isRead = true
        notifyItemChanged(position)
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvText: TextView = itemView.findViewById(R.id.tvNotificationText)
        private val tvTime: TextView = itemView.findViewById(R.id.tvNotificationTime)
        private val ivReadStatus: ImageView = itemView.findViewById(R.id.ivReadStatus)

        fun bind(notification: NotificationEntity) {
            tvText.text = notification.text ?: "No content"

            try {
                tvTime.text = notification.createdAt?.let { TimeUtils.getRelativeTime(it) } ?: "Recently"
            } catch (e: Exception) {
                Log.e("Notification", "Error formatting time", e)
                tvTime.text = "Recently"
            }

            ivReadStatus.visibility = if (notification.isRead) View.GONE else View.VISIBLE
        }
    }
}
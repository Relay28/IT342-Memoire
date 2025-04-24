package com.example.memoire.models

import java.time.LocalDateTime

data class NotificationDTO(
    val id: Long,
    val userId: Long,
    val type: String,
    val text: String,
    val relatedItemId: Long,
    val itemType: String,
    var isRead: Boolean,
    val createdAt: LocalDateTime
)
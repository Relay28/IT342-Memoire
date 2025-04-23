package com.example.memoire.utils

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


// TimeUtils.kt
object TimeUtils {
    private val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    )

    fun getRelativeTime(dateTime: Any?): String {
        if (dateTime == null) return "Recently"

        val parsedDateTime = when (dateTime) {
            is LocalDateTime -> dateTime
            is String -> tryParseDateTime(dateTime)
            else -> null
        } ?: return "Recently"

        val duration = Duration.between(parsedDateTime, LocalDateTime.now())

        return when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toHours() < 1 -> "${duration.toMinutes()} min ago"
            duration.toDays() < 1 -> "${duration.toHours()} hours ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            else -> parsedDateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        }
    }

    private fun tryParseDateTime(dateString: String): LocalDateTime? {
        return formatters.firstNotNullOfOrNull { formatter ->
            try {
                LocalDateTime.parse(dateString, formatter)
            } catch (e: Exception) {
                null
            }
        }
    }
}
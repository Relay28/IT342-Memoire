package com.example.memoire.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    // For display in UI
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val displayTimeFormat = SimpleDateFormat("h:mm a", Locale.US)

    // For API communication (ISO 8601 format)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun formatDateForDisplay(date: Date): String = displayDateFormat.format(date)
    fun formatTimeForDisplay(date: Date): String = displayTimeFormat.format(date)

    fun formatForApi(date: Date): String = apiDateFormat.format(date)

    fun parseFromApi(dateString: String): Date? = try {
        apiDateFormat.parse(dateString)
    } catch (e: Exception) {
        null
    }
}
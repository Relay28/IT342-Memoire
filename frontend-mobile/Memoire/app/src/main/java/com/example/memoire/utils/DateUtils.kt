package com.example.memoire.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    // For display in UI - using Philippines locale
    private val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale("fil", "PH"))
    private val displayTimeFormat = SimpleDateFormat("h:mm a", Locale("fil", "PH"))

    // For API communication (ISO 8601 format)
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun formatDateForDisplay(date: Date): String = displayDateFormat.format(date)
    fun formatTimeForDisplay(date: Date): String = displayTimeFormat.format(date)

    // This method converts local Philippines time to UTC for API consumption
    fun formatForApi(localDate: Date): String {
        val utcTime = Date(localDate.time)
        Log.d("DateUtils", "Local time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(localDate)}")
        Log.d("DateUtils", "UTC time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(utcTime)}")
        return apiDateFormat.format(utcTime)
    }

    // This method converts UTC time from API to local Philippines time
    fun parseFromApi(dateString: String): Date? = try {
        val utcDate = apiDateFormat.parse(dateString)
        utcDate?.let { convertToLocalTime(it) }
    } catch (e: Exception) {
        Log.e("DateUtils", "Failed to parse date: $dateString", e)
        null
    }

    // Helper method to convert UTC dates to Philippine time for display
    fun convertToLocalTime(utcDate: Date): Date {
        val philippinesTimeZone = TimeZone.getTimeZone("Asia/Manila")
        val offsetMs = philippinesTimeZone.getOffset(utcDate.time)
        return Date(utcDate.time + offsetMs)
    }
}
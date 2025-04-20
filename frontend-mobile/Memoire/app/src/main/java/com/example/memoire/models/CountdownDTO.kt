package com.example.memoire.models

data class CountdownDTO(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Int,
    val isOpen: Boolean
) {
    // Helper function to format the countdown as a string
    fun formattedCountdown(): String {
        return if (isOpen) {
            "Ready to open!"
        } else {
            String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds)
        }
    }

    // Helper function to check if the countdown has finished
    fun hasFinished(): Boolean {
        return days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0
    }

    // Helper function to get total remaining seconds
    fun totalSeconds(): Long {
        return days * 86400 + hours * 3600 + minutes * 60 + seconds
    }
}
package com.example.autocarservice.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Format a timestamp to a readable date string
 */
fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return format.format(date)
}

/**
 * Format a timestamp to a readable time string
 */
fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(date)
}

/**
 * Format a timestamp to a readable date and time string
 */
fun formatDateTime(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}

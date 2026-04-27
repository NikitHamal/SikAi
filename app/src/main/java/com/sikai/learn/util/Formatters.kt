package com.sikai.learn.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "—"
    val units = arrayOf("B", "KB", "MB", "GB")
    var size = bytes.toDouble()
    var unit = 0
    while (size >= 1024 && unit < units.lastIndex) {
        size /= 1024
        unit++
    }
    return if (size >= 100 || unit == 0) "${size.toInt()} ${units[unit]}"
    else "%.1f %s".format(size, units[unit])
}

fun formatTimestamp(millis: Long): String {
    if (millis <= 0) return "—"
    return SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(millis))
}

fun formatDate(millis: Long): String {
    if (millis <= 0) return "—"
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
}

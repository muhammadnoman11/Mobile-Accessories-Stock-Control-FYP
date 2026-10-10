package com.fyp.mobileaccessories.stockcontrol.util


// ============================================================
// FILE: util/DateUtils.kt
// Date formatting helpers
// ============================================================

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun currentTimestamp(): Long = System.currentTimeMillis()
}
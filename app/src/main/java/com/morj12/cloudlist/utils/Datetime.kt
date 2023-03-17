package com.morj12.cloudlist.utils

import android.icu.text.SimpleDateFormat
import java.util.*

object Datetime {

    private const val DEFAULT_TIME_FORMAT = "yyyy.MM.dd - hh:mm:ss a"

    fun getCurrentTime(): String {
        val formatter = SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault())
        return formatter.format(Calendar.getInstance().time)
    }

    fun getTimeStamp(datetime: String): Long {
        val date = SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault()).parse(datetime)
        return date.time
    }

    fun getDateTime(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat(DEFAULT_TIME_FORMAT, Locale.getDefault()).format(date)
    }
}
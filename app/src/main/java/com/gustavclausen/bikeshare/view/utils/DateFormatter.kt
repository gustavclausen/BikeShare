package com.gustavclausen.bikeshare.view.utils

import java.text.SimpleDateFormat
import java.util.*

class DateFormatter {

    companion object {
        fun fullTimestamp(timestamp: Date): String =
            format(
                "EEE, MMM dd, yyyy @ HH:mm",
                timestamp
            )

        fun fullDate(timestamp: Date): String =
            format("dd/MM/yy", timestamp)

        fun hourMinute(timestamp: Date): String =
            format("HH:mm", timestamp)

        private fun format(stringFormat: String, timestamp: Date): String {
            val dateFormat = SimpleDateFormat(stringFormat, Locale.ENGLISH)
            return dateFormat.format(timestamp)
        }
    }
}
package com.example.balikin.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateFormatter {

    private val localeId = Locale.forLanguageTag("id-ID")
    private val standardDateFormat = SimpleDateFormat("dd MMM yyyy", localeId)
    private val fullDateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", localeId)

    /**
     * Formats an epoch timestamp into standard Indonesian date format (e.g., "15 Okt 2026").
     */
    fun formatStandard(timestamp: Long): String {
        return standardDateFormat.format(Date(timestamp))
    }

    /**
     * Formats an epoch timestamp into full Indonesian date format (e.g., "Kamis, 15 Oktober 2026").
     */
    fun formatFull(timestamp: Long): String {
        return fullDateFormat.format(Date(timestamp))
    }

    /**
     * Converts a UTC midnight timestamp from Material 3 DatePicker into a local midday (12:00 PM) timestamp.
     * This eliminates timezone boundary shifting where selecting date D in timezones behind UTC
     * formats as date D-1 (mundur 1 hari).
     */
    fun utcMillisToLocalDateMillis(utcMillis: Long): Long {
        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = utcMillis
        }
        val year = utcCal.get(Calendar.YEAR)
        val month = utcCal.get(Calendar.MONTH)
        val day = utcCal.get(Calendar.DAY_OF_MONTH)

        val localCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return localCal.timeInMillis
    }

    /**
     * Converts a local date timestamp into UTC midnight timestamp for initializing Material 3 DatePicker.
     */
    fun localDateMillisToUtcMillis(localMillis: Long): Long {
        val localCal = Calendar.getInstance().apply {
            timeInMillis = localMillis
        }
        val year = localCal.get(Calendar.YEAR)
        val month = localCal.get(Calendar.MONTH)
        val day = localCal.get(Calendar.DAY_OF_MONTH)

        val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }
        return utcCal.timeInMillis
    }

    /**
     * Returns a human-friendly due date string relative to current time:
     * - "Hari ini"
     * - "Besok"
     * - "Kemarin"
     * - "Sisa X hari"
     * - "Terlambat X hari"
     */
    fun getFriendlyDueLabel(dueDate: Long, currentTime: Long = System.currentTimeMillis()): String {
        val daysOffset = StatusHelper.getDayOffsetFromNow(dueDate, currentTime)
        return when {
            daysOffset == 0L -> "Jatuh tempo hari ini"
            daysOffset == 1L -> "Sisa 1 hari (Besok)"
            daysOffset > 1L -> "Sisa $daysOffset hari"
            daysOffset == -1L -> "Terlambat 1 hari"
            else -> "Terlambat ${-daysOffset} hari"
        }
    }
}

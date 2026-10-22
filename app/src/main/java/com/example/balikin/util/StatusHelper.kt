package com.example.balikin.util

import java.util.Calendar

enum class TransactionStatus(val displayName: String) {
    ACTIVE("Aktif"),
    OVERDUE("Terlambat"),
    COMPLETED("Selesai")
}

object StatusHelper {

    /**
     * Shared status calculation function used across UI, ViewModels, and Statistics.
     * Evaluates whether an unreturned transaction has passed its due date based on local end-of-day.
     */
    fun calculateStatus(
        returnedDate: Long?,
        dueDate: Long,
        currentTime: Long = System.currentTimeMillis()
    ): TransactionStatus {
        if (returnedDate != null) return TransactionStatus.COMPLETED

        // End of the due date in local calendar timezone (23:59:59.999)
        val endOfDueDay = Calendar.getInstance().apply {
            timeInMillis = dueDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return if (currentTime > endOfDueDay) {
            TransactionStatus.OVERDUE
        } else {
            TransactionStatus.ACTIVE
        }
    }

    /**
     * Validates that due date calendar day is >= lending date calendar day.
     * Prevents cases where due date is set earlier than the transaction start date.
     */
    fun isDueDateValid(lendingDate: Long, dueDate: Long): Boolean {
        val startDay = Calendar.getInstance().apply {
            timeInMillis = lendingDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dueDay = Calendar.getInstance().apply {
            timeInMillis = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return dueDay >= startDay
    }

    /**
     * Returns the difference in calendar days between today and the due date.
     * Positive value = remaining days (e.g. 3 = "Sisa 3 hari", 0 = "Hari ini").
     * Negative value = days overdue (e.g. -2 = "Terlambat 2 hari").
     */
    fun getDayOffsetFromNow(dueDate: Long, currentTime: Long = System.currentTimeMillis()): Long {
        val currentCal = Calendar.getInstance().apply {
            timeInMillis = currentTime
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val dueCal = Calendar.getInstance().apply {
            timeInMillis = dueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = dueCal.timeInMillis - currentCal.timeInMillis
        return diffMillis / (24 * 60 * 60 * 1000)
    }
}

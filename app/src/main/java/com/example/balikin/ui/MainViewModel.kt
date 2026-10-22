package com.example.balikin.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.balikin.data.local.AppDatabase
import com.example.balikin.data.local.TransactionDirection
import com.example.balikin.data.local.TransactionEntity
import com.example.balikin.data.repository.TransactionRepository
import com.example.balikin.util.PhotoStorageHelper
import com.example.balikin.util.SoundEffectHelper
import com.example.balikin.util.StatusHelper
import com.example.balikin.util.TransactionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StatsData(
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val overdueCount: Int = 0,
    val completedCount: Int = 0,
    val lentCount: Int = 0,
    val borrowedCount: Int = 0,
    val completionRate: Int = 0,
    val categoryCounts: Map<String, Int> = emptyMap()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val context: android.content.Context
        get() = getApplication<Application>().applicationContext

    init {
        val database = AppDatabase.getInstance(application)
        repository = TransactionRepository(database.transactionDao())
        SoundEffectHelper.init(application)
    }

    // Time ticker state to trigger UI recalculations when app resumes or day advances
    private val _lastResumedTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastResumedTimestamp: StateFlow<Long> = _lastResumedTimestamp.asStateFlow()


    fun onResume() {
        _lastResumedTimestamp.value = System.currentTimeMillis()
    }

    // Search and filter states for Home (Beranda)
    val homeSearchQuery = MutableStateFlow("")
    val homeDirectionFilter = MutableStateFlow<TransactionDirection?>(null)

    // Search and filter states for History (Riwayat)
    val historySearchQuery = MutableStateFlow("")
    val historyDirectionFilter = MutableStateFlow<TransactionDirection?>(null)

    // Filtered Active Transactions for Beranda
    val filteredActiveTransactions: StateFlow<List<TransactionEntity>> = combine(
        repository.activeTransactions,
        homeSearchQuery,
        homeDirectionFilter,
        _lastResumedTimestamp
    ) { transactions, query, directionFilter, _ ->
        transactions.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.itemName.contains(query, ignoreCase = true) ||
                    item.personName.contains(query, ignoreCase = true)

            val matchesDirection = directionFilter == null || item.direction == directionFilter.name

            matchesQuery && matchesDirection
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filtered Completed Transactions for Riwayat
    val filteredCompletedTransactions: StateFlow<List<TransactionEntity>> = combine(
        repository.completedTransactions,
        historySearchQuery,
        historyDirectionFilter
    ) { transactions, query, directionFilter ->
        transactions.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.itemName.contains(query, ignoreCase = true) ||
                    item.personName.contains(query, ignoreCase = true)

            val matchesDirection = directionFilter == null || item.direction == directionFilter.name

            matchesQuery && matchesDirection
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard & Statistics data dynamically calculated from all transactions
    val statsData: StateFlow<StatsData> = combine(
        repository.allTransactions,
        _lastResumedTimestamp
    ) { transactions, _ ->
        val now = System.currentTimeMillis()
        var active = 0
        var overdue = 0
        var completed = 0
        var lent = 0
        var borrowed = 0
        val categoryMap = mutableMapOf<String, Int>()

        for (item in transactions) {
            val status = StatusHelper.calculateStatus(item.returnedDate, item.dueDate, now)
            when (status) {
                TransactionStatus.ACTIVE -> active++
                TransactionStatus.OVERDUE -> overdue++
                TransactionStatus.COMPLETED -> completed++
            }

            if (item.direction == TransactionDirection.LENT.name) {
                lent++
            } else {
                borrowed++
            }

            categoryMap[item.category] = (categoryMap[item.category] ?: 0) + 1
        }

        val total = transactions.size
        val rate = if (total > 0) ((completed.toDouble() / total) * 100).toInt() else 0

        StatsData(
            totalCount = total,
            activeCount = active,
            overdueCount = overdue,
            completedCount = completed,
            lentCount = lent,
            borrowedCount = borrowed,
            completionRate = rate,
            categoryCounts = categoryMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsData()
    )

    // Home Summary Counters
    val homeSummary: StateFlow<Triple<Int, Int, Int>> = combine(
        repository.activeTransactions,
        _lastResumedTimestamp
    ) { activeList, _ ->
        val now = System.currentTimeMillis()
        var lentCount = 0
        var borrowedCount = 0
        var overdueCount = 0

        for (item in activeList) {
            val status = StatusHelper.calculateStatus(item.returnedDate, item.dueDate, now)
            if (status == TransactionStatus.OVERDUE) {
                overdueCount++
            }
            if (item.direction == TransactionDirection.LENT.name) {
                lentCount++
            } else {
                borrowedCount++
            }
        }
        Triple(lentCount, borrowedCount, overdueCount)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Triple(0, 0, 0)
    )

    /**
     * Inserts or updates a transaction and handles local image persistence safely.
     */
    fun saveTransaction(
        transaction: TransactionEntity,
        newPhotoUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            var savedPhotoPath = transaction.photoPath
            var newlyCopiedPath: String? = null

            try {
                // If a new photo was picked from device gallery, save to context.filesDir/item_photos/
                if (newPhotoUri != null) {
                    newlyCopiedPath = PhotoStorageHelper.savePhotoFromUri(context, newPhotoUri)
                    if (newlyCopiedPath != null) {
                        savedPhotoPath = newlyCopiedPath
                    }
                }

                val entityToSave = transaction.copy(
                    photoPath = savedPhotoPath,
                    updatedAt = System.currentTimeMillis()
                )

                if (entityToSave.id == 0L) {
                    repository.insert(entityToSave)
                } else {
                    repository.update(entityToSave)
                }

                // If old photo was replaced, safely clean up old photo if not referenced elsewhere
                if (newlyCopiedPath != null && transaction.photoPath != null && transaction.photoPath != newlyCopiedPath) {
                    val count = repository.countOtherReferencesToPhoto(transaction.photoPath, transaction.id)
                    if (count == 0) {
                        PhotoStorageHelper.deletePhotoFile(transaction.photoPath)
                    }
                }

                SoundEffectHelper.play(context)
                onSuccess()
            } catch (e: Exception) {
                // Rollback newly copied file if database write failed
                if (newlyCopiedPath != null) {
                    PhotoStorageHelper.deletePhotoFile(newlyCopiedPath)
                }
                onError(e.localizedMessage ?: "Gagal menyimpan data ke database.")
            }
        }
    }

    /**
     * Marks transaction as returned.
     */
    fun markAsReturned(transactionId: Long, returnDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.markAsReturned(transactionId, returnDate)
            SoundEffectHelper.play(context)
        }
    }

    /**
     * Reverts completed transaction to active status.
     */
    fun unmarkReturned(transactionId: Long) {
        viewModelScope.launch {
            repository.unmarkReturned(transactionId)
            SoundEffectHelper.play(context)
        }
    }

    /**
     * Deletes transaction and cleans up local photo file.
     */
    fun deleteTransaction(transaction: TransactionEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(transaction)
            // Verify photo is not referenced elsewhere before deleting
            if (!transaction.photoPath.isNullOrBlank()) {
                val otherReferences = repository.countOtherReferencesToPhoto(transaction.photoPath, transaction.id)
                if (otherReferences == 0) {
                    PhotoStorageHelper.deletePhotoFile(transaction.photoPath)
                }
            }
            SoundEffectHelper.play(context)
            onComplete()
        }
    }
}

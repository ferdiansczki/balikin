package com.example.balikin.data.repository

import com.example.balikin.data.local.TransactionDao
import com.example.balikin.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository mediating between the local Room DAO and UI ViewModels.
 */
class TransactionRepository(
    private val transactionDao: TransactionDao
) {
    val activeTransactions: Flow<List<TransactionEntity>> =
        transactionDao.observeActiveTransactions()

    val completedTransactions: Flow<List<TransactionEntity>> =
        transactionDao.observeCompletedTransactions()

    val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.observeAllTransactions()

    fun observeTransactionById(id: Long): Flow<TransactionEntity?> =
        transactionDao.observeTransactionById(id)

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    suspend fun insert(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun update(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun delete(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    suspend fun deleteById(id: Long) =
        transactionDao.deleteTransactionById(id)

    suspend fun markAsReturned(id: Long, returnedDate: Long = System.currentTimeMillis()) =
        transactionDao.markAsReturned(id = id, returnedDate = returnedDate)

    suspend fun unmarkReturned(id: Long) =
        transactionDao.unmarkReturned(id = id)

    suspend fun getActiveTransactionsSync(): List<TransactionEntity> =
        transactionDao.getActiveTransactionsSync()

    suspend fun countOtherReferencesToPhoto(photoPath: String, excludeId: Long): Int =
        transactionDao.countOtherReferencesToPhoto(photoPath, excludeId)
}

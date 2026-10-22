package com.example.balikin.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun observeTransactionById(id: Long): Flow<TransactionEntity?>

    /**
     * Unreturned transactions (returned_date IS NULL) sorted by due_date ascending
     * (earliest due dates first).
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE returned_date IS NULL 
        ORDER BY due_date ASC
    """)
    fun observeActiveTransactions(): Flow<List<TransactionEntity>>

    /**
     * Completed transactions (returned_date IS NOT NULL) sorted by returned_date descending
     * (most recently returned first).
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE returned_date IS NOT NULL 
        ORDER BY returned_date DESC
    """)
    fun observeCompletedTransactions(): Flow<List<TransactionEntity>>

    /**
     * All transactions for statistics calculation, sorted by created_at descending.
     */
    @Query("SELECT * FROM transactions ORDER BY created_at DESC")
    fun observeAllTransactions(): Flow<List<TransactionEntity>>

    /**
     * Synchronous query for background checks/workers.
     */
    @Query("SELECT * FROM transactions WHERE returned_date IS NULL")
    suspend fun getActiveTransactionsSync(): List<TransactionEntity>

    /**
     * Safety check for photo deletion: verify if any other transaction references the same photo path.
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE photo_path = :photoPath AND id != :excludeId")
    suspend fun countOtherReferencesToPhoto(photoPath: String, excludeId: Long): Int

    /**
     * Mark a transaction as returned with specific returnedDate timestamp.
     */
    @Query("""
        UPDATE transactions 
        SET returned_date = :returnedDate, updated_at = :updatedAt 
        WHERE id = :id
    """)
    suspend fun markAsReturned(
        id: Long,
        returnedDate: Long,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * Reverts a returned transaction back to active (sets returned_date to NULL).
     */
    @Query("""
        UPDATE transactions 
        SET returned_date = NULL, updated_at = :updatedAt 
        WHERE id = :id
    """)
    suspend fun unmarkReturned(
        id: Long,
        updatedAt: Long = System.currentTimeMillis()
    )
}

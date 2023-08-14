package com.junianto.posedc.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.junianto.posedc.database.model.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Int): Transaction

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET status = 1 WHERE id = :transactionId")
    suspend fun updateTransactionStatus(transactionId: Int)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransactionById(transactionId: Int)

    @Query("SELECT COUNT(*) FROM transactions WHERE id = :traceId")
    suspend fun checkIfTransactionExists(traceId: Int): Int
}

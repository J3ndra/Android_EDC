package com.junianto.posedc.database.repository

import com.junianto.posedc.database.dao.TransactionDao
import com.junianto.posedc.database.model.Transaction
import javax.inject.Inject

class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {
    suspend fun getAllTransactions(): List<Transaction> {
        return transactionDao.getAllTransactions()
    }

    suspend fun getTransactionById(transactionId: Int): Transaction {
        return transactionDao.getTransactionById(transactionId)
    }

    suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun updateTransactionStatus(transactionId: Int) {
        transactionDao.updateTransactionStatus(transactionId)
    }

    suspend fun deleteTransactionById(transactionId: Int) {
        transactionDao.deleteTransactionById(transactionId)
    }

    suspend fun checkIfTransactionExists(traceId: Int): Int {
        return transactionDao.checkIfTransactionExists(traceId)
    }
}

package com.junianto.posedc.menu.qris

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junianto.posedc.database.model.Transaction
import com.junianto.posedc.database.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrisViewModel @Inject constructor(private val transactionRepository: TransactionRepository): ViewModel() {

    private val _transactionDetail = MutableLiveData<Transaction?>()
    val transactionDetail: LiveData<Transaction?> = _transactionDetail

    fun getTransactionById(transactionId: Int) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(transactionId)
            _transactionDetail.value = transaction
        }
    }
    
    suspend fun updateTransactionStatus(transactionId: Int) {
        transactionRepository.updateTransactionStatus(transactionId)
    }

    suspend fun checkTransactionExists(traceId: Int): Boolean {
        val count = transactionRepository.checkIfTransactionExists(traceId)
        return count > 0
    }
}
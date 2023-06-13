package com.junianto.posedc.menu.abort.viewmodel

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
class AbortViewModel @Inject constructor(private val transactionRepository: TransactionRepository) : ViewModel() {
    private val _transactionDetail = MutableLiveData<Transaction?>()
    val transactionDetail: LiveData<Transaction?> = _transactionDetail

    fun getTransactionById(transactionId: Int) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(transactionId)
            _transactionDetail.value = transaction
        }
    }

    fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch {
            transactionRepository.deleteTransactionById(transactionId)
        }
    }
}
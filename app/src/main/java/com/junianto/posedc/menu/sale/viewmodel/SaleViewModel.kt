package com.junianto.posedc.menu.sale.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
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
class SaleViewModel @Inject constructor(private val transactionRepository: TransactionRepository) : ViewModel() {
    private val _insertedTransactionId = MutableLiveData<Long>()
    val insertedTransactionId: LiveData<Long> = _insertedTransactionId

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransactionAndNavigate(transaction: Transaction, onTransactionInserted: (Long) -> Unit) {
        viewModelScope.launch {
            val insertedId = transactionRepository.insertTransaction(transaction)
            onTransactionInserted(insertedId)
        }
    }
}
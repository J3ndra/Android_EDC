package com.junianto.posedc.menu.sale.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.junianto.posedc.database.model.Transaction
import com.junianto.posedc.database.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SaleViewModel @Inject constructor(private val transactionRepository: TransactionRepository) : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveTransactionAndNavigate(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }
}
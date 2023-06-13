package com.junianto.posedc.menu.reprint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.junianto.posedc.database.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReprintViewModel @Inject constructor(private val transactionRepository: TransactionRepository) : ViewModel() {
    val getAllTransaction = liveData {
        emit(transactionRepository.getAllTransactions())
    }
}
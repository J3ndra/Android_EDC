package com.junianto.posedc.menu.settlements.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.junianto.posedc.database.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettlementsViewModel @Inject constructor(private val transactionRepository: TransactionRepository) : ViewModel() {
    val allTransactions = liveData {
        emit(transactionRepository.getAllTransactions())
    }
}
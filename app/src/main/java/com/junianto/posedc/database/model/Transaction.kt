package com.junianto.posedc.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val price: Int,
    val transactionDate: String,
    val cardId: String,
)


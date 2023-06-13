package com.junianto.posedc.menu.reprint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.junianto.posedc.R
import com.junianto.posedc.database.model.Transaction
import com.junianto.posedc.menu.reprint.viewmodel.ReprintViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReprintActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReprintAdapter

    private val viewModel: ReprintViewModel by viewModels()
    private var transactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reprint)

        recyclerView = findViewById(R.id.rv_reprint)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReprintAdapter()
        recyclerView.adapter = adapter

        viewModel.getAllTransaction.observe(this) { transactionList ->
            transactions = transactionList
            adapter.setTransactions(transactions)
        }
    }
}
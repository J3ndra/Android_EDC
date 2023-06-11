package com.junianto.posedc.menu.settlements

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.junianto.posedc.R
import com.junianto.posedc.menu.settlements.viewmodel.SettlementsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettlementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val viewModel: SettlementsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlements)

        recyclerView = findViewById(R.id.transaction_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter()
        recyclerView.adapter = adapter

        viewModel.allTransactions.observe(this) { transactions ->
            adapter.setTransactions(transactions)
        }
    }
}
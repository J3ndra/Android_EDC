package com.junianto.posedc.menu.settlements

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.database.model.Transaction
import com.junianto.posedc.menu.settlements.viewmodel.SettlementsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SettlementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionAdapter
    private val viewModel: SettlementsViewModel by viewModels()

    private var transactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlements)

        recyclerView = findViewById(R.id.transaction_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TransactionAdapter()
        recyclerView.adapter = adapter

        viewModel.allTransactions.observe(this) { transactionList ->
            transactions = transactionList // Update the transactions variable
            adapter.setTransactions(transactions)

            val totalAmount = calculateTotalAmount(transactions)
            updateTotalAmount(totalAmount)
        }

        val filterSpinner = findViewById<Spinner>(R.id.filter_spinner)
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFilter = parent?.getItemAtPosition(position).toString()
                val filterDays = when (selectedFilter) {
                    "1 Day" -> 1
                    "7 Days" -> 7
                    "30 Days" -> 30
                    else -> 30 // Default to 30 Days if no match is found
                }
                val filteredTransactions = filterTransactions(transactions, filterDays)
                adapter.setTransactions(filteredTransactions)

                val totalAmount = calculateTotalAmount(filteredTransactions)
                updateTotalAmount(totalAmount)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }


        val btnSettlements = findViewById<TextView>(R.id.btn_settlements)
        btnSettlements.setOnClickListener {
            Toast.makeText(this, "Settlements printed!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun filterTransactions(transactions: List<Transaction>, days: Int): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days) // Subtract the specified number of days from the current date
        val startDate = calendar.time // Get the start date as a Date object

        return transactions.filter { transaction ->
            val transactionDate = convertStringToDate(transaction.transactionDate)
            transactionDate >= startDate
        }
    }

    private fun convertStringToDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }

    private fun calculateTotalAmount(transactions: List<Transaction>): Double {
        var totalAmount = 0.0
        for (transaction in transactions) {
            totalAmount += transaction.price
        }
        return totalAmount
    }

    private fun updateTotalAmount(totalAmount: Double) {
        val totalAmountTextView = findViewById<TextView>(R.id.tv_total)
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        currencyFormat.maximumFractionDigits = 0
        val formattedTotalAmount = currencyFormat.format(totalAmount)
        totalAmountTextView.text = formattedTotalAmount
    }

}
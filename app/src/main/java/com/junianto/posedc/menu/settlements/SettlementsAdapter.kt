package com.junianto.posedc.menu.settlements

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.junianto.posedc.R
import com.junianto.posedc.database.model.Transaction
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

class SettlementsAdapter : RecyclerView.Adapter<SettlementsAdapter.TransactionViewHolder>() {
    private var transactions: List<Transaction> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTransactions(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transactionIdTextView: TextView = itemView.findViewById(R.id.transactionIdTextView)
        private val priceTextView: TextView = itemView.findViewById(R.id.priceTextView)
        private val transactionDateTextView: TextView = itemView.findViewById(R.id.transactionDateTextView)

        @SuppressLint("SetTextI18n")
        fun bind(transaction: Transaction) {
            transactionIdTextView.text = "TRACE NO : ${transaction.id}"

            val formattedAmount = NumberFormat.getNumberInstance(Locale("en", "ID")).format(transaction.price)
            val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
            val formattedAmountWithDots = formattedAmount.replace(decimalSeparator.toString(), ".")
            priceTextView.text = "Rp. $formattedAmountWithDots"

            transactionDateTextView.text = transaction.transactionDate
        }
    }
}
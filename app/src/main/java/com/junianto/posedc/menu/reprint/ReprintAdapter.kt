package com.junianto.posedc.menu.reprint

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.junianto.posedc.R
import com.junianto.posedc.database.model.Transaction
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

interface ReprintButtonClickListener {
    fun onReprintButtonClick(transaction: Transaction)
}

class ReprintAdapter : RecyclerView.Adapter<ReprintAdapter.ReprintViewHolder>() {
    private var transactions: List<Transaction> = emptyList()
    private var reprintButtonClickListener: ReprintButtonClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReprintViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_reprint, parent, false)
        return ReprintViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return transactions.size
    }

    override fun onBindViewHolder(holder: ReprintViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setTransactions(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }

    inner class ReprintViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val transactionIdTextView: TextView = itemView.findViewById(R.id.tv_id)
        private val priceTextView: TextView = itemView.findViewById(R.id.tv_amount)
        private val transactionDateTextView: TextView = itemView.findViewById(R.id.tv_date)
        private val reprintButton: TextView = itemView.findViewById(R.id.btn_reprint)

        @SuppressLint("SetTextI18n")
        fun bind(transaction: Transaction) {
            transactionIdTextView.text = "No. ${transaction.id}"

            val formattedAmount = NumberFormat.getNumberInstance(Locale("en", "ID")).format(transaction.price)
            val decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator
            val formattedAmountWithDots = formattedAmount.replace(decimalSeparator.toString(), ".")
            priceTextView.text = "Rp. $formattedAmountWithDots"

            transactionDateTextView.text = transaction.transactionDate

            reprintButton.setOnClickListener {
//                Toast.makeText(itemView.context, "Reprint transaction with id ${transaction.id}", Toast.LENGTH_SHORT).show()
                reprintButtonClickListener?.onReprintButtonClick(transaction)
            }
        }
    }
    fun reprintButtonClickListener(reprintButtonClickListener: ReprintButtonClickListener) {
        this.reprintButtonClickListener = reprintButtonClickListener
    }
}
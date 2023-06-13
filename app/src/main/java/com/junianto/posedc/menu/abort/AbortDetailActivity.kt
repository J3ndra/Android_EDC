package com.junianto.posedc.menu.abort

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import com.junianto.posedc.R
import com.junianto.posedc.menu.abort.viewmodel.AbortViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class AbortDetailActivity : AppCompatActivity() {

    private val viewModel: AbortViewModel by viewModels()
    private lateinit var dialog: AlertDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abort_detail)

        val transactionId = intent.getIntExtra("transactionId", -1)
        val transactionAmount = intent.getIntExtra("transactionAmount", 0)
        val transactionDate = intent.getStringExtra("transactionDate")

        val tvTraceId = findViewById<TextView>(R.id.tv_trace_id)
        val traceIdText = getString(R.string.trace_id_placeholder, transactionId)
        tvTraceId.text = traceIdText

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = transactionDate?.let { dateFormat.parse(it) }

        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formattedDate = date?.let { dateFormatter.format(it) }
        val formattedTime = date?.let { timeFormatter.format(it) }

        val dateTextView = findViewById<TextView>(R.id.tv_date)
        val dateText = getString(R.string.date_placeholder, formattedDate)
        dateTextView.text = dateText

        val timeTextView = findViewById<TextView>(R.id.tv_time)
        val timeText = getString(R.string.time_placeholder, formattedTime)
        timeTextView.text = timeText

        val amountTextView = findViewById<TextView>(R.id.tv_amount)
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        numberFormat.maximumFractionDigits = 0
        val formattedAmount = numberFormat.format(transactionAmount)
        val formattedText = formattedAmount.replace("Rp", "-Rp.")
        amountTextView.text = formattedText

        val btnVoid = findViewById<Button>(R.id.btn_void)

        btnVoid.setOnClickListener {
            showConfirmationDialog(transactionId)
        }
    }

    private fun showConfirmationDialog(transactionId: Int) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Are you sure to void this transaction? This action cannot be undone.")
        dialogBuilder.setPositiveButton("Yes") { dialog: DialogInterface, _: Int ->
            viewModel.deleteTransaction(transactionId)
            dialog.dismiss()
            Toast.makeText(this, "Transaction deleted!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, VoidSuccessfulActivity::class.java)
            intent.putExtra("transactionId", transactionId)
            startActivity(intent)
            finish()
            // Handle void action here
        }
        dialogBuilder.setNegativeButton("No") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        dialog = dialogBuilder.create()
        dialog.show()
    }
}
package com.junianto.posedc.menu.abort

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.menu.abort.viewmodel.AbortViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AbortActivity : AppCompatActivity() {

    private val viewModel: AbortViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abort)

        val etTraceId = findViewById<EditText>(R.id.et_trace_id)

        // Button
        val btn1 = findViewById<Button>(R.id.btn_1)
        btn1.setOnClickListener {
            fillEditText("1")
        }
        val btn2 = findViewById<Button>(R.id.btn_2)
        btn2.setOnClickListener {
            fillEditText("2")
        }
        val btn3 = findViewById<Button>(R.id.btn_3)
        btn3.setOnClickListener {
            fillEditText("3")
        }
        val btn4 = findViewById<Button>(R.id.btn_4)
        btn4.setOnClickListener {
            fillEditText("4")
        }
        val btn5 = findViewById<Button>(R.id.btn_5)
        btn5.setOnClickListener {
            fillEditText("5")
        }
        val btn6 = findViewById<Button>(R.id.btn_6)
        btn6.setOnClickListener {
            fillEditText("6")
        }
        val btn7 = findViewById<Button>(R.id.btn_7)
        btn7.setOnClickListener {
            fillEditText("7")
        }
        val btn8 = findViewById<Button>(R.id.btn_8)
        btn8.setOnClickListener {
            fillEditText("8")
        }
        val btn9 = findViewById<Button>(R.id.btn_9)
        btn9.setOnClickListener {
            fillEditText("9")
        }
        val btn0 = findViewById<Button>(R.id.btn_0)
        btn0.setOnClickListener {
            fillEditText("0")
        }
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnClear = findViewById<Button>(R.id.btn_clear)
        val btnOk = findViewById<Button>(R.id.btn_ok)

        btnStop.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnClear.setOnClickListener {
            clearEditText()
        }

        btnOk.setOnClickListener {
            val transactionId = etTraceId.text.toString().toInt()
            viewModel.getTransactionById(transactionId)
            Timber.log(1, "Transaction ID: ${etTraceId.text}")
        }

        viewModel.transactionDetail.observe(this, Observer { transaction ->
            if (transaction != null) {
                Timber.d("Transaction: $transaction")
                val intent = Intent(this, AbortDetailActivity::class.java)
                intent.putExtra("transactionId", transaction.id)
                intent.putExtra("transactionAmount", transaction.price)
                intent.putExtra("transactionDate", transaction.transactionDate)
                intent.putExtra("transactionCardId", transaction.cardId)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fillEditText(digit: String) {
        val etTraceId = findViewById<EditText>(R.id.et_trace_id)
        val currentText = etTraceId.text.toString()
        val newText = StringBuilder(currentText).append(digit).toString()
        etTraceId.setText(newText)
    }

    private fun clearEditText() {
        val etTraceId = findViewById<EditText>(R.id.et_trace_id)
        val currentText = etTraceId.text.toString()
        if (currentText.isNotEmpty()) {
            val newText = currentText.substring(0, currentText.length - 1)
            etTraceId.setText(newText)
        }
    }

}
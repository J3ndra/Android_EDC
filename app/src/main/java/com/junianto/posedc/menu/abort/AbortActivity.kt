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

        val btnAbort = findViewById<Button>(R.id.btn_abort)

        btnAbort.setOnClickListener {
            val transactionId = etTraceId.text.toString().toInt()
            viewModel.getTransactionById(transactionId)
            Timber.log(1, "Transaction ID: ${etTraceId.text}")
        }

        viewModel.transactionDetail.observe(this, Observer { transaction ->
            if (transaction != null) {
                val intent = Intent(this, AbortDetailActivity::class.java)
                intent.putExtra("transactionId", transaction.id)
                intent.putExtra("transactionAmount", transaction.price)
                intent.putExtra("transactionDate", transaction.transactionDate)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
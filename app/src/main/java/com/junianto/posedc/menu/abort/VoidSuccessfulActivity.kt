package com.junianto.posedc.menu.abort

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoidSuccessfulActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_void_successful)

        val transactionId = intent.getIntExtra("transactionId", -1)

        val tv7 = findViewById<TextView>(R.id.textView7)
        val voidSuccessfulMessage = getString(R.string.void_successful_message, transactionId)
        tv7.text = voidSuccessfulMessage

        val btnReprintReceipt = findViewById<Button>(R.id.btn_re_print_receipt)
        btnReprintReceipt.setOnClickListener {
            Toast.makeText(this, "Reprint Receipt successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
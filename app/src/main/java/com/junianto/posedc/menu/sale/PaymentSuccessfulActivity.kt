package com.junianto.posedc.menu.sale

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.junianto.posedc.R
import com.junianto.posedc.menu.MenuActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class PaymentSuccessfulActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_successful)

        val totalAmount = intent.getIntExtra("totalAmount", 0)

        val successMessageTextView = findViewById<TextView>(R.id.textView6)
        val successDescriptionTextView = findViewById<TextView>(R.id.textView7)
        val btnRePrintReceipt = findViewById<Button>(R.id.btn_re_print_receipt)

        successMessageTextView.text = getString(R.string.payment_successful)
        val formattedAmount = NumberFormat.getNumberInstance(Locale("en", "ID")).format(totalAmount)
        successDescriptionTextView.text = "Hooray! Your payment of Rp. $formattedAmount is successful"

        btnRePrintReceipt.setOnClickListener {
            // TODO: Implement the desired action when the button is clicked
        }

        btnRePrintReceipt.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
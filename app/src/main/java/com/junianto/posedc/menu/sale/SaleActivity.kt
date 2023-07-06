package com.junianto.posedc.menu.sale

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.database.AppDatabase
import com.junianto.posedc.database.model.Transaction
import com.junianto.posedc.database.repository.TransactionRepository
import com.junianto.posedc.menu.sale.viewmodel.SaleViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class SaleActivity : AppCompatActivity() {

    private lateinit var amountEt: TextView
    private val viewModel: SaleViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale)

        val cardId = intent.getStringExtra("tagId")

        amountEt = findViewById(R.id.amount_et)

        val btn1 = findViewById<Button>(R.id.btn_1)
        val btn2 = findViewById<Button>(R.id.btn_2)
        val btn3 = findViewById<Button>(R.id.btn_3)
        val btn4 = findViewById<Button>(R.id.btn_4)
        val btn5 = findViewById<Button>(R.id.btn_5)
        val btn6 = findViewById<Button>(R.id.btn_6)
        val btn7 = findViewById<Button>(R.id.btn_7)
        val btn8 = findViewById<Button>(R.id.btn_8)
        val btn9 = findViewById<Button>(R.id.btn_9)
        val btn0 = findViewById<Button>(R.id.btn_0)
        val btn000 = findViewById<Button>(R.id.btn_000)
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnClear = findViewById<Button>(R.id.btn_clear)
        val btnOk = findViewById<Button>(R.id.btn_ok)

        btn1.setOnClickListener {
            updateAmount("1")
        }

        btn2.setOnClickListener {
            updateAmount("2")
        }

        btn3.setOnClickListener {
            updateAmount("3")
        }

        btn4.setOnClickListener {
            updateAmount("4")
        }

        btn5.setOnClickListener {
            updateAmount("5")
        }

        btn6.setOnClickListener {
            updateAmount("6")
        }

        btn7.setOnClickListener {
            updateAmount("7")
        }

        btn8.setOnClickListener {
            updateAmount("8")
        }

        btn9.setOnClickListener {
            updateAmount("9")
        }

        btn0.setOnClickListener {
            updateAmount("0")
        }

        btn000.setOnClickListener {
            updateAmount("000")
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnClear.setOnClickListener {
            clearAmount()
        }

        btnOk.setOnClickListener {
            val amountString = amountEt.text.toString().substring(4).replace(".", "")
            val amount = amountString.toInt()
            Timber.i("amount_total: $amount")

            val intent = Intent(this, SaleEnterPinActivity::class.java)
            intent.putExtra("amount", amount)
            intent.putExtra("tagId", cardId)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAmount(digit: String) {
        val currentAmount = amountEt.text.toString().substring(4).replace(".", "").replace(",", "")
        val newAmount = if (currentAmount == "0") digit else currentAmount + digit

        val formatter = DecimalFormat("#,###")
        val newAmountFormatted = formatter.format(newAmount.toInt()).replace(",", ".")
        amountEt.text = "Rp. $newAmountFormatted"
    }

    @SuppressLint("SetTextI18n")
    private fun clearAmount() {
        val currentAmount = amountEt.text.toString().substring(4).replace(".", "").replace(",", "")
        if (currentAmount.isNotEmpty()) {
            val newAmount = currentAmount.substring(0, currentAmount.length - 1)
            if (newAmount.isNotEmpty()) {
                val formattedAmount = formatAmount(newAmount)
                amountEt.text = formattedAmount
            } else {
                amountEt.text = "Rp. 0"
            }
        } else {
            amountEt.text = "Rp. 0"
        }
    }


    private fun formatAmount(amount: String): String {
        val amountWithoutSeparator = amount.replace(".", "").replace(",", "")
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        val decimalFormat = DecimalFormat("#,###", decimalFormatSymbols)
        val formattedAmount = decimalFormat.format(amountWithoutSeparator.toLong())
        return "Rp. $formattedAmount"
    }


//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun getCurrentDateTime(): String {
//        val currentDateTime = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//        return currentDateTime.format(formatter)
//    }
}

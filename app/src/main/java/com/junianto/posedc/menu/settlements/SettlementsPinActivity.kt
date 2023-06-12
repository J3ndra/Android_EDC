package com.junianto.posedc.menu.settlements

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class SettlementsPinActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_enter_pin)

        // Intent
        val totalAmount = intent.getIntExtra("totalAmount", 0)

        // EditText
        val etPin1 = findViewById<TextView>(R.id.et_pin_1)
        etPin1.requestFocus()
        val etPin2 = findViewById<TextView>(R.id.et_pin_2)
        val etPin3 = findViewById<TextView>(R.id.et_pin_3)
        val etPin4 = findViewById<TextView>(R.id.et_pin_4)
        val etPin5 = findViewById<TextView>(R.id.et_pin_5)
        val etPin6 = findViewById<TextView>(R.id.et_pin_6)

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



        etPin1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    etPin2.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        etPin2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    etPin3.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        etPin3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    etPin4.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        etPin4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    etPin5.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        etPin5.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length == 1) {
                    etPin6.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

        etPin6.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No implementation needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No implementation needed
            }

            override fun afterTextChanged(s: Editable?) {
                // No implementation needed
            }
        })

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
            if (checkPassword()) {
                val intent = Intent(this, SettlementsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Wrong PIN", Toast.LENGTH_SHORT).show()
                clearEditText()
            }
        }
    }

    private fun checkPassword(): Boolean {
        // Password
        val password = "123456"

        // EditText
        val et1 = findViewById<TextView>(R.id.et_pin_1)
        val et2 = findViewById<TextView>(R.id.et_pin_2)
        val et3 = findViewById<TextView>(R.id.et_pin_3)
        val et4 = findViewById<TextView>(R.id.et_pin_4)
        val et5 = findViewById<TextView>(R.id.et_pin_5)
        val et6 = findViewById<TextView>(R.id.et_pin_6)

        // Get value from EditText
        val value = et1.text.toString() + et2.text.toString() + et3.text.toString() + et4.text.toString() + et5.text.toString() + et6.text.toString()

        return value == password
    }

    private fun clearEditText() {
        val editTextList = listOf(
            findViewById<TextView>(R.id.et_pin_1),
            findViewById<TextView>(R.id.et_pin_2),
            findViewById<TextView>(R.id.et_pin_3),
            findViewById<TextView>(R.id.et_pin_4),
            findViewById<TextView>(R.id.et_pin_5),
            findViewById<TextView>(R.id.et_pin_6)
        )

        for (editText in editTextList) {
            editText.text = ""
            editText.setBackgroundResource(R.drawable.et_pin)
            editText.setTextColor(Color.BLACK)
        }
    }


    private fun fillEditText(digit: String) {
        val editTextList = listOf(
            findViewById<TextView>(R.id.et_pin_1),
            findViewById<TextView>(R.id.et_pin_2),
            findViewById<TextView>(R.id.et_pin_3),
            findViewById<TextView>(R.id.et_pin_4),
            findViewById<TextView>(R.id.et_pin_5),
            findViewById<TextView>(R.id.et_pin_6)
        )

        for (i in editTextList.indices) {
            val editText = editTextList[i]
            if (editText.text.isEmpty()) {
                editText.text = digit
                editText.background = ContextCompat.getDrawable(this, R.drawable.et_pin_filled)
                editText.setTextColor(Color.TRANSPARENT) // Set text color to transparent
                if (i < editTextList.size - 1) {
                    editTextList[i + 1].requestFocus()
                }
                break
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return currentDateTime.format(formatter)
    }
}
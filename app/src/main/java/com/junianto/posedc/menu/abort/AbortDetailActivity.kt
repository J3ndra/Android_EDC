package com.junianto.posedc.menu.abort

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.junianto.posedc.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AbortDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abort_detail)

        val transactionId = intent.getIntExtra("transactionId", -1)
        val transactionAmount = intent.getIntExtra("transactionAmount", 0)
        val transactionDate = intent.getStringExtra("transactionDate")

        Timber.d("transactionId: $transactionId")
        Timber.d("transactionAmount: $transactionAmount")
        Timber.d("transactionDate: $transactionDate")
    }
}
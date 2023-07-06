package com.junianto.posedc.menu.sale

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.util.CircleProgressView
import com.junianto.posedc.util.NfcTagReader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SaleTapCardActivity : AppCompatActivity() {

    @Inject
    lateinit var nfcTagReader: NfcTagReader

    private lateinit var circleProgressView: CircleProgressView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_tap_card)

        circleProgressView = findViewById(R.id.circle_progress_view)
        circleProgressView.setListener(object : CircleProgressView.Listener {
            override fun onTimerFinished(success: Boolean) {
                val tagId = circleProgressView.getTagId()

                if (success) {
                    Toast.makeText(this@SaleTapCardActivity, "Success", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SaleTapCardActivity, SaleActivity::class.java)
                    intent.putExtra("tagId", tagId)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@SaleTapCardActivity, "Failed", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@SaleTapCardActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        })
        circleProgressView.startCountdown(this)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onResume() {
        super.onResume()
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.enableForegroundDispatch(
            this,
            PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0),
            null,
            null
        )
    }

    override fun onPause() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter?.disableForegroundDispatch(this)
        super.onPause()
    }

    override fun onNewIntent(intent: Intent) {
        circleProgressView.handleNfcIntent(intent)
        super.onNewIntent(intent)
    }

    override fun onBackPressed() {
        circleProgressView.stopTimer()
        super.onBackPressed()
    }
}
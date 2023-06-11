package com.junianto.posedc.util

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.tech.Ndef
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.menu.sale.SaleActivity

class TapCardActivity : AppCompatActivity(), CircleProgressView.Listener {

    private lateinit var circleProgressView: CircleProgressView
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tap_card)

        circleProgressView = findViewById(R.id.circle_progress_view)
        circleProgressView.setListener(this)
        circleProgressView.startCountdown(this)

        // Get NFC adapter
        val nfcManager = getSystemService(NFC_SERVICE) as? NfcManager
        nfcAdapter = nfcManager?.defaultAdapter
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val intentFiltersArray = arrayOf(IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED))
        val techListsArray = arrayOf(arrayOf(Ndef::class.java.name))

        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        circleProgressView.handleNfcIntent(intent)
    }

    override fun onTimerFinished(success: Boolean) {
        if (success) {
            // NFC tag tapped successfully
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // NFC tag not tapped within the time limit
            // Add your desired action here
            finish()
        }
    }
}

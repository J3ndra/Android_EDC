package com.junianto.posedc.util

import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import java.nio.charset.Charset
import javax.inject.Inject

class NfcTagReaderImpl @Inject constructor() : NfcTagReader {
    override fun readNfcTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        ndef?.connect()

        val ndefMessage = ndef?.ndefMessage
        ndefMessage?.let {
            val records = it.records
            for (record in records) {
                val payload = String(record.payload, Charset.forName("UTF-8"))
                // Handle the payload data as needed
                Log.d("NFC", "Payload: $payload")
            }
        }

        ndef?.close()
    }

}

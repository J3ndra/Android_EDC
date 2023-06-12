package com.junianto.posedc.util

import android.nfc.Tag

interface NfcTagReader {
    fun readNfcTag(tag: Tag)
}
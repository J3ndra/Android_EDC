package com.junianto.posedc.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.appcompat.app.AlertDialog

fun Context.isPermissionGranted(permission: String): Boolean {
    return android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M ||
            checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
}

inline fun Context.cameraPermissionRequest(crossinline positive: () -> Unit) {
    AlertDialog.Builder(this)
        .setTitle("Permission Required")
        .setMessage("Camera permission is required to use this feature")
        .setPositiveButton("OK") { _, _ ->
            positive.invoke()
        }
        .setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        .create()
        .show()
}

fun Context.openPermissionSetting() {
    Intent(ACTION_APPLICATION_DETAILS_SETTINGS).also {
        val uri: Uri = Uri.fromParts("package", packageName, null)
        it.data = uri
        startActivity(it)
    }
}
package com.junianto.posedc.util

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BluetoothPermissionHelper(private val activity: Activity) {
    companion object {
        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 123
    }

    fun requestBluetoothPermission() {
        if (isBluetoothPermissionGranted()) {
            // Bluetooth permission is already granted
            // Implement your Bluetooth-related logic here
        } else {
            // Request Bluetooth permission
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH),
                BLUETOOTH_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun isBluetoothPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun handleBluetoothPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permission granted
                    // Implement your Bluetooth-related logic here
                } else {
                    // Bluetooth permission denied
                    // Handle the case where the permission was denied
                }
            }
        }
    }
}


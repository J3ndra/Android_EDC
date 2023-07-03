package com.junianto.posedc.util

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.*

object BluetoothUtil {
    private const val TAG = "BluetoothUtil"
    private val IPOSPRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private const val IPosPrinter_Address = "00:AA:11:BB:22:CC"

    fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter
    }

    fun getIposPrinterDevice(
        context: Context,
        mBluetoothAdapter: BluetoothAdapter?
    ): BluetoothDevice? {
        var IPosPrinter_device: BluetoothDevice? = null
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val devices: Set<BluetoothDevice>? = mBluetoothAdapter?.bondedDevices
            devices?.forEach { device ->
                if (device.address == IPosPrinter_Address) {
                    IPosPrinter_device = device
                    return@forEach
                }
            }
        } else {
            // Request the missing permission if not granted
            val activity = context as? Activity
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.BLUETOOTH),
                    0
                )
            }
        }
        return IPosPrinter_device
    }

    @Throws(IOException::class)
    fun getSocket(context: Context, mDevice: BluetoothDevice): BluetoothSocket {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val socket: BluetoothSocket =
                mDevice.createRfcommSocketToServiceRecord(IPOSPRINTER_UUID)
            socket.connect()
            return socket
        } else {
            // Request the missing permission if not granted
            val activity = context as? Activity
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.BLUETOOTH),
                    0
                )
            }
            throw IOException("Bluetooth permission not granted")
        }
    }
}

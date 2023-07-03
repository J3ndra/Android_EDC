package com.junianto.posedc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.junianto.posedc.menu.MenuActivity
import com.junianto.posedc.service.PrinterServiceConnection
import com.junianto.posedc.util.BluetoothPermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var bluetoothPermissionHelper: BluetoothPermissionHelper

    private lateinit var printerServiceConnection: PrinterServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGoToMenu: Button = findViewById(R.id.btn_go_to_menu)
        btnGoToMenu.setOnClickListener {
            val intent = Intent(this@MainActivity, MenuActivity::class.java)
            startActivity(intent)
        }

        // Request Bluetooth permission
        bluetoothPermissionHelper.requestBluetoothPermission()

        // Create the PrinterServiceConnection instance
        printerServiceConnection = PrinterServiceConnection(this)
    }

    override fun onStart() {
        super.onStart()
        // Connect to the printer service
        printerServiceConnection.connectService()

        // Check if the printer service is connected
        if (printerServiceConnection.isServiceConnected()) {
            Timber.d("Printer service is connected")
            printerServiceConnection.getPrinterService()?.let { printerService ->
                // Initialize the printer
                printerService.setPrinterPrintFontSize(24, null)
                printerService.setPrinterPrintAlignment(1, null)
                printerService.printText("Hello World", null)

                // Print
                printerService.printerInit(null)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Disconnect from the printer service
        printerServiceConnection.disconnectService()
    }
}
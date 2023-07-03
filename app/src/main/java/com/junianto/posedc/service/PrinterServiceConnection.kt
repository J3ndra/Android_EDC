package com.junianto.posedc.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.iposprinter.iposprinterservice.IPosPrinterService

class PrinterServiceConnection(private val context: Context): ServiceConnection {

    private var printerService: IPosPrinterService? = null
    private var isServiceConnected = false

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        printerService = IPosPrinterService.Stub.asInterface(service)
        isServiceConnected = true
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        printerService = null
        isServiceConnected = false
    }

    fun getPrinterService(): IPosPrinterService? {
        return printerService
    }

    fun isServiceConnected(): Boolean {
        return isServiceConnected
    }

    fun connectService() {
        if (printerService == null) {
            val intent = ComponentName("com.iposprinter.iposprinterservice", "com.iposprinter.iposprinterservice.IPosPrinterService")
            val bindIntent = Intent()
            bindIntent.component = intent
            context.bindService(bindIntent, this, Context.BIND_AUTO_CREATE)
        }
    }

    fun disconnectService() {
        if (printerService != null) {
            context.unbindService(this)
        }
    }
}
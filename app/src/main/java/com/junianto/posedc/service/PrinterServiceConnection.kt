package com.junianto.posedc.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.iposprinter.iposprinterservice.IPosPrinterCallback
import com.iposprinter.iposprinterservice.IPosPrinterService

class PrinterServiceConnection(private val context: Context) : ServiceConnection {
    private var printerService: IPosPrinterService? = null

    fun getPrinterService(): IPosPrinterService? {
        return printerService
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        printerService = IPosPrinterService.Stub.asInterface(service)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        printerService = null
    }

    fun bindService() {
        val intent = Intent("com.iposprinter.iposprinterservice.IPosPrinterService")
        intent.setPackage("com.iposprinter.iposprinterservice") // Replace with the package name of your printer service
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    fun unbindService() {
        context.unbindService(this)
    }
}
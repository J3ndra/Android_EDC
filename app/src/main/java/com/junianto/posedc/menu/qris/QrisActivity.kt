package com.junianto.posedc.menu.qris

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.RemoteException
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.iposprinter.iposprinterservice.IPosPrinterCallback
import com.iposprinter.iposprinterservice.IPosPrinterService
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.databinding.ActivityQrisBinding
import com.junianto.posedc.menu.sale.PaymentSuccessfulActivity
import com.junianto.posedc.util.HandlerUtils
import com.junianto.posedc.util.ThreadPoolManager
import com.junianto.posedc.util.cameraPermissionRequest
import com.junianto.posedc.util.isPermissionGranted
import com.junianto.posedc.util.openPermissionSetting
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.concurrent.Executors

@AndroidEntryPoint
class QrisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrisBinding

    private val TAG = "IPosPrinterTestDemo"

    /* Demo 版本号*/
    private val VERSION = "V1.1.0"

    private val PRINTER_NORMAL = 0
    private val PRINTER_PAPERLESS = 1
    private val PRINTER_THP_HIGH_TEMPERATURE = 2
    private val PRINTER_MOTOR_HIGH_TEMPERATURE = 3
    private val PRINTER_IS_BUSY = 4
    private val PRINTER_ERROR_UNKNOWN = 5

    /*打印机当前状态*/
    private var printerStatus = 0

    /*定义状态广播*/
    private val PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.NORMAL_ACTION"
    private val PRINTER_PAPERLESS_ACTION = "com.iposprinter.iposprinterservice.PAPERLESS_ACTION"
    private val PRINTER_PAPEREXISTS_ACTION = "com.iposprinter.iposprinterservice.PAPEREXISTS_ACTION"
    private val PRINTER_THP_HIGHTEMP_ACTION =
        "com.iposprinter.iposprinterservice.THP_HIGHTEMP_ACTION"
    private val PRINTER_THP_NORMALTEMP_ACTION =
        "com.iposprinter.iposprinterservice.THP_NORMALTEMP_ACTION"
    private val PRINTER_MOTOR_HIGHTEMP_ACTION =
        "com.iposprinter.iposprinterservice.MOTOR_HIGHTEMP_ACTION"
    private val PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.BUSY_ACTION"
    private val PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION =
        "com.iposprinter.iposprinterservice.CURRENT_TASK_PRINT_COMPLETE_ACTION"

    /*定义消息*/
    private val MSG_TEST = 1
    private val MSG_IS_NORMAL = 2
    private val MSG_IS_BUSY = 3
    private val MSG_PAPER_LESS = 4
    private val MSG_PAPER_EXISTS = 5
    private val MSG_THP_HIGH_TEMP = 6
    private val MSG_THP_TEMP_NORMAL = 7
    private val MSG_MOTOR_HIGH_TEMP = 8
    private val MSG_MOTOR_HIGH_TEMP_INIT_PRINTER = 9
    private val MSG_CURRENT_TASK_PRINT_COMPLETE = 10

    /*循环打印类型*/
    private val MULTI_THREAD_LOOP_PRINT = 1
    private val INPUT_CONTENT_LOOP_PRINT = 2
    private val DEMO_LOOP_PRINT = 3
    private val PRINT_DRIVER_ERROR_TEST = 4
    private val DEFAULT_LOOP_PRINT = 0

    //循环打印标志位
    private var loopPrintFlag = DEFAULT_LOOP_PRINT
    private val loopContent: Byte = 0x00
    private var printDriverTestCount = 0

    private var mIPosPrinterService: IPosPrinterService? = null
    private var callback: IPosPrinterCallback? = null

    private val random = Random()
    private var handler: HandlerUtils.MyHandler? = null

    private val iHandlerIntent: HandlerUtils.IHandlerIntent = object : HandlerUtils.IHandlerIntent {
        override fun handlerIntent(msg: Message) {
            when (msg.what) {
                MSG_TEST -> {
                    // Handle MSG_TEST
                }
                MSG_IS_NORMAL -> {
                    if (getPrinterStatus() == PRINTER_NORMAL) {
                        // loopPrint(loopPrintFlag)
                    }
                }
                MSG_IS_BUSY -> {
                    Toast.makeText(this@QrisActivity, R.string.printer_is_working, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_LESS -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@QrisActivity, R.string.out_of_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_EXISTS -> {
                    Toast.makeText(this@QrisActivity, R.string.exists_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_THP_HIGH_TEMP -> {
                    Toast.makeText(this@QrisActivity, R.string.printer_high_temp_alarm, Toast.LENGTH_SHORT).show()
                }
                MSG_MOTOR_HIGH_TEMP -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@QrisActivity, R.string.motor_high_temp_alarm, Toast.LENGTH_SHORT).show()
                    handler?.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000) //马达高温报警，等待3分钟后复位打印机
                }
                MSG_MOTOR_HIGH_TEMP_INIT_PRINTER -> {
                    printerInit()
                }
                MSG_CURRENT_TASK_PRINT_COMPLETE -> {
                    Toast.makeText(this@QrisActivity, R.string.printer_current_task_print_complete, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // Default case
                }
            }
        }
    }

    private val IPosPrinterStatusListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == null) {
                Timber.d("IPosPrinterStatusListener onReceive action = null")
                return
            }
            Timber.d("IPosPrinterStatusListener action = $action")
            when (action) {
                PRINTER_NORMAL_ACTION -> {
                    handler!!.sendEmptyMessageDelayed(MSG_IS_NORMAL, 0)
                }
                PRINTER_PAPERLESS_ACTION -> {
                    handler!!.sendEmptyMessageDelayed(MSG_PAPER_LESS, 0)
                }
                PRINTER_BUSY_ACTION -> {
                    handler!!.sendEmptyMessageDelayed(MSG_IS_BUSY, 0)
                }
                PRINTER_PAPEREXISTS_ACTION -> {
                    handler!!.sendEmptyMessageDelayed(MSG_PAPER_EXISTS, 0)
                }
                PRINTER_THP_HIGHTEMP_ACTION -> {
                    handler!!.sendEmptyMessageDelayed(MSG_THP_HIGH_TEMP, 0)
                }
                PRINTER_THP_NORMALTEMP_ACTION -> {
                    handler!!.sendEmptyMessageDelayed(MSG_THP_TEMP_NORMAL, 0)
                }
                PRINTER_MOTOR_HIGHTEMP_ACTION //此时当前任务会继续打印，完成当前任务后，请等待2分钟以上时间，继续下一个打印任务
                -> {
                    handler!!.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP, 0)
                }
                PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION -> {
                    handler!!.sendEmptyMessageDelayed(MSG_CURRENT_TASK_PRINT_COMPLETE, 0)
                }
                else -> {
                    handler!!.sendEmptyMessageDelayed(MSG_TEST, 0)
                }
            }
        }
    }

    private val connectService: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mIPosPrinterService = IPosPrinterService.Stub.asInterface(service)
            Timber.i("Service connected!")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mIPosPrinterService = null
        }
    }

    private val cameraPermission = android.Manifest.permission.CAMERA

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // START SCANNER
            bindInputAnalyser()
        } else {
            cameraPermissionRequest {
                openPermissionSetting()
            }
        }
    }

    private val viewModel: QrisViewModel by viewModels()

    private lateinit var cameraSelector: CameraSelector
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private var shouldProcessFrames = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler = HandlerUtils.MyHandler(iHandlerIntent)

        callback = object : IPosPrinterCallback.Stub() {
            @Throws(RemoteException::class)
            override fun onRunResult(isSuccess: Boolean) {
                Timber.i("result:$isSuccess\n")
            }

            @Throws(RemoteException::class)
            override fun onReturnString(value: String) {
                Timber.i("result:$value\n")
            }
        }

        //绑定服务

        //绑定服务
        val intent = Intent()
        intent.setPackage("com.iposprinter.iposprinterservice")
        intent.action = "com.iposprinter.iposprinterservice.IPosPrintService"
        //startService(intent);
        //startService(intent);
        bindService(intent, connectService, BIND_AUTO_CREATE)

        //注册打印机状态接收器

        //注册打印机状态接收器
        val printerStatusFilter = IntentFilter()
        printerStatusFilter.addAction(PRINTER_NORMAL_ACTION)
        printerStatusFilter.addAction(PRINTER_PAPERLESS_ACTION)
        printerStatusFilter.addAction(PRINTER_PAPEREXISTS_ACTION)
        printerStatusFilter.addAction(PRINTER_THP_HIGHTEMP_ACTION)
        printerStatusFilter.addAction(PRINTER_THP_NORMALTEMP_ACTION)
        printerStatusFilter.addAction(PRINTER_MOTOR_HIGHTEMP_ACTION)
        printerStatusFilter.addAction(PRINTER_BUSY_ACTION)

        registerReceiver(IPosPrinterStatusListener, printerStatusFilter)

        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            processCameraProvider = cameraProviderFuture.get()
            bindCameraPreview()
            requestCameraAndStartScanner()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun requestCameraAndStartScanner() {
        if (isPermissionGranted(cameraPermission)) {
            // START SCANNER
            bindInputAnalyser()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                shouldShowRequestPermissionRationale(cameraPermission) -> {
                    cameraPermissionRequest {
                        openPermissionSetting()
                    }
                }
                else -> {
                    requestCameraPermission.launch(cameraPermission)
                }
            }
        } else {
            requestCameraPermission.launch(cameraPermission)
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.cameraxPreview.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.cameraxPreview.surfaceProvider)
        processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
    }

    private fun bindInputAnalyser() {
        val barcodeScanner: BarcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )

        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.cameraxPreview.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            if (shouldProcessFrames) {
                processImageProxy(barcodeScanner, imageProxy)
            } else {
                imageProxy.close()
            }
        }

        processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(
        barcodeScanner: BarcodeScanner,
        imageProxy: ImageProxy
    ) {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(inputImage)
            .addOnSuccessListener {
                if (it.isNotEmpty()) {
                    shouldProcessFrames = false

                    lifecycleScope.launch {
                        val transactionExists = it[0].rawValue?.let { it1 ->
                            viewModel.checkTransactionExists(
                                it1.toInt()
                            )
                        }
                        if (transactionExists == true) {
                            AlertDialog.Builder(this@QrisActivity)
                                .setTitle("QRIS")
                                .setMessage("Pesanan dengan Trace ID ${it[0].rawValue} ditemukan, apakah anda menyelesaikan pembayaran?")
                                .setPositiveButton("YA") { dialog, _ ->
                                    dialog.dismiss()

                                    // UPDATE THE DATA
                                    it[0].rawValue?.let { rawValue ->
                                        lifecycleScope.launch {
                                            viewModel.updateTransactionStatus(rawValue.toInt())
                                        }
                                    }

                                    // GET THE DATA
                                    it[0].rawValue?.let { it1 -> viewModel.getTransactionById(it1.toInt()) }

                                    // DECLARE THE DATA
                                    var traceId: Int? = null
                                    var cardId: String? = null
                                    var price: Int? = null
                                    var status: Boolean? = null

                                    // OBSERVE THE DATA
                                    viewModel.transactionDetail.observe(this@QrisActivity) { transaction ->
                                        if (transaction != null) {
                                            // PRINT THE RECEIPT
                                            printReceipt(transaction.price, transaction.id, transaction.cardId, transaction.status)
                                            traceId = transaction.id
                                            cardId = transaction.cardId
                                            price = transaction.price
                                            status = transaction.status

                                            val i = Intent(this@QrisActivity, PaymentSuccessfulActivity::class.java)
                                            i.putExtra("traceId", traceId)
                                            i.putExtra("cardId", cardId)
                                            i.putExtra("totalAmount", price)
                                            i.putExtra("transactionStatus", status)

                                            Timber.i("QRIS ACTIVITY : traceId: $traceId | cardId: $cardId | totalAmount: $price | transactionStatus: $status")

                                            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                            startActivity(i)
                                            finish()
                                        }
                                    }
                                }
                                .setNegativeButton("TIDAK") { dialog, _ ->
                                    dialog.dismiss()
                                    shouldProcessFrames = true
                                }
                                .setOnDismissListener {
                                    shouldProcessFrames = true
                                }
                                .create()
                                .show()
                        } else {
                            AlertDialog.Builder(this@QrisActivity)
                                .setTitle("QRIS")
                                .setMessage("Pesanan dengan Trace ID ${it[0].rawValue} tidak ditemukan!")
                                .setPositiveButton("CLOSE") { dialog, _ ->
                                    dialog.dismiss()
                                    shouldProcessFrames = true
                                }
                                .setOnDismissListener {
                                    shouldProcessFrames = true
                                }
                                .create()
                                .show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun printReceipt(totalAmount: Int, traceId: Int?, cardId: String?, transactionStatus: Boolean?) {
        ThreadPoolManager.getInstance().executeTask {
            val mBitmap = BitmapFactory.decodeResource(resources, R.mipmap.tutwuri)
            try {
                mIPosPrinterService!!.printBitmap(1, 8, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "SMK",
                    "ST",
                    48,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 4, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "Bisnis dan Manajemen",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 4, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "SMKN 9 SEMARANG",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 4, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "EDC NO. 493.24 TYPE IB1",
                    "ST",
                    16,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 4, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "23112022",
                    "ST",
                    16,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 24, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "TERMINAL ID : 0000000",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "MERCHANT ID : 0000000000000",
                    "ST",
                    24,
                    callback
                )
                // DATE
                val currentDate: String
                val currentTime: String

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    currentDate = dateFormatter.format(Date())
                    currentTime = timeFormatter.format(Date())
                } else {
                    val currentTimeMillis = System.currentTimeMillis()
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    currentDate = dateFormatter.format(Date(currentTimeMillis))
                    currentTime = timeFormatter.format(Date(currentTimeMillis))
                }

                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "DATE: $currentDate",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "TIME: $currentTime",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "REFF NO: 000000",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "APRV NO: 000000",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "TRACE NO: $traceId",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "BATCH NO: 000000",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                val paymentStatus = if (transactionStatus == true) "PAID" else "SETTLED"
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "STATUS : $paymentStatus",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "CARD NO: $cardId",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "SALE",
                    "ST",
                    48,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 16, callback)

                // Create a NumberFormat instance for the desired locale and currency
                val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                // Format the totalAmount as rupiah currency
                val formattedAmount = numberFormat.format(totalAmount)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "AMOUNT: $formattedAmount",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 32, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "CARDHOLDER ACKNOWLEDGE RECEIPT OF GOODS AND SERVICES AND AGREES TO PAY THE TOTAL SHOW HERE",
                    "ST",
                    16,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "** CUSTOMER COPY **",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun printerInit() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printerInit(callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    fun getPrinterStatus(): Int {
        Timber.i("***** printerStatus$printerStatus")
        try {
            printerStatus = mIPosPrinterService!!.printerStatus
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        Timber.i("#### printerStatus$printerStatus")
        return printerStatus
    }
}
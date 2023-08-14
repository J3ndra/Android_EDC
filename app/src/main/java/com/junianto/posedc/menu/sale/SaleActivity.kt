package com.junianto.posedc.menu.sale

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.iposprinter.iposprinterservice.IPosPrinterCallback
import com.iposprinter.iposprinterservice.IPosPrinterService
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.database.AppDatabase
import com.junianto.posedc.database.model.Transaction
import com.junianto.posedc.database.repository.TransactionRepository
import com.junianto.posedc.menu.MenuActivity
import com.junianto.posedc.menu.sale.viewmodel.SaleViewModel
import com.junianto.posedc.util.HandlerUtils
import com.junianto.posedc.util.ThreadPoolManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.Random

@AndroidEntryPoint
class SaleActivity : AppCompatActivity() {

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
                    Toast.makeText(this@SaleActivity, R.string.printer_is_working, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_LESS -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@SaleActivity, R.string.out_of_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_EXISTS -> {
                    Toast.makeText(this@SaleActivity, R.string.exists_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_THP_HIGH_TEMP -> {
                    Toast.makeText(this@SaleActivity, R.string.printer_high_temp_alarm, Toast.LENGTH_SHORT).show()
                }
                MSG_MOTOR_HIGH_TEMP -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@SaleActivity, R.string.motor_high_temp_alarm, Toast.LENGTH_SHORT).show()
                    handler?.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000) //马达高温报警，等待3分钟后复位打印机
                }
                MSG_MOTOR_HIGH_TEMP_INIT_PRINTER -> {
                    printerInit()
                }
                MSG_CURRENT_TASK_PRINT_COMPLETE -> {
                    Toast.makeText(this@SaleActivity, R.string.printer_current_task_print_complete, Toast.LENGTH_SHORT).show()
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

    private lateinit var amountEt: TextView
    private val viewModel: SaleViewModel by viewModels()

    private lateinit var dialog: AlertDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale)

        val cardId = intent.getStringExtra("tagId")

        amountEt = findViewById(R.id.amount_et)

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

        val btn1 = findViewById<Button>(R.id.btn_1)
        val btn2 = findViewById<Button>(R.id.btn_2)
        val btn3 = findViewById<Button>(R.id.btn_3)
        val btn4 = findViewById<Button>(R.id.btn_4)
        val btn5 = findViewById<Button>(R.id.btn_5)
        val btn6 = findViewById<Button>(R.id.btn_6)
        val btn7 = findViewById<Button>(R.id.btn_7)
        val btn8 = findViewById<Button>(R.id.btn_8)
        val btn9 = findViewById<Button>(R.id.btn_9)
        val btn0 = findViewById<Button>(R.id.btn_0)
        val btn000 = findViewById<Button>(R.id.btn_000)
        val btnStop = findViewById<Button>(R.id.btn_stop)
        val btnClear = findViewById<Button>(R.id.btn_clear)
        val btnOk = findViewById<Button>(R.id.btn_ok)

        btn1.setOnClickListener {
            updateAmount("1")
        }

        btn2.setOnClickListener {
            updateAmount("2")
        }

        btn3.setOnClickListener {
            updateAmount("3")
        }

        btn4.setOnClickListener {
            updateAmount("4")
        }

        btn5.setOnClickListener {
            updateAmount("5")
        }

        btn6.setOnClickListener {
            updateAmount("6")
        }

        btn7.setOnClickListener {
            updateAmount("7")
        }

        btn8.setOnClickListener {
            updateAmount("8")
        }

        btn9.setOnClickListener {
            updateAmount("9")
        }

        btn0.setOnClickListener {
            updateAmount("0")
        }

        btn000.setOnClickListener {
            updateAmount("000")
        }

        btnStop.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnClear.setOnClickListener {
            clearAmount()
        }

        btnOk.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Apakah anda ingin menggunakan cashless atau cash?")
            dialogBuilder.setPositiveButton("Cash") { dialog: DialogInterface, _: Int ->
                val amountString = amountEt.text.toString().substring(4).replace(".", "")
                val amount = amountString.toInt()
                Timber.i("amount_total: $amount")

                val i = Intent(this, SaleEnterPinActivity::class.java)
                i.putExtra("amount", amount)
                i.putExtra("tagId", cardId)
                startActivity(i)

                dialog.dismiss()
            }
            dialogBuilder.setNegativeButton("Cashless") { dialog: DialogInterface, _: Int ->
                val currentDateTime: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    formatter.format(Date())
                } else {
                    val currentTime = System.currentTimeMillis()
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    formatter.format(Date(currentTime))
                }

                val transaction = Transaction(
                    id = 0,
                    price = amountEt.text.toString().substring(4).replace(".", "").toInt(),
                    transactionDate = currentDateTime,
                    cardId = cardId!!,
                    status = false,
                )

                viewModel.saveTransactionAndNavigate(transaction) {
                    printReceipt(
                        amountEt.text.toString().substring(4).replace(".", "").toInt(),
                        it.toInt(),
                        cardId,
                        false,
                    )

                    val i = Intent(this, MenuActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(i)
                    finish()
                }

                dialog.dismiss()
            }

            dialog = dialogBuilder.create()
            dialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateAmount(digit: String) {
        val currentAmount = amountEt.text.toString().substring(4).replace(".", "").replace(",", "")
        val newAmount = if (currentAmount == "0") digit else currentAmount + digit

        val formatter = DecimalFormat("#,###")
        val newAmountFormatted = formatter.format(newAmount.toInt()).replace(",", ".")
        amountEt.text = "Rp. $newAmountFormatted"
    }

    @SuppressLint("SetTextI18n")
    private fun clearAmount() {
        val currentAmount = amountEt.text.toString().substring(4).replace(".", "").replace(",", "")
        if (currentAmount.isNotEmpty()) {
            val newAmount = currentAmount.substring(0, currentAmount.length - 1)
            if (newAmount.isNotEmpty()) {
                val formattedAmount = formatAmount(newAmount)
                amountEt.text = formattedAmount
            } else {
                amountEt.text = "Rp. 0"
            }
        } else {
            amountEt.text = "Rp. 0"
        }
    }


    private fun formatAmount(amount: String): String {
        val amountWithoutSeparator = amount.replace(".", "").replace(",", "")
        val decimalFormatSymbols = DecimalFormatSymbols()
        decimalFormatSymbols.groupingSeparator = '.'
        val decimalFormat = DecimalFormat("#,###", decimalFormatSymbols)
        val formattedAmount = decimalFormat.format(amountWithoutSeparator.toLong())
        return "Rp. $formattedAmount"
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
                mIPosPrinterService!!.printBlankLines(1, 32, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "QRIS",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.printQRCode(traceId.toString(), 10, 3, callback)
                mIPosPrinterService!!.printBlankLines(1, 32, callback)
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


//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun getCurrentDateTime(): String {
//        val currentDateTime = LocalDateTime.now()
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//        return currentDateTime.format(formatter)
//    }
}

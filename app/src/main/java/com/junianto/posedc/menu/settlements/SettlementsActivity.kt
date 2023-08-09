package com.junianto.posedc.menu.settlements

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
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iposprinter.iposprinterservice.IPosPrinterCallback
import com.iposprinter.iposprinterservice.IPosPrinterService
import com.junianto.posedc.MainActivity
import com.junianto.posedc.R
import com.junianto.posedc.database.model.Transaction
import com.junianto.posedc.menu.settlements.viewmodel.SettlementsViewModel
import com.junianto.posedc.util.HandlerUtils
import com.junianto.posedc.util.ThreadPoolManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random

@AndroidEntryPoint
class SettlementsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SettlementsAdapter
    private val viewModel: SettlementsViewModel by viewModels()

    private var transactions: List<Transaction> = emptyList()

    private var totalAmount: Double = 0.0
    private var totalSales: Int = 0

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
                    Toast.makeText(this@SettlementsActivity, R.string.printer_is_working, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_LESS -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@SettlementsActivity, R.string.out_of_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_EXISTS -> {
                    Toast.makeText(this@SettlementsActivity, R.string.exists_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_THP_HIGH_TEMP -> {
                    Toast.makeText(this@SettlementsActivity, R.string.printer_high_temp_alarm, Toast.LENGTH_SHORT).show()
                }
                MSG_MOTOR_HIGH_TEMP -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@SettlementsActivity, R.string.motor_high_temp_alarm, Toast.LENGTH_SHORT).show()
                    handler?.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000) //马达高温报警，等待3分钟后复位打印机
                }
                MSG_MOTOR_HIGH_TEMP_INIT_PRINTER -> {
                    printerInit()
                }
                MSG_CURRENT_TASK_PRINT_COMPLETE -> {
                    Toast.makeText(this@SettlementsActivity, R.string.printer_current_task_print_complete, Toast.LENGTH_SHORT).show()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settlements)

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

        recyclerView = findViewById(R.id.transaction_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SettlementsAdapter()
        recyclerView.adapter = adapter

        viewModel.allTransactions.observe(this) { transactionList ->
            transactions = transactionList // Update the transactions variable
            adapter.setTransactions(transactions)

            val totalAmount = calculateTotalAmount(transactions)
            updateTotalAmount(totalAmount)
        }

        val filterSpinner = findViewById<Spinner>(R.id.filter_spinner)
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val filterDays = when (parent?.getItemAtPosition(position).toString()) {
                    "1 Day" -> 1
                    "7 Days" -> 7
                    "30 Days" -> 30
                    else -> 30 // Default to 30 Days if no match is found
                }
                val filteredTransactions = filterTransactions(transactions, filterDays)
                adapter.setTransactions(filteredTransactions)

                totalAmount = calculateTotalAmount(filteredTransactions)
                updateTotalAmount(totalAmount)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }


        val btnSettlements = findViewById<TextView>(R.id.btn_settlements)
        btnSettlements.setOnClickListener {
            printSettlement()

            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(i)
            finish()
        }
    }

    private fun filterTransactions(transactions: List<Transaction>, days: Int): List<Transaction> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days) // Subtract the specified number of days from the current date
        val startDate = calendar.time // Get the start date as a Date object

        return transactions.filter { transaction ->
            val transactionDate = convertStringToDate(transaction.transactionDate)
            transactionDate >= startDate
        }
    }

    private fun convertStringToDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }

    private fun calculateTotalAmount(transactions: List<Transaction>): Double {
        var totalAmount = 0.0
        for (transaction in transactions) {
            totalAmount += transaction.price
        }
        return totalAmount
    }

    private fun updateTotalAmount(totalAmount: Double) {
        val totalAmountTextView = findViewById<TextView>(R.id.tv_total)
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        currencyFormat.maximumFractionDigits = 0
        val formattedTotalAmount = currencyFormat.format(totalAmount).substring(2)
        val finalFormattedTotalAmount = "Rp. $formattedTotalAmount"

        totalAmountTextView.text = finalFormattedTotalAmount
    }

    private fun printSettlement() {
        ThreadPoolManager.getInstance().executeTask{
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
                mIPosPrinterService!!.printBlankLines(1, 8, callback)

                val currentDateTime: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    formatter.format(Date())
                } else {
                    val currentTime = System.currentTimeMillis()
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    formatter.format(Date(currentTime))
                }
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "REPORT DATE : $currentDateTime",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 32, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "SETTLEMENT",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "HOST : SMKN 9 SEMARANG",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                for (transaction in transactions) {
                    totalSales++

                    mIPosPrinterService!!.printSpecifiedTypeText(
                        "TRACE ID : ${transaction.id}",
                        "ST",
                        24,
                        callback
                    )
                    mIPosPrinterService!!.printBlankLines(1, 8, callback)
                    mIPosPrinterService!!.printSpecifiedTypeText(
                        "DATE TIME : ${transaction.transactionDate}",
                        "ST",
                        24,
                        callback
                    )
                    mIPosPrinterService!!.printBlankLines(1, 8, callback)

                    // Create a NumberFormat instance for the desired locale and currency
                    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    // Format the totalAmount as rupiah currency
                    val formattedAmount = numberFormat.format(transaction.price).substring(2)
                    mIPosPrinterService!!.printSpecifiedTypeText(
                        "AMOUNT : $formattedAmount",
                        "ST",
                        24,
                        callback
                    )
                    mIPosPrinterService!!.printBlankLines(1, 8, callback)
                }
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "TOTAL SALES : $totalSales",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)

                // Create a NumberFormat instance for the desired locale and currency
                val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                // Format the totalAmount as rupiah currency
                val formattedTotalAmount = numberFormat.format(totalAmount).substring(2)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "TOTAL AMOUNT : $formattedTotalAmount",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    -------------------------
                    """.trimIndent(), "ST", 32, callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "** SETTLEMENT CONFIRMED **",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
//        for (transaction in transactions) {
//            val settlementText = "ID: ${transaction.id}, Price: ${transaction.price}, Date: ${transaction.transactionDate}"
//            Timber.d(settlementText)
//            // Print the settlement using your desired printing mechanism
//        }
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
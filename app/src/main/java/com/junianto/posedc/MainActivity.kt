package com.junianto.posedc

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.RemoteException
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iposprinter.iposprinterservice.IPosPrinterCallback
import com.iposprinter.iposprinterservice.IPosPrinterService
import com.junianto.posedc.menu.MenuActivity
import com.junianto.posedc.util.ButtonDelayUtils
import com.junianto.posedc.util.BytesUtil
import com.junianto.posedc.util.HandlerUtils
import com.junianto.posedc.util.MemInfo.bitmapRecycle
import com.junianto.posedc.util.PrintContentsExamples.Baidu
import com.junianto.posedc.util.PrintContentsExamples.Elemo
import com.junianto.posedc.util.PrintContentsExamples.Text
import com.junianto.posedc.util.PrintContentsExamples.customCHR
import com.junianto.posedc.util.PrintContentsExamples.customCHZ1
import com.junianto.posedc.util.ThreadPoolManager
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Random

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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
                        loopPrint(loopPrintFlag)
                    }
                }
                MSG_IS_BUSY -> {
                    Toast.makeText(this@MainActivity, R.string.printer_is_working, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_LESS -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@MainActivity, R.string.out_of_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_PAPER_EXISTS -> {
                    Toast.makeText(this@MainActivity, R.string.exists_paper, Toast.LENGTH_SHORT).show()
                }
                MSG_THP_HIGH_TEMP -> {
                    Toast.makeText(this@MainActivity, R.string.printer_high_temp_alarm, Toast.LENGTH_SHORT).show()
                }
                MSG_MOTOR_HIGH_TEMP -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(this@MainActivity, R.string.motor_high_temp_alarm, Toast.LENGTH_SHORT).show()
                    handler?.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP_INIT_PRINTER, 180000) //马达高温报警，等待3分钟后复位打印机
                }
                MSG_MOTOR_HIGH_TEMP_INIT_PRINTER -> {
                    printerInit()
                }
                MSG_CURRENT_TASK_PRINT_COMPLETE -> {
                    Toast.makeText(this@MainActivity, R.string.printer_current_task_print_complete, Toast.LENGTH_SHORT).show()
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
        setContentView(R.layout.activity_main)

        val btnGoToMenu: Button = findViewById(R.id.btn_go_to_menu)

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

        btnGoToMenu.setOnClickListener {
//            if (ButtonDelayUtils.isFastDoubleClick()) {
//                return@setOnClickListener
//            }
//
//            printText()
            val i = Intent(this, MenuActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onResume() {
        Timber.d("activity onResume")
        super.onResume()
    }

    override fun onPause() {
        Timber.d("activity onPause")
        super.onPause()
    }

    override fun onStop() {
        Timber.d("activity onStop")
        loopPrintFlag = DEFAULT_LOOP_PRINT
        super.onStop()
    }

    override fun onDestroy() {
        Timber.d("activity onDestroy")
        super.onDestroy()
        unregisterReceiver(IPosPrinterStatusListener)
        unbindService(connectService)
        handler!!.removeCallbacksAndMessages(null)
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

    fun loopPrint(flag: Int) {
        when (flag) {
            MULTI_THREAD_LOOP_PRINT -> multiThreadLoopPrint()
            DEMO_LOOP_PRINT -> demoLoopPrint()
            INPUT_CONTENT_LOOP_PRINT -> bigDataPrintTest(127, loopContent)
            PRINT_DRIVER_ERROR_TEST -> printDriverTest()
            else -> {}
        }
    }

    fun multiThreadLoopPrint() {
        Timber.e("发起打印任务 --> ")
        when (random.nextInt(12)) {
            0 -> printText()
            1 -> printBarcode()
            2 -> fullTest()
            3 -> printQRCode()
            4 -> printBitmap()
            5 -> printTable()
            6 -> printBaiduBill()
            7 -> printKoubeiBill()
            8 -> printMeiTuanBill()
            9 -> printErlmoBill()
            10 -> printSelf()
            11 -> continuPrint()
            else -> {}
        }
    }

    fun demoLoopPrint() {
        Timber.e("发起打印任务 --> ")
        when (random.nextInt(7)) {
            0 -> printKoubeiBill()
            1 -> printBarcode()
            2 -> printBaiduBill()
            3 -> printBitmap()
            4 -> printErlmoBill()
            5 -> printQRCode()
            6 -> printMeiTuanBill()
            else -> {}
        }
    }

    fun printSelf() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printerInit(callback)
                mIPosPrinterService!!.printSpecifiedTypeText("   打印机自检\n", "ST", 48, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printRawData(BytesUtil.blackBlockData(300), callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 10, 1, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printSpecifiedTypeText("   打印机正常\n", "ST", 48, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.printSpecifiedTypeText("        欢迎使用\n", "ST", 32, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取打印长度
     */
    fun queryPrintLength() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "获取打印长度\n暂未实现\n\n----------end-----------\n\n",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印机走纸
     */
    fun printerRunPaper(lines: Int) {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printerFeedLines(lines, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印空白行
     */
    fun printLineWrap(lines: Int, height: Int) {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printBlankLines(lines, height, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印随机黑点
     */
    fun printRandomDot(lines: Int) {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printRawData(BytesUtil.randomDotData(lines), callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印大黑块
     */
    fun printBlackBlock(height: Int) {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printRawData(BytesUtil.blackBlockData(height), callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印文字
     */
    fun printText() {
        ThreadPoolManager.getInstance().executeTask {
            val mBitmap = BitmapFactory.decodeResource(resources, R.mipmap.test)
            try {
                val cmd = ByteArray(3)
                cmd[0] = 0x1B
                cmd[1] = 0x45
                cmd[2] = 125
                mIPosPrinterService!!.sendUserCMDData(cmd, callback)
                val cmd1 = byteArrayOf(27, 45, 0)
                mIPosPrinterService!!.printSpecifiedTypeText("    智能POS机\n", "ST", 48, callback)
                mIPosPrinterService!!.sendUserCMDData(cmd1, callback)
                mIPosPrinterService!!.printSpecifiedTypeText("    智能POS机\n", "ST", 48, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "    智能POS机数据终端\n",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "      欢迎使智能POS机数据终端\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "智能POS 数据终端 智能POS\n",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "#POS POS ipos POS POS POS POS ipos POS POS ipos#\n",
                    "ST",
                    16,
                    callback
                )
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.printBitmap(1, 12, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.PrintSpecFormatText("开启打印测试\n", "ST", 32, 1, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "********************************",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText("这是一行16号字体\n", "ST", 16, callback)
                mIPosPrinterService!!.printSpecifiedTypeText("这是一行24号字体\n", "ST", 24, callback)
                mIPosPrinterService!!.PrintSpecFormatText("这是一行24号字体\n", "ST", 24, 2, callback)
                mIPosPrinterService!!.printSpecifiedTypeText("这是一行32号字体\n", "ST", 32, callback)
                mIPosPrinterService!!.PrintSpecFormatText("这是一行32号字体\n", "ST", 32, 2, callback)
                mIPosPrinterService!!.printSpecifiedTypeText("这是一行48号字体\n", "ST", 48, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234\n",
                    "ST",
                    16,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "abcdefghijklmnopqrstuvwxyz56789\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "κρχκμνκλρκνκνμρτυφ\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 10, 1, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                for (i in 0..11) {
                    mIPosPrinterService!!.printRawData(BytesUtil.initLine1(384, i), callback)
                }
                mIPosPrinterService!!.PrintSpecFormatText("打印测试完成\n", "ST", 32, 1, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "**********END***********\n\n",
                    "ST",
                    32,
                    callback
                )
                bitmapRecycle(mBitmap)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印表格
     */
    fun printTable() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                mIPosPrinterService!!.setPrinterPrintFontSize(24, callback)
                var text = arrayOfNulls<String>(4)
                var width = intArrayOf(8, 6, 6, 7)
                var align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                text[0] = "名称"
                text[1] = "数量"
                text[2] = "单价"
                text[3] = "金额"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "草莓酸奶A布甸"
                text[1] = "4"
                text[2] = "12.00"
                text[3] = "48.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包B"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                text[1] = "100"
                text[2] = "16.00"
                text[3] = "1600.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.setPrinterPrintFontSize(24, callback)
                text = arrayOfNulls(3)
                width = intArrayOf(8, 6, 7)
                align = intArrayOf(0, 2, 2)
                text[0] = "菜品"
                text[1] = "数量"
                text[2] = "金额"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "草莓酸奶布甸"
                text[1] = "4"
                text[2] = "48.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包B"
                text[1] = "10"
                text[2] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                text[1] = "100"
                text[2] = "1600.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包"
                text[1] = "10"
                text[2] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(2, callback)
                mIPosPrinterService!!.setPrinterPrintFontSize(16, callback)
                text = arrayOfNulls(4)
                width = intArrayOf(10, 6, 6, 8)
                align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                text[0] = "名称"
                text[1] = "数量"
                text[2] = "单价"
                text[3] = "金额"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "草莓酸奶A布甸"
                text[1] = "4"
                text[2] = "12.00"
                text[3] = "48.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包B"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                text[1] = "100"
                text[2] = "16.00"
                text[3] = "1600.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 8, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印图片
     */
    fun printBitmap() {
        ThreadPoolManager.getInstance().executeTask {
            val mBitmap = BitmapFactory.decodeResource(resources, R.mipmap.test_p)
            try {
                mIPosPrinterService!!.printBitmap(0, 4, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printBitmap(1, 6, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printBitmap(2, 8, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printBitmap(2, 10, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printBitmap(1, 12, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printBitmap(0, 14, mBitmap, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印一维码
     */
    fun printBarcode() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                mIPosPrinterService!!.printBarCode("2017072618", 8, 2, 5, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 25, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printBarCode("2017072618", 8, 3, 6, 1, callback)
                mIPosPrinterService!!.printBlankLines(1, 25, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(2, callback)
                mIPosPrinterService!!.printBarCode("2017072618", 8, 4, 7, 2, callback)
                mIPosPrinterService!!.printBlankLines(1, 25, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(2, callback)
                mIPosPrinterService!!.printBarCode("2017072618", 8, 5, 8, 3, callback)
                mIPosPrinterService!!.printBlankLines(1, 25, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printBarCode("2017072618", 8, 3, 7, 3, callback)
                mIPosPrinterService!!.printBlankLines(1, 25, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printBarCode("2017072618", 8, 3, 6, 1, callback)
                mIPosPrinterService!!.printBlankLines(1, 25, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printBarCode("2017072618", 8, 3, 4, 2, callback)
                mIPosPrinterService!!.printBlankLines(1, 25, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印二维码
     */
    fun printQRCode() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 2, 1, callback)
                mIPosPrinterService!!.printBlankLines(1, 15, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 3, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 15, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(2, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 4, 2, callback)
                mIPosPrinterService!!.printBlankLines(1, 15, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 5, 3, callback)
                mIPosPrinterService!!.printBlankLines(1, 15, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 6, 2, callback)
                mIPosPrinterService!!.printBlankLines(1, 15, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(2, callback)
                mIPosPrinterService!!.printQRCode("http://www.baidu.com\n", 7, 1, callback)
                mIPosPrinterService!!.printBlankLines(1, 15, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打印饿了么小票
     */
    fun printErlmoBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printSpecifiedTypeText(Elemo, "ST", 32, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 打百度小票
     */
    fun printBaiduBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printSpecifiedTypeText(Baidu, "ST", 32, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 口碑外卖
     */
    fun printKoubeiBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printSpecifiedTypeText("   #4口碑外卖\n", "ST", 48, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """         冯记黄焖鸡米饭
********************************
""",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText("17:20 尽快送达\n", "ST", 48, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "--------------------------------\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "18610858337韦小宝创智天地广场7号楼(605室)\n",
                    "ST",
                    48,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "--------------------------------\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText("下单: 16:35\n", "ST", 48, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "********************************\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """菜品          数量   单价   金额
--------------------------------
黄焖五花肉 (大) (不辣)
               1      25      25
黄焖五花肉 (小) (不辣)
               1      25      25黄焖五花肉 (小) (微辣)
               1      25      25
--------------------------------
配送费                         2
--------------------------------
""", "ST", 24, callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "            实付金额: 27\n\n",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText("    口碑外卖\n\n\n", "ST", 48, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 美团小票
     */
    fun printMeiTuanBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "  #1  美团测试\n\n",
                    "ST",
                    48,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "      粤香港式烧腊(第1联)\n\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "------------------------\n\n*********预订单*********\n",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "  期望送达时间:[18:00]\n\n",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    --------------------------------
                    下单时间: 01-01 12:00
                    """.trimIndent(),
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText("备注: 别太辣\n", "ST", 32, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    菜品          数量   小计金额
                    --------------------------------
                    
                    
                    """.trimIndent(),
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    红烧肉          X1    12
                    红烧肉1         X1    12
                    红烧肉2         X1    12
                    
                    
                    """.trimIndent(),
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "--------------------------------\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    配送费                         5
                    餐盒费                         1
                    [超时赔付] - 详见订单
                    可口可乐: x1
                    """.trimIndent(), "ST", 24, callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "--------------------------------\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "合计                18元\n\n",
                    "ST",
                    32,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "--------------------------------\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "张* 18312345678\n地址信息\n",
                    "ST",
                    48,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "--------------------------------\n",
                    "ST",
                    24,
                    callback
                )
                mIPosPrinterService!!.printSpecifiedTypeText(
                    "  #1  美团测试\n\n\n",
                    "ST",
                    48,
                    callback
                )
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 每次下发内容以64k为单位递增，最大512k
     */
    fun printDriverTest() {
        if (printDriverTestCount >= 8) {
            loopPrintFlag = DEFAULT_LOOP_PRINT
            printDriverTestCount = 0
        } else {
            printDriverTestCount++
            bigDataPrintTest(printDriverTestCount * 16, 0x11.toByte())
        }
    }

    fun bigDataPrintTest(numK: Int, data: Byte) {
        ThreadPoolManager.getInstance().executeTask(Runnable {
            val num4K = 1024 * 4
            val length = if (numK > 127) num4K * 127 else num4K * numK
            val dataBytes = ByteArray(length)
            for (i in 0 until length) {
                dataBytes[i] = data
            }
            try {
                mIPosPrinterService!!.printRawData(dataBytes, callback)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        })
    }

    fun fullTest() {
        ThreadPoolManager.getInstance().executeTask {
            var bmp: Bitmap?
            try {
                mIPosPrinterService!!.printRawData(BytesUtil.initBlackBlock(384), callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printRawData(BytesUtil.initBlackBlock(48, 384), callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printRawData(BytesUtil.initGrayBlock(48, 384), callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(0, callback)
                mIPosPrinterService!!.setPrinterPrintFontSize(24, callback)
                var text = arrayOfNulls<String>(4)
                var width = intArrayOf(10, 6, 6, 8)
                var align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                text[0] = "名称"
                text[1] = "数量"
                text[2] = "单价"
                text[3] = "金额"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "草莓酸奶A布甸"
                text[1] = "4"
                text[2] = "12.00"
                text[3] = "48.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包B"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                text[1] = "100"
                text[2] = "16.00"
                text[3] = "1600.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(1, callback)
                mIPosPrinterService!!.setPrinterPrintFontSize(24, callback)
                text = arrayOfNulls(3)
                width = intArrayOf(10, 6, 8)
                align = intArrayOf(0, 2, 2)
                text[0] = "菜品"
                text[1] = "数量"
                text[2] = "金额"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "草莓酸奶布甸"
                text[1] = "4"
                text[2] = "48.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包B"
                text[1] = "10"
                text[2] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                text[1] = "100"
                text[2] = "1600.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包"
                text[1] = "10"
                text[2] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 16, callback)
                mIPosPrinterService!!.setPrinterPrintAlignment(2, callback)
                mIPosPrinterService!!.setPrinterPrintFontSize(16, callback)
                text = arrayOfNulls(4)
                width = intArrayOf(10, 6, 6, 8)
                align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                text[0] = "名称"
                text[1] = "数量"
                text[2] = "单价"
                text[3] = "金额"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "草莓酸奶A布甸"
                text[1] = "4"
                text[2] = "12.00"
                text[3] = "48.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包B"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                text[1] = "100"
                text[2] = "16.00"
                text[3] = "1600.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 1, callback)
                text[0] = "酸奶水果夹心面包"
                text[1] = "10"
                text[2] = "4.00"
                text[3] = "40.00"
                mIPosPrinterService!!.printColumnsText(text, width, align, 0, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                bmp = BitmapFactory.decodeResource(resources, R.mipmap.test_p)
                mIPosPrinterService!!.printBitmap(0, 12, bmp, callback)
                mIPosPrinterService!!.printBitmap(1, 6, bmp, callback)
                mIPosPrinterService!!.printBitmap(2, 16, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    智能POS
                    智能POS智能POS
                    智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS智能POS智能POS智能
                    智能POS智能POS智能POS智能POS智能POS智能POS智能
                    智能POS智能POS智能POS智能POS智能POS智能POS智能
                    智能POS智能POS智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS
                    智能POS智能POS
                    智能POS
                    
                    """.trimIndent(), "ST", 16, callback
                )
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    智能POS
                    智能POS智能POS
                    智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS智能POS智能
                    智能POS智能POS智能POS智能POS
                    智能POS智能POS智能POS
                    智能POS智能POS
                    智能POS
                    
                    """.trimIndent(), "ST", 24, callback
                )
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    手
                    手手
                    手手手
                    手手手手
                    手手手手手
                    手手手手手手
                    手手手手手手手
                    手手手手手手手手
                    手手手手手手手手手
                    手手手手手手手手手手
                    手手手手手手手手手手手
                    手手手手手手手手手手手手手手手手手手手手手手手
                    手手手手手手手手手手
                    手手手手手手手手手
                    手手手手手手手手
                    手手手手手手手
                    手手手手手手
                    手手手手手
                    手手手手
                    手手手
                    手手
                    手
                    
                    """.trimIndent(), "ST", 32, callback
                )
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(
                    """
                    手
                    手手
                    手手手
                    手手手手
                    手手手手手
                    手手手手手手
                    手手手手手手手
                    手手手手手手手手手手手手手手手
                    手手手手手手
                    手手手手手
                    手手手手
                    手手手
                    手手
                    手
                    
                    """.trimIndent(), "ST", 48, callback
                )
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                var k = 8
                for (i in 0..47) {
                    bmp = BytesUtil.getLineBitmapFromData(12, k)
                    k += 8
                    if (null != bmp) {
                        mIPosPrinterService!!.printBitmap(1, 11, bmp, callback)
                    }
                }
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                /*加快bitmap回收，减少内存占用*/bitmapRecycle(bmp)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    fun continuPrint() {
        ThreadPoolManager.getInstance().executeTask {
            val bmp = BitmapFactory.decodeResource(resources, R.mipmap.test)
            try {
                mIPosPrinterService!!.printSpecifiedTypeText(customCHR, "ST", 16, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(Text, "ST", 16, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(customCHR, "ST", 24, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(Text, "ST", 24, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(customCHR, "ST", 32, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(Text, "ST", 32, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(customCHR, "ST", 48, callback)
                mIPosPrinterService!!.printSpecifiedTypeText(customCHZ1, "ST", 48, callback)
                mIPosPrinterService!!.printBlankLines(1, 10, callback)
                mIPosPrinterService!!.printBitmap(0, 4, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(0, 5, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(0, 6, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(0, 7, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(0, 8, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(1, 9, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(1, 10, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(1, 11, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(1, 12, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(1, 13, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(2, 12, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(3, 11, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(4, 10, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(5, 9, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                mIPosPrinterService!!.printBitmap(6, 8, bmp, callback)
                mIPosPrinterService!!.printBlankLines(1, 20, callback)
                /*加快bitmap回收，减少内存占用*/bitmapRecycle(bmp)
                mIPosPrinterService!!.printerPerformPrint(160, callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }
}
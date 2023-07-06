package com.junianto.posedc.util

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import java.util.Random

object BytesUtil {
    private const val TAG = "BytesUtil"
    private const val MATRIX_DATA_ROW = 384
    private const val BYTE_BIT = 8
    private const val BYTE_PER_LINE = 48

    // Fields
    private val random = Random()

    // Methods

    /**
     * 随机生成黑点打印数据
     */
    fun randomDotData(lines: Int): ByteArray {
        val printData = ByteArray(lines * BYTE_PER_LINE)
        for (i in 0 until lines) {
            val lineData = ByteArray(BYTE_PER_LINE)
            val randData = random.nextInt(BYTE_PER_LINE)
            lineData[randData] = 0x01
            System.arraycopy(lineData, 0, printData, i * 48, BYTE_PER_LINE)
        }
        return printData
    }

    /**
     * 生成间断性黑块数据
     *
     * @param w : 打印纸宽度, 单位点
     * @return
     */
    fun initBlackBlock(w: Int): ByteArray {
        val ww = w / 8
        val n = ww / 12
        val hh = n * 24
        val data = ByteArray(hh * ww)

        var k = 0
        for (i in 0 until n) {
            for (j in 0 until 24) {
                for (m in 0 until ww) {
                    data[k++] = if (m / 12 == i) {
                        0xFF.toByte()
                    } else {
                        0x00.toByte()
                    }
                }
            }
        }

        return data
    }

    /**
     * 生成一大块黑块数据
     *
     * @param h : 黑块高度, 单位点
     * @param w : 黑块宽度, 单位点, 8的倍数
     * @return
     */
    fun initBlackBlock(h: Int, w: Int): ByteArray {
        val hh = h
        val ww = w / 8
        val data = ByteArray(hh * ww)

        var k = 0
        for (i in 0 until hh) {
            for (j in 0 until ww) {
                data[k++] = 0xFF.toByte()
            }
        }

        return data
    }

    /**
     * 生成黑块数据
     */
    fun blackBlockData(lines: Int): ByteArray {
        val printData = ByteArray(lines * BYTE_PER_LINE)
        for (i in 0 until lines * BYTE_PER_LINE) {
            printData[i] = 0xFF.toByte()
        }
        return printData
    }

    /**
     * 生成灰块数据
     *
     * @param h : 灰块高度, 单位点
     * @param w : 灰块宽度, 单位点, 8的倍数
     * @return
     */
    fun initGrayBlock(h: Int, w: Int): ByteArray {
        val hh = h
        val ww = w / 8
        val data = ByteArray(hh * ww)

        var k = 0
        for (i in 0 until hh) {
            for (j in 0 until ww) {
                data[k++] = 0x55.toByte()
            }
        }

        return data
    }

    fun initLine1(w: Int, type: Int): ByteArray? {
        val kk = arrayOf(
            byteArrayOf(0x00, 0x00, 0x7c, 0x7c, 0x7c, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0xff.toByte(), 0xff.toByte(), 0xff.toByte(), 0x00, 0x00),
            byteArrayOf(0x00, 0x44, 0x44, 0xff.toByte(), 0x44, 0x44, 0x00),
            byteArrayOf(0x00, 0x22, 0x55, 0x88.toByte(), 0x55, 0x22, 0x00),
            byteArrayOf(0x08, 0x08, 0x1c, 0x7f, 0x1c, 0x08, 0x08),
            byteArrayOf(0x08, 0x14, 0x22, 0x41, 0x22, 0x14, 0x08),
            byteArrayOf(0x08, 0x14, 0x2a, 0x55, 0x2a, 0x14, 0x08),
            byteArrayOf(0x08, 0x1c, 0x3e, 0x7f, 0x3e, 0x1c, 0x08),
            byteArrayOf(0x49, 0x22, 0x14, 0x49, 0x14, 0x22, 0x49),
            byteArrayOf(0x63, 0x77, 0x3e, 0x1c, 0x3e, 0x77, 0x63),
            byteArrayOf(0x70, 0x20, 0xaf.toByte(), 0xaa.toByte(), 0xfa.toByte(), 0x02, 0x07),
            byteArrayOf(
                0xef.toByte(),
                0x28,
                0xee.toByte(),
                0xaa.toByte(),
                0xee.toByte(),
                0x82.toByte(),
                0xfe.toByte()
            )
        )
        val ww = w / 8
        val data = ByteArray(13 * ww)
        var k = 0
        for (i in 0 until 3 * ww) {
            data[k++] = 0
        }
        for (i in 0 until ww) {
            data[k++] = kk[type][0]
        }
        for (i in 0 until ww) {
            data[k++] = kk[type][1]
        }
        for (i in 0 until ww) {
            data[k++] = kk[type][2]
        }
        for (i in 0 until ww) {
            data[k++] = kk[type][3]
        }
        for (i in 0 until ww) {
            data[k++] = kk[type][4]
        }
        for (i in 0 until ww) {
            data[k++] = kk[type][5]
        }
        for (i in 0 until ww) {
            data[k++] = kk[type][6]
        }
        for (i in 0 until 3 * ww) {
            data[k++] = 0
        }
        return data
    }

    fun initLine2(w: Int): ByteArray? {
        val ww = (w + 7) / 8
        val data = ByteArray(12 * ww + 8)
        data[0] = 0x1D
        data[1] = 0x76
        data[2] = 0x30
        data[3] = 0x00
        data[4] = ww.toByte() //xL
        data[5] = (ww shr 8).toByte() //xH
        data[6] = 12 //高度13
        data[7] = 0
        var k = 8
        for (i in 0 until 5 * ww) {
            data[k++] = 0
        }
        for (i in 0 until ww) {
            data[k++] = 0x7f
        }
        for (i in 0 until ww) {
            data[k++] = 0x7f
        }
        for (i in 0 until 5 * ww) {
            data[k++] = 0
        }
        return data
    }

    /**
     * 将Bitmap转换为打印数据
     */
    fun bitmap2PrinterBytes(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        // 计算缩放比例
        val scaleWidth = BYTE_PER_LINE * BYTE_BIT / width.toFloat()
        val scaleHeight = MATRIX_DATA_ROW / height.toFloat()

        // 创建一个矩阵对象
        val matrix = Matrix()
        // 设置缩放比例
        matrix.postScale(scaleWidth, scaleHeight)

        // 创建缩放后的Bitmap
        val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)

        // 将Bitmap转换为灰度图像
        val grayBitmap = convertToGrayscale(scaledBitmap)

        // 将灰度图像转换为打印数据
        val printData = convertBitmapToByteArray(grayBitmap)

        return printData
    }

    @SuppressLint("DrawAllocation")
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height

        val bmpData = ByteArray(width * height / BYTE_BIT)

        val bmpBuffer = IntArray(width * height)
        bitmap.getPixels(bmpBuffer, 0, width, 0, 0, width, height)

        var index = 0
        var byteValue = 0
        var bitValue = BYTE_BIT - 1

        for (i in bmpBuffer.indices) {
            val gray = getGrayLevel(bmpBuffer[i])
            if (gray < 128) {
                byteValue = byteValue or (1 shl bitValue)
            }
            bitValue--

            if (bitValue < 0) {
                bmpData[index++] = byteValue.toByte()
                byteValue = 0
                bitValue = BYTE_BIT - 1
            }
        }

        return bmpData
    }

    private fun convertToGrayscale(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val grayBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayBitmap)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        paint.colorFilter = colorFilter

        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return grayBitmap
    }

    private fun getGrayLevel(color: Int): Int {
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return (red * 0.3 + green * 0.59 + blue * 0.11).toInt()
    }

    fun getBitmapFromData(pixels: IntArray?, width: Int, height: Int): Bitmap? {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun getLineBitmapFromData(size: Int, width: Int): Bitmap? {
        val pixels: IntArray? = BytesUtil.createLineData(size, width)
        return BytesUtil.getBitmapFromData(pixels, width, size + 6)
    }

    fun createLineData(size: Int, width: Int): IntArray? {
        val pixels = IntArray(width * (size + 6))
        var k = 0
        for (j in 0..2) {
            for (i in 0 until width) {
                pixels[k++] = -0x1
            }
        }
        for (j in 0 until size) {
            for (i in 0 until width) {
                pixels[k++] = -0x1000000
            }
        }
        for (j in 0..2) {
            for (i in 0 until width) {
                pixels[k++] = -0x1
            }
        }
        return pixels
    }
}
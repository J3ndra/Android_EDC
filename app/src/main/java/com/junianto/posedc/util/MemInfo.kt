package com.junianto.posedc.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

object MemInfo {

    // Get available memory
    fun getmem_UNUSED(mContext: Context): Long {
        var MEM_UNUSED: Long
        // Get ActivityManager
        val am = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // Create ActivityManager.MemoryInfo object
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        // Get available memory space
        MEM_UNUSED = mi.availMem / 1048576
        return MEM_UNUSED
    }

    // Get total memory
    fun getmem_TOLAL(): Long {
        var mTotal: Long
        // Interpret kernel information read from /proc/meminfo
        val path = "/proc/meminfo"
        var content: String? = null
        var br: BufferedReader? = null
        try {
            br = BufferedReader(FileReader(path), 8)
            var line: String?
            if (br.readLine().also { line = it } != null) {
                content = line
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            if (br != null) {
                try {
                    br.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        // Begin index
        val begin = content!!.indexOf(':')
        // End index
        val end = content.indexOf('k')
        // Extract string information
        content = content.substring(begin + 1, end).trim()
        mTotal = content.toLong()
        return mTotal
    }

    /* Speed up bitmap recycling to reduce memory usage */
    fun bitmapRecycle(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}

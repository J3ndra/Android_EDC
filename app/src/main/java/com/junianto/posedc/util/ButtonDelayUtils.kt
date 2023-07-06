package com.junianto.posedc.util

object ButtonDelayUtils {
    // Constants
    private var lastClickTime: Long = 0

    // Methods
    fun isFastDoubleClick(): Boolean {
        val time = System.currentTimeMillis()
        if (time - lastClickTime < 500) {
            return true
        }
        lastClickTime = time
        return false
    }
}
package com.junianto.posedc.util

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object ThreadPoolManager {
    private val service: ExecutorService

    init {
        val num = Runtime.getRuntime().availableProcessors() * 20
        service = Executors.newFixedThreadPool(num)
    }

    private val manager = ThreadPoolManager

    fun getInstance(): ThreadPoolManager {
        return manager
    }

    fun executeTask(runnable: Runnable) {
        service.execute(runnable)
    }
}
package com.junianto.posedc.util

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.SoftReference

object HandlerUtils {
    /**
     * Implement this interface in the class where the handler is used,
     * and pass the instantiated reference to the handler.
     */
    interface IHandlerIntent {
        fun handlerIntent(message: Message)
    }

    class MyHandler(t: IHandlerIntent) : Handler() {
        private val owner: SoftReference<IHandlerIntent> = SoftReference(t)

        constructor(looper: Looper, t: IHandlerIntent) : this(t) {
            owner.get()
        }

        override fun handleMessage(msg: Message) {
            owner.get()?.handlerIntent(msg)
        }
    }
}
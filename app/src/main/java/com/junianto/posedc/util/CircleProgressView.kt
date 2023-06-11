package com.junianto.posedc.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Bundle
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import android.widget.Toast

class CircleProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val progressPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    private val textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }

    private var progress = 0f
    private val rectF = RectF()

    private var listener: Listener? = null

    interface Listener {
        fun onTimerFinished(success: Boolean)
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (Math.min(width, height) - paddingLeft - paddingRight) / 2f - progressPaint.strokeWidth / 2f

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Draw progress arc
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        canvas.drawArc(rectF, -90f, 360f * progress, false, progressPaint)

        // Draw text timer
        val text = (progress * 20).toInt().toString() // Replace 20 with your desired countdown time
        val textHeight = textPaint.descent() + textPaint.ascent()
        canvas.drawText(text, centerX, centerY - textHeight / 2f, textPaint)
    }

    fun startCountdown(activity: Activity) {
        val totalTime = 20000L // 20 seconds in milliseconds
        val interval = 100L // Timer interval in milliseconds
        var elapsedTime = 0L

        val timer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime = totalTime - millisUntilFinished
                progress = elapsedTime.toFloat() / totalTime.toFloat()
                invalidate()
            }

            override fun onFinish() {
                progress = 1f
                invalidate()
                listener?.onTimerFinished(false)
            }
        }
        timer.start()
    }

    fun handleNfcIntent(intent: Intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            val ndef = Ndef.get(tag)

            ndef?.connect()
            val ndefMessage = ndef?.ndefMessage
            if (ndefMessage != null) {
                // NFC tag tapped successfully
                listener?.onTimerFinished(true)
            } else {
                // NFC tag tapped but not recognized
                Toast.makeText(context, "NFC tag not recognized", Toast.LENGTH_SHORT).show()
            }
            ndef?.close()
        }
    }
}

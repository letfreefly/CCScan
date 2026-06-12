package com.ccscan.barcode

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import kotlin.math.abs

class FloatingBallService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingBallView: View? = null
    private var ballParams: WindowManager.LayoutParams? = null

    private val handler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null

    private var initialX = 0
    private var initialY = 0
    private var touchX = 0f
    private var touchY = 0f
    private var isLongPressTriggered = false
    private var isDragging = false

    private val LONG_PRESS_DURATION = 500L
    private val MOVE_THRESHOLD = 10

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        showFloatingBall()
    }

    override fun onDestroy() {
        removeFloatingBall()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "CCScan Floating Ball",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮球服务"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setContentTitle(getString(R.string.floating_service_notification_title))
            .setContentText(getString(R.string.floating_service_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    private fun showFloatingBall() {
        if (floatingBallView != null) return

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        floatingBallView = inflater.inflate(R.layout.floating_ball, null)

        val ball = floatingBallView?.findViewById<ImageView>(R.id.floatingBall)
        ball?.setOnTouchListener { view, event -> handleTouch(view, event) }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        ballParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 300
        }

        windowManager.addView(floatingBallView, ballParams)
    }

    private fun removeFloatingBall() {
        try {
            floatingBallView?.let {
                if (it.isAttachedToWindow) {
                    windowManager.removeView(it)
                }
            }
            floatingBallView = null
            longPressRunnable?.let { handler.removeCallbacks(it) }
        } catch (_: Exception) {
        }
    }

    private fun handleTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isLongPressTriggered = false
                isDragging = false
                initialX = ballParams?.x ?: 0
                initialY = ballParams?.y ?: 0
                touchX = event.rawX
                touchY = event.rawY

                longPressRunnable = Runnable {
                    isLongPressTriggered = true
                    triggerScan()
                }
                longPressRunnable?.let {
                    handler.postDelayed(it, LONG_PRESS_DURATION)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - touchX
                val dy = event.rawY - touchY

                if (!isDragging && (abs(dx) > MOVE_THRESHOLD || abs(dy) > MOVE_THRESHOLD)) {
                    isDragging = true
                    longPressRunnable?.let { handler.removeCallbacks(it) }
                }

                if (isDragging && !isLongPressTriggered) {
                    ballParams?.x = (initialX + dx).toInt()
                    ballParams?.y = (initialY + dy).toInt()
                    try {
                        windowManager.updateViewLayout(floatingBallView, ballParams)
                    } catch (_: Exception) {
                    }
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longPressRunnable?.let { handler.removeCallbacks(it) }
                if (!isDragging && !isLongPressTriggered) {
                    // 单击：也可以触发一次扫描（可选）
                }
                // 松开后自动贴边
                if (isDragging && !isLongPressTriggered) {
                    snapToEdge()
                }
                view.performClick()
                return true
            }
        }
        return false
    }

    private fun snapToEdge() {
        val screenWidth = windowManager.defaultDisplay.width
        ballParams?.let { params ->
            val ballCenterX = params.x + (floatingBallView?.width ?: 0) / 2
            params.x = if (ballCenterX < screenWidth / 2) {
                0
            } else {
                screenWidth - (floatingBallView?.width ?: 0)
            }
            try {
                windowManager.updateViewLayout(floatingBallView, params)
            } catch (_: Exception) {
            }
        }
    }

    private fun triggerScan() {
        val intent = Intent(this, ScanActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
    }

    companion object {
        private const val CHANNEL_ID = "ccscan_floating_ball_channel"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, FloatingBallService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingBallService::class.java)
            context.stopService(intent)
        }
    }
}

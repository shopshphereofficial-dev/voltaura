package com.voltaura.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.voltaura.app.MainActivity
import com.voltaura.app.battery.BatteryReader
import com.voltaura.app.data.Prefs
import com.voltaura.app.overlay.ChargingAnimationView
import kotlin.math.abs

class ChargingOverlayService : Service() {

    private lateinit var prefs: Prefs
    private lateinit var wm: WindowManager
    private var view: ChargingAnimationView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var receiver: BroadcastReceiver? = null
    private var hideRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()
        register()
        maybeShow()
        return START_STICKY
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(CHANNEL, "VoltAura", NotificationManager.IMPORTANCE_MIN)
        ch.setShowBadge(false)
        nm.createNotificationChannel(ch)
    }

    private fun startForegroundCompat() {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notif: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("VoltAura active")
            .setContentText("Charging animation is enabled")
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setOngoing(true)
            .setContentIntent(open)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        val type = if (Build.VERSION.SDK_INT >= 34)
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, NOTIF_ID, notif, type)
    }

    private fun register() {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> maybeShow()
                    Intent.ACTION_POWER_DISCONNECTED -> hide()
                    Intent.ACTION_BATTERY_CHANGED -> updateStats()
                }
            }
        }
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        registerReceiver(receiver, f)
    }

    private fun updateStats() {
        val v = view ?: return
        val r = BatteryReader.read(this)
        v.batteryPercent = r.levelPercent
        v.watts = r.watts
        v.amps = abs(r.currentUa) / 1_000_000.0
        v.volts = r.voltageMv / 1000.0
    }

    private fun maybeShow() {
        if (!prefs.chargingEnabled) return
        val r = BatteryReader.read(this)
        if (!r.isCharging) return
        show()
    }

    private fun show() {
        if (view != null) {
            updateStats()
            return
        }
        if (!Settings.canDrawOverlays(this)) return

        val v = ChargingAnimationView(this)
        v.style = prefs.chargingStyle
        val r = BatteryReader.read(this)
        v.batteryPercent = r.levelPercent
        v.watts = r.watts
        v.amps = abs(r.currentUa) / 1_000_000.0
        v.volts = r.voltageMv / 1000.0

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.TOP or Gravity.START
        v.setOnTouchListener { _, e ->
            if (e.action == MotionEvent.ACTION_UP) hide()
            true
        }
        try {
            wm.addView(v, lp)
            view = v
        } catch (e: Exception) {
            view = null
            return
        }
        hideRunnable = Runnable { hide() }
        handler.postDelayed(hideRunnable!!, SHOW_MS)
    }

    private fun hide() {
        hideRunnable?.let { handler.removeCallbacks(it) }
        hideRunnable = null
        val v = view ?: return
        try {
            wm.removeView(v)
        } catch (_: Exception) {
        }
        view = null
    }

    override fun onDestroy() {
        hide()
        receiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {
            }
        }
        receiver = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "voltaura_charging"
        private const val NOTIF_ID = 1001
        private const val SHOW_MS = 7000L

        fun start(ctx: Context) {
            ContextCompat.startForegroundService(ctx, Intent(ctx, ChargingOverlayService::class.java))
        }

        fun stop(ctx: Context) {
            ctx.stopService(Intent(ctx, ChargingOverlayService::class.java))
        }
    }
}

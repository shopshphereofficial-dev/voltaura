package com.voltaura.app.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.Gravity
import android.view.WindowManager
import com.voltaura.app.data.Prefs
import com.voltaura.app.overlay.EdgeGlowView

class NotificationGlowService : NotificationListenerService() {

    private lateinit var prefs: Prefs
    private var wm: WindowManager? = null
    private var view: EdgeGlowView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val s = sbn ?: return
        if (!prefs.glowEnabled) return
        if (s.packageName == packageName) return
        handler.post { showGlow() }
    }

    private fun showGlow() {
        val manager = wm ?: return
        if (!Settings.canDrawOverlays(this)) return

        if (view == null) {
            val v = EdgeGlowView(this)
            v.style = prefs.glowStyle
            v.baseColor = prefs.glowColor
            v.strokeWidthPx = prefs.glowWidth.toFloat()

            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            lp.gravity = Gravity.TOP or Gravity.START
            try {
                manager.addView(v, lp)
                view = v
            } catch (_: Exception) {
                return
            }
        } else {
            view?.style = prefs.glowStyle
            view?.baseColor = prefs.glowColor
            view?.strokeWidthPx = prefs.glowWidth.toFloat()
        }

        hideRunnable?.let { handler.removeCallbacks(it) }
        hideRunnable = Runnable { hideGlow() }
        handler.postDelayed(hideRunnable!!, prefs.glowDuration.toLong())
    }

    private fun hideGlow() {
        val v = view ?: return
        try {
            wm?.removeView(v)
        } catch (_: Exception) {
        }
        view = null
    }

    override fun onDestroy() {
        hideGlow()
        super.onDestroy()
    }
}

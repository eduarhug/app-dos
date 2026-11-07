package com.pantallanegra.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager

class OverlayService : Service() {

    companion object {
        const val ACTION_STOP = "com.pantallanegra.app.STOP"
        private const val CHANNEL_ID = "pantalla_negra"
        private const val NOTIF_ID = 1

        @Volatile
        var running = false
    }

    private lateinit var wm: WindowManager
    private var button: FloatButton? = null
    private var buttonParams: WindowManager.LayoutParams? = null
    private var blackView: BlackView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!running) {
            startAsForeground()
            createButton()
            running = true
        }
        return START_NOT_STICKY
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Pantalla Negra", NotificationManager.IMPORTANCE_LOW)
        )
        val stop = PendingIntent.getService(
            this, 0,
            Intent(this, OverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        @Suppress("DEPRECATION")
        val notif = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Pantalla Negra activa")
            .setContentText("Toca el botón flotante para poner la pantalla en negro")
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(0, "Detener", stop).build())
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    private fun createButton() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val size = dp(52)
        val p = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        p.gravity = Gravity.TOP or Gravity.START
        p.x = dp(12)
        p.y = dp(220)
        buttonParams = p
        val b = FloatButton(this, wm, p) { toggleBlack() }
        button = b
        wm.addView(b, p)
    }

    private fun toggleBlack() {
        if (blackView == null) showBlack() else hideBlack()
    }

    private fun showBlack() {
        val v = BlackView(this)
        val p = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        )
        p.gravity = Gravity.TOP or Gravity.START
        if (Build.VERSION.SDK_INT >= 30) {
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= 28) {
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        wm.addView(v, p)
        blackView = v

        // Volver a poner el botón por encima de la capa negra
        val b = button
        val bp = buttonParams
        if (b != null && bp != null) {
            wm.removeView(b)
            wm.addView(b, bp)
            b.active = true
        }
    }

    private fun hideBlack() {
        blackView?.let {
            wm.removeView(it)
            it.release()
        }
        blackView = null
        button?.active = false
    }

    override fun onDestroy() {
        running = false
        blackView?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
            it.release()
        }
        blackView = null
        button?.let {
            try { wm.removeView(it) } catch (_: Exception) {}
        }
        button = null
        super.onDestroy()
    }
}

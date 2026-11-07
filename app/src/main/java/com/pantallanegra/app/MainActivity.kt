package com.pantallanegra.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var button: Button

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#101010"))
            setPadding(dp(28), dp(28), dp(28), dp(28))
        }

        val title = TextView(this).apply {
            text = "Pantalla Negra"
            textSize = 28f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        val desc = TextView(this).apply {
            text = "Pone una capa negra sobre cualquier app para ahorrar batería mientras " +
                "escuchas un video o música.\n\n" +
                "1. Activa el botón flotante.\n" +
                "2. Abre tu video y toca el botón para poner la pantalla en negro.\n" +
                "3. Desliza el dedo para ver una zona (por ejemplo una notificación): " +
                "a los 4 segundos vuelve a negro.\n" +
                "4. Toca el botón otra vez para quitar el negro."
            textSize = 15f
            setTextColor(Color.parseColor("#BBBBBB"))
            setPadding(0, dp(18), 0, dp(24))
        }
        status = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#7FD1FF"))
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        }
        button = Button(this).apply {
            setOnClickListener { onMainButton() }
        }

        root.addView(title)
        root.addView(desc)
        root.addView(status)
        root.addView(button)
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val hasPerm = Settings.canDrawOverlays(this)
        when {
            !hasPerm -> {
                status.text = "Falta el permiso \"Mostrar sobre otras apps\""
                button.text = "Dar permiso"
            }
            OverlayService.running -> {
                status.text = "Botón flotante activo"
                button.text = "Detener"
            }
            else -> {
                status.text = "Listo"
                button.text = "Activar botón flotante"
            }
        }
    }

    private fun onMainButton() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Activa \"Permitir sobre otras apps\" y vuelve", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
            return
        }
        if (OverlayService.running) {
            stopService(Intent(this, OverlayService::class.java))
            Handler(Looper.getMainLooper()).postDelayed({ refresh() }, 300)
        } else {
            if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
            startForegroundService(Intent(this, OverlayService::class.java))
            Handler(Looper.getMainLooper()).postDelayed({
                refresh()
                moveTaskToBack(true)
            }, 400)
        }
    }
}

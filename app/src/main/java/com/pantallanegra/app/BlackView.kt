package com.pantallanegra.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View

/**
 * Capa negra a pantalla completa.
 * Al deslizar el dedo se "borra" el negro y se ve lo que hay debajo.
 * A los 4 segundos de soltar el dedo, vuelve a negro con un suave desvanecimiento.
 */
class BlackView(context: Context) : View(context) {

    companion object {
        const val REVERT_DELAY_MS = 4000L
        const val BRUSH_DP = 80f
        private const val FADE_FRAMES = 30
        private const val FADE_FRAME_MS = 16L
    }

    private var bitmap: Bitmap? = null
    private var canvasBmp: Canvas? = null

    private val density = resources.displayMetrics.density

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = BRUSH_DP * density
    }

    private val fadePaint = Paint().apply { color = Color.argb(45, 0, 0, 0) }

    private val handler = Handler(Looper.getMainLooper())
    private var lastX = 0f
    private var lastY = 0f
    private var framesLeft = 0

    private val revertRunnable = Runnable {
        framesLeft = FADE_FRAMES
        handler.post(fadeRunnable)
    }

    private val fadeRunnable = object : Runnable {
        override fun run() {
            val c = canvasBmp ?: return
            c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), fadePaint)
            invalidate()
            framesLeft--
            if (framesLeft > 0) {
                handler.postDelayed(this, FADE_FRAME_MS)
            } else {
                c.drawColor(Color.BLACK)
                invalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return
        bitmap?.recycle()
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap = bmp
        canvasBmp = Canvas(bmp).also { it.drawColor(Color.BLACK) }
    }

    override fun onDraw(canvas: Canvas) {
        bitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val c = canvasBmp ?: return true
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                handler.removeCallbacks(revertRunnable)
                handler.removeCallbacks(fadeRunnable)
                lastX = e.x
                lastY = e.y
                c.drawPoint(lastX, lastY, clearPaint)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                c.drawLine(lastX, lastY, e.x, e.y, clearPaint)
                lastX = e.x
                lastY = e.y
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(revertRunnable)
                handler.postDelayed(revertRunnable, REVERT_DELAY_MS)
            }
        }
        return true
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        bitmap?.recycle()
        bitmap = null
        canvasBmp = null
    }
}

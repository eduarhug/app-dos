package com.pantallanegra.app

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import kotlin.math.abs

/** Botón flotante redondo (estilo "AssistiveTouch"). Se arrastra y un toque activa/desactiva el negro. */
class FloatButton(
    context: Context,
    private val wm: WindowManager,
    private val params: WindowManager.LayoutParams,
    private val onTap: () -> Unit
) : View(context) {

    var active: Boolean = false
        set(value) {
            field = value
            alpha = if (value) 0.35f else 0.9f
            invalidate()
        }

    private val density = resources.displayMetrics.density
    private val slop = ViewConfiguration.get(context).scaledTouchSlop

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }

    private var downRawX = 0f
    private var downRawY = 0f
    private var startX = 0
    private var startY = 0
    private var dragging = false

    init {
        alpha = 0.9f
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val r = width / 2f - 2f * density
        fill.color = if (active) 0xFF2A2A2A.toInt() else 0xFF1C1C1E.toInt()
        ring.color = if (active) 0xFF8E8E93.toInt() else 0xFFFFFFFF.toInt()
        canvas.drawCircle(cx, cy, r, fill)
        canvas.drawCircle(cx, cy, r, ring)
        canvas.drawCircle(cx, cy, r * 0.5f, ring)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = e.rawX
                downRawY = e.rawY
                startX = params.x
                startY = params.y
                dragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = e.rawX - downRawX
                val dy = e.rawY - downRawY
                if (!dragging && (abs(dx) > slop || abs(dy) > slop)) dragging = true
                if (dragging) {
                    params.x = startX + dx.toInt()
                    params.y = startY + dy.toInt()
                    wm.updateViewLayout(this, params)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!dragging) onTap()
            }
        }
        return true
    }
}

package com.voltaura.app.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import kotlin.math.sin

class EdgeGlowView(context: Context) : View(context) {

    var style: Int = 0
    var baseColor: Int = Color.rgb(0, 229, 255)
    var strokeWidthPx: Float = 26f

    private val start = SystemClock.uptimeMillis()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun t(): Float = (SystemClock.uptimeMillis() - start) / 1000f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val tt = t()
        val inset = strokeWidthPx / 2f
        val rect = RectF(inset, inset, w - inset, h - inset)
        val radius = 48f
        val mode = ((style % 5) + 5) % 5

        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.shader = null

        val col = when (mode) {
            0 -> baseColor
            1 -> rainbow(tt)
            2 -> blend(baseColor, Color.rgb(124, 77, 255), 0.5f + 0.5f * sin(tt * 2f))
            3 -> baseColor
            else -> rainbow(tt + 0.3f)
        }

        for (i in 5 downTo 1) {
            paint.color = col
            paint.alpha = (12 * (6 - i)).coerceIn(0, 255)
            paint.strokeWidth = strokeWidthPx * (i / 2.5f)
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
        paint.color = col
        paint.alpha = 255
        paint.strokeWidth = strokeWidthPx
        canvas.drawRoundRect(rect, radius, radius, paint)

        if (mode == 3) {
            paint.color = col
            paint.alpha = (120 + 120 * sin(tt * 6f)).toInt().coerceIn(0, 255)
            paint.strokeWidth = strokeWidthPx * 1.6f
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
        postInvalidateOnAnimation()
    }

    private fun rainbow(t: Float): Int {
        val hue = (t * 120f) % 360f
        return Color.HSVToColor(floatArrayOf(hue, 0.9f, 1f))
    }

    private fun blend(a: Int, b: Int, f: Float): Int {
        val ff = f.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * ff).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * ff).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * ff).toInt()
        )
    }
}

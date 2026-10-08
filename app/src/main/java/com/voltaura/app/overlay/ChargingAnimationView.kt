package com.voltaura.app.overlay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.SystemClock
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ChargingAnimationView(context: Context) : View(context) {

    var style: Int = 0
    var batteryPercent: Int = -1
    var watts: Double = 0.0
    var amps: Double = 0.0
    var volts: Double = 0.0

    private val start = SystemClock.uptimeMillis()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
    }
    private val bg = Paint().apply { color = 0xE6070A16.toInt() }

    private fun t(): Float = (SystemClock.uptimeMillis() - start) / 1000f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f
        val tt = t()
        val base = min(w, h)

        canvas.drawRect(0f, 0f, w, h, bg)

        when (((style % 6) + 6) % 6) {
            0 -> drawNeonRing(canvas, cx, cy, base, tt)
            1 -> drawLiquid(canvas, cx, cy, base, tt)
            2 -> drawParticles(canvas, cx, cy, base, tt)
            3 -> drawBolts(canvas, cx, cy, base, tt)
            4 -> drawGradientPulse(canvas, cx, cy, base, tt)
            else -> drawBars(canvas, cx, cy, base, tt)
        }

        drawCenterText(canvas, cx, cy, base)
        postInvalidateOnAnimation()
    }

    private fun drawBolt(c: Canvas, cx: Float, cy: Float, s: Float) {
        val pts = floatArrayOf(
            0.20f, -0.55f, -0.18f, 0.05f, 0.02f, 0.05f, -0.14f, 0.55f, 0.22f, -0.02f, 0.04f, -0.02f
        )
        val p = Path()
        p.moveTo(cx + pts[0] * s, cy + pts[1] * s)
        var i = 2
        while (i < pts.size) {
            p.lineTo(cx + pts[i] * s, cy + pts[i + 1] * s)
            i += 2
        }
        p.close()
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = Color.rgb(0, 229, 255)
        c.drawPath(p, paint)
    }

    private fun drawNeonRing(c: Canvas, cx: Float, cy: Float, base: Float, t: Float) {
        val r = base * 0.28f
        val pulse = 1f + 0.06f * sin(t * 3f)
        val sweep = (t * 90f) % 360f
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        for (i in 4 downTo 1) {
            paint.strokeWidth = r * 0.12f * i
            paint.color = Color.argb(12 * (5 - i), 0, 229, 255)
            c.drawCircle(cx, cy, r * pulse, paint)
        }
        paint.strokeWidth = r * 0.12f
        paint.color = Color.argb(70, 0, 229, 255)
        c.drawCircle(cx, cy, r * pulse, paint)

        val rect = RectF(cx - r * pulse, cy - r * pulse, cx + r * pulse, cy + r * pulse)
        paint.strokeWidth = r * 0.14f
        paint.color = Color.rgb(0, 229, 255)
        c.drawArc(rect, sweep, 120f, false, paint)
        paint.color = Color.rgb(124, 77, 255)
        c.drawArc(rect, sweep + 180f, 120f, false, paint)
        drawBolt(c, cx, cy, r * 0.75f)
    }

    private fun drawLiquid(c: Canvas, cx: Float, cy: Float, base: Float, t: Float) {
        val r = base * 0.30f
        paint.style = Paint.Style.STROKE
        paint.shader = null
        paint.strokeWidth = r * 0.06f
        paint.color = Color.argb(90, 0, 229, 255)
        c.drawCircle(cx, cy, r, paint)

        c.save()
        val clip = Path().apply { addCircle(cx, cy, r * 0.95f, Path.Direction.CW) }
        c.clipPath(clip)
        val pct = if (batteryPercent in 0..100) batteryPercent else 50
        val level = cy + r - (2 * r) * (pct / 100f)
        val wave = r * 0.06f
        val path = Path()
        path.moveTo(cx - r, level)
        var x = cx - r
        while (x <= cx + r) {
            val y = level + wave * sin((x / r) * 3f + t * 4f)
            path.lineTo(x, y)
            x += r / 20f
        }
        path.lineTo(cx + r, cy + r)
        path.lineTo(cx - r, cy + r)
        path.close()
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            cx - r, level, cx + r, cy + r,
            Color.rgb(0, 229, 255), Color.rgb(124, 77, 255), Shader.TileMode.CLAMP
        )
        c.drawPath(path, paint)
        paint.shader = null
        c.restore()
    }

    private fun drawParticles(c: Canvas, cx: Float, cy: Float, base: Float, t: Float) {
        val r = base * 0.30f
        paint.style = Paint.Style.STROKE
        paint.shader = null
        paint.strokeWidth = 3f
        paint.color = Color.argb(70, 124, 77, 255)
        c.drawCircle(cx, cy, r, paint)

        paint.style = Paint.Style.FILL
        val n = 24
        for (i in 0 until n) {
            val a = (i / n.toFloat()) * 6.28318f + t * 0.8f
            val rr = r * (0.7f + 0.3f * sin(t * 2f + i))
            val x = cx + cos(a) * rr
            val y = cy + sin(a) * rr
            val size = 4f + 6f * (0.5f + 0.5f * sin(t * 3f + i))
            val col = if (i % 2 == 0) Color.rgb(0, 229, 255) else Color.rgb(255, 77, 166)
            paint.color = Color.argb(190, Color.red(col), Color.green(col), Color.blue(col))
            c.drawCircle(x, y, size, paint)
        }
        drawBolt(c, cx, cy, r * 0.7f)
    }

    private fun drawBolts(c: Canvas, cx: Float, cy: Float, base: Float, t: Float) {
        val r = base * 0.32f
        val n = 8
        for (i in 0 until n) {
            val a = (i / n.toFloat()) * 6.28318f + t * 0.6f
            val len = r * (0.85f + 0.15f * sin(t * 4f + i))
            val x2 = cx + cos(a) * len
            val y2 = cy + sin(a) * len
            val col = if (i % 2 == 0) Color.rgb(0, 229, 255) else Color.rgb(124, 77, 255)
            paint.shader = null
            paint.color = Color.argb(200, Color.red(col), Color.green(col), Color.blue(col))
            paint.strokeWidth = 6f
            paint.style = Paint.Style.STROKE
            c.drawLine(cx + cos(a) * r * 0.35f, cy + sin(a) * r * 0.35f, x2, y2, paint)
            paint.style = Paint.Style.FILL
            c.drawCircle(x2, y2, 6f, paint)
        }
        drawBolt(c, cx, cy, r * 0.6f)
    }

    private fun drawGradientPulse(c: Canvas, cx: Float, cy: Float, base: Float, t: Float) {
        val maxR = base * 0.42f
        paint.style = Paint.Style.STROKE
        paint.shader = null
        paint.strokeWidth = 8f
        for (k in 0 until 4) {
            val phase = ((t * 0.6f) + k / 4f) % 1f
            val rr = maxR * phase
            val alpha = (255 * (1f - phase)).toInt().coerceIn(0, 255)
            paint.color = Color.argb(alpha, 0, 229, 255)
            c.drawCircle(cx, cy, rr, paint)
        }
        drawBolt(c, cx, cy, maxR * 0.5f)
    }

    private fun drawBars(c: Canvas, cx: Float, cy: Float, base: Float, t: Float) {
        val n = 9
        val bw = base * 0.03f
        val gap = bw * 0.7f
        val total = n * bw + (n - 1) * gap
        var x = cx - total / 2f
        paint.style = Paint.Style.FILL
        paint.shader = null
        for (i in 0 until n) {
            val hgt = base * 0.10f + base * 0.14f * (0.5f + 0.5f * sin(t * 4f + i * 0.7f))
            val col = if (i % 2 == 0) Color.rgb(0, 229, 255) else Color.rgb(124, 77, 255)
            paint.color = col
            val rect = RectF(x, cy - hgt / 2f, x + bw, cy + hgt / 2f)
            c.drawRoundRect(rect, bw / 2f, bw / 2f, paint)
            x += bw + gap
        }
    }

    private fun drawCenterText(c: Canvas, cx: Float, cy: Float, base: Float) {
        textPaint.textSize = base * 0.11f
        textPaint.color = Color.WHITE
        val pctText = if (batteryPercent in 0..100) "$batteryPercent%" else "--%"
        c.drawText(pctText, cx, cy + base * 0.24f, textPaint)
        textPaint.textSize = base * 0.034f
        textPaint.color = Color.argb(210, 154, 166, 199)
        val info = String.format("%.1fW   %.2fA   %.1fV", watts, amps, volts)
        c.drawText(info, cx, cy + base * 0.31f, textPaint)
    }
}

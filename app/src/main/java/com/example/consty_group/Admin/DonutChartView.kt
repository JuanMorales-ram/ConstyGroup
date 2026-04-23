package com.example.consty_group.admin

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.consty_group.R

/**
 * DonutChartView
 * ──────────────
 * Vista circular de progreso tipo "donut" para el panel de administrador.
 * Soporta animación de entrada con ValueAnimator.
 */
class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var progress      = 0f   // valor animado (0..maxValue)
    private var targetProgress = 0
    private var maxValue      = 100
    private var centerText    = ""

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        strokeCap   = Paint.Cap.ROUND
        color       = ContextCompat.getColor(context, R.color.progressBarVacia)
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style       = Paint.Style.STROKE
        strokeCap   = Paint.Cap.ROUND
        color       = ContextCompat.getColor(context, R.color._2dd4bf_)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = ContextCompat.getColor(context, R.color._2dd4bf_)
        textAlign   = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val oval = RectF()

    // ── API pública ────────────────────────────────────────────────────
    fun setProgress(value: Int, max: Int, animate: Boolean = true) {
        targetProgress = value
        maxValue       = max
        if (animate) {
            ValueAnimator.ofFloat(0f, value.toFloat()).apply {
                duration     = 1000
                interpolator = DecelerateInterpolator()
                addUpdateListener {
                    progress = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else {
            progress = value.toFloat()
            invalidate()
        }
    }

    fun setColors(trackColor: Int, progressColor: Int) {
        trackPaint.color    = trackColor
        progressPaint.color = progressColor
        textPaint.color     = progressColor
        invalidate()
    }

    fun setCenterText(text: String) {
        centerText = text
        invalidate()
    }

    // ── Dibujo ─────────────────────────────────────────────────────────
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val stroke  = width * 0.12f
        val padding = stroke / 2f

        trackPaint.strokeWidth    = stroke
        progressPaint.strokeWidth = stroke
        textPaint.textSize        = width * 0.22f

        oval.set(padding, padding, width - padding, height - padding)

        // Track completo
        canvas.drawArc(oval, 0f, 360f, false, trackPaint)

        // Arco de progreso (empieza desde las 12, horario)
        val sweep = (progress / maxValue.toFloat()) * 360f
        canvas.drawArc(oval, -90f, sweep, false, progressPaint)

        // Texto central
        if (centerText.isNotEmpty()) {
            val x = width / 2f
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(centerText, x, y, textPaint)
        }
    }
}
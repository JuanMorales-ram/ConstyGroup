package com.example.consty_group.admin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * DonutChartView
 * ──────────────
 * Vista circular de progreso tipo "donut" para el panel de administrador.
 * Archivo nuevo — no modifica nada existente.
 */
class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var progress   = 0
    private var maxValue   = 100
    private var centerText = ""

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style     = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color     = Color.parseColor("#1E1E3A")
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style     = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color     = Color.parseColor("#2DD4BF")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = Color.parseColor("#2DD4BF")
        textAlign = Paint.Align.CENTER
        textSize  = 32f
    }

    private val oval = RectF()

    fun setProgress(value: Int, max: Int) {
        progress = value
        maxValue = max
        invalidate()
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val stroke    = width * 0.12f
        val padding   = stroke / 2f
        trackPaint.strokeWidth    = stroke
        progressPaint.strokeWidth = stroke

        oval.set(padding, padding, width - padding, height - padding)

        // Fondo del donut
        canvas.drawArc(oval, 0f, 360f, false, trackPaint)

        // Arco de progreso (empieza desde arriba, sentido horario)
        val sweep = (progress.toFloat() / maxValue.toFloat()) * 360f
        canvas.drawArc(oval, -90f, sweep, false, progressPaint)

        // Texto central
        if (centerText.isNotEmpty()) {
            val x = width / 2f
            val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(centerText, x, y, textPaint)
        }
    }
}

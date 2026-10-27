package com.example.attentrack

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.attentrack.R
import kotlin.math.round

/** Simple bar chart (0-100%). Bar er rong: threshold er niche = lal, 85 er niche = holud, nahole shobuj. */
class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    data class Bar(val label: String, val value: Double?)

    private var bars: List<Bar> = emptyList()
    private var threshold = 75.0

    init {
        if (isInEditMode) {
            bars = listOf(
                Bar("C1", 85.0),
                Bar("C2", 65.0),
                Bar("C3", 92.0),
                Bar("C4", 78.0),
                Bar("C5", 88.0)
            )
        }
    }

    private val density = resources.displayMetrics.density
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = ContextCompat.getColor(context, R.color.divider)
        pathEffect = DashPathEffect(floatArrayOf(4f * density, 4f * density), 0f)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        color = ContextCompat.getColor(context, R.color.text_secondary)
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f * resources.displayMetrics.scaledDensity
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        color = ContextCompat.getColor(context, R.color.text_primary)
    }
    private val rect = RectF()

    fun setData(bars: List<Bar>, threshold: Double = 75.0) {
        this.bars = bars
        this.threshold = threshold
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = resolveSize((190f * density).toInt(), heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (bars.isEmpty()) {
            labelPaint.textSize = 13f * resources.displayMetrics.scaledDensity
            canvas.drawText("No data yet", w / 2f, h / 2f, labelPaint)
            labelPaint.textSize = 10f * resources.displayMetrics.scaledDensity
            return
        }

        val left = 8f * density
        val right = w - 8f * density
        val top = 20f * density
        val bottom = h - 24f * density
        val chartH = bottom - top
        val slot = (right - left) / bars.size
        val barW = minOf(slot * 0.62f, 30f * density)

        // threshold line
        val ty = bottom - chartH * (threshold / 100.0).toFloat()
        canvas.drawLine(left, ty, right, ty, gridPaint)

        val showValues = bars.size <= 9
        bars.forEachIndexed { i, bar ->
            val cx = left + slot * i + slot / 2f
            val value = bar.value
            if (value != null) {
                val colorRes = when {
                    value < threshold -> R.color.danger
                    value < 85.0 -> R.color.warning
                    else -> R.color.success
                }
                barPaint.color = ContextCompat.getColor(context, colorRes)
                val barH = maxOf(chartH * (value / 100.0).toFloat(), 3f * density)
                rect.set(cx - barW / 2f, bottom - barH, cx + barW / 2f, bottom)
                canvas.drawRoundRect(rect, 6f * density, 6f * density, barPaint)
                if (showValues) {
                    val text = round(value).toInt().toString()
                    canvas.drawText(text, cx, bottom - barH - 5f * density, valuePaint)
                }
            }
            canvas.drawText(bar.label, cx, h - 7f * density, labelPaint)
        }
    }
}

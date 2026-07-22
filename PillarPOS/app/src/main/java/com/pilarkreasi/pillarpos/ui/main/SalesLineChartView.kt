package com.pilarkreasi.pillarpos.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View


class SalesLineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var labels: List<String> = emptyList()
    private var values: List<Double> = emptyList()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4E342E") 
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFB300") 
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BDBDBD")
        strokeWidth = 2f
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 24f
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#9E9E9E")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    fun setData(labels: List<String>, values: List<Double>) {
        this.labels = labels
        this.values = values
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val paddingLeft = 16f
        val paddingRight = 16f
        val paddingTop = 24f
        val paddingBottom = 48f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        if (values.isEmpty() || values.all { it == 0.0 }) {
            canvas.drawText("Belum ada data penjualan", width / 2f, height / 2f, emptyPaint)
            return
        }

        val maxValue = (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)

        
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = paddingTop + chartHeight * i / gridLines
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)
        }

        
        val path = Path()
        val stepX = if (values.size > 1) chartWidth / (values.size - 1) else 0f
        val points = mutableListOf<Pair<Float, Float>>()

        values.forEachIndexed { index, value ->
            val x = paddingLeft + stepX * index
            val y = paddingTop + chartHeight - (value / maxValue * chartHeight).toFloat()
            points.add(x to y)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        canvas.drawPath(path, linePaint)

        points.forEach { (x, y) -> canvas.drawCircle(x, y, 8f, pointPaint) }

        
        val maxLabelsShown = 6
        val labelStep = (labels.size / maxLabelsShown).coerceAtLeast(1)
        labels.forEachIndexed { index, label ->
            if (index % labelStep == 0 && index < points.size) {
                val (x, _) = points[index]
                canvas.drawText(label, x, height - 12f, labelPaint)
            }
        }
    }
}

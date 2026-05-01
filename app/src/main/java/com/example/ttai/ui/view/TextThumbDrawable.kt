package com.example.ttai.ui.view

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.PixelFormat
import androidx.core.graphics.toColorInt

class TextThumbDrawable(private var text: String) : Drawable() {
    private val paint = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    private val bgPaint = Paint().apply {
        color = "#585858".toColorInt()
        isAntiAlias = true
    }

    fun updateText(newText: String) {
        text = newText
        invalidateSelf() // Trigger redraw
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val centerX = bounds.centerX().toFloat()
        val centerY = bounds.centerY().toFloat()
        val radius = (bounds.width() / 2).coerceAtMost(bounds.height() / 2).toFloat()

        // Draw circular background
        canvas.drawCircle(centerX, centerY, radius, bgPaint)

        // Draw text
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        canvas.drawText(text, centerX, centerY + textBounds.height() / 2, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        bgPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        paint.colorFilter = colorFilter
        bgPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = 80 // Adjust size as needed
    override fun getIntrinsicHeight(): Int = 80 // Adjust size as needed
}
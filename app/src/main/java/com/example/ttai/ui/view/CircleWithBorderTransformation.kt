package com.example.ttai.ui.view

import android.graphics.*
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class CircleWithBorderTransformation(private val borderWidth: Float, private val borderColor: Int) : BitmapTransformation() {

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val size = Math.min(toTransform.width, toTransform.height)
        val x = (toTransform.width - size) / 2
        val y = (toTransform.height - size) / 2

        val bitmap = pool.get(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // 绘制原始图片圆形
        val shader = BitmapShader(toTransform, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        if (x != 0 || y != 0) {
            val matrix = Matrix()
            matrix.setTranslate((-x).toFloat(), (-y).toFloat())
            shader.setLocalMatrix(matrix)
        }
        paint.shader = shader
        paint.isAntiAlias = true

        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)

        // 绘制边框
        val borderPaint = Paint()
        borderPaint.style = Paint.Style.STROKE
        borderPaint.color = borderColor
        borderPaint.strokeWidth = borderWidth
        borderPaint.isAntiAlias = true

        // 边框的半径要减去边框宽度的一半，防止被切掉
        canvas.drawCircle(r, r, r - borderWidth / 2, borderPaint)

        return bitmap
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("circle_border" + borderWidth + borderColor).toByteArray())
    }
}
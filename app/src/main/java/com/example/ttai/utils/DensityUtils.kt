package com.example.ttai.utils
import android.content.res.Resources
import android.util.TypedValue

object DensityUtils {
    /**
     * 将 dp 转换为像素 (px)
     */
    fun dpToPx(dp: Float): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    /**
     * 将像素 (px) 转换为 dp
     */
    fun pxToDp(px: Float): Int {
        return (px / Resources.getSystem().displayMetrics.density).toInt()
    }

    /**
     * 将 sp 转换为像素 (px)
     */
    fun spToPx(sp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    /**
     * 将像素 (px) 转换为 sp
     */
    fun pxToSp(px: Float): Int {
        return (px / Resources.getSystem().displayMetrics.scaledDensity).toInt()
    }
}
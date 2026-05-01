package com.example.ttai.utils

import android.content.Context

class KTXUtils {
}

// 扩展函数：dp 转 px
public fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}
fun Int.formatPopularity(): String {
    return when {
        this >= 1000 -> {
            // 将数字除以 1000，并保留一位小数
            val shortened = this / 1000.0
            "%.1fk".format(shortened)
        }
        else -> {
            // 不超过 1000，转为整数显示（根据需要也可以保留小数）
            this.toInt().toString()
        }
    }
}
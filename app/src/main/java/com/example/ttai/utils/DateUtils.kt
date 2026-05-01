package com.example.ttai.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/**
 * 日期工具类，用于获取当前年份和月份。
 * 注意：此工具类适用于 Android API 26+，如果需要支持更低版本，可考虑使用 ThreeTenABP 库。
 */
object DateUtils {

    /**
     * 获取当前年份。
     * @return 当前年份的整数值，例如 2025。
     */
    fun getCurrentYear(): Int {
        return LocalDate.now().year
    }

    /**
     * 获取当前月份（1-12）。
     * @return 当前月份的整数值，例如 11（代表 November）。
     */
    fun getCurrentMonth(): Int {
        return LocalDate.now().monthValue
    }


    /**
     * 将 Unix 时间戳（秒）转换为指定格式的日期时间字符串。
     * * @param timestampSeconds 以秒为单位的时间戳，例如: 1762190190
     * @param formatString 目标日期格式，例如 "MM-dd HH:mm"
     * @param locale 地区设置，推荐使用 Locale.getDefault() 或 Locale.CHINA
     * @return 格式化后的日期字符串，如果输入无效则返回空字符串
     */
    fun formatTimestamp(
        timestampSeconds: Long,
        formatString: String = "MM-dd HH:mm", // 默认格式为 月-日 时:分
        locale: Locale = Locale.getDefault() // 使用系统默认地区
    ): String {
        // 1. Unix 时间戳通常以秒为单位，Java/Kotlin 的 Date 需要毫秒，所以需要乘以 1000
        val timestampMillis = timestampSeconds * 1000L

        // 2. 创建一个 Date 对象
        val date = Date(timestampMillis)

        // 3. 创建 SimpleDateFormat 对象，并设置格式和地区
        val sdf = SimpleDateFormat(formatString, locale)

        // 4. 格式化并返回结果
        return sdf.format(date)
    }
}
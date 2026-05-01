package com.example.ttai.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.UUID

/**
 * 设备工具类，用于获取设备相关信息
 */
object DeviceUtils {
    /**
     * 获取设备唯一标识符
     * 在实际应用中，应该使用更可靠的方法获取设备ID
     */
    fun getDeviceId(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            // 9774d56d682e549c 是某些设备的默认ANDROID_ID，需要避免使用
            return androidId
        }
        // 使用设备型号、制造商和序列号生成一个唯一ID
        val deviceInfo = "${Build.MANUFACTURER}_${Build.MODEL}_${Build.SERIAL}"
        return UUID.nameUUIDFromBytes(deviceInfo.toByteArray()).toString()
    }
}
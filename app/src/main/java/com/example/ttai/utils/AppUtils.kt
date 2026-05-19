package com.example.ttai.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object AppUtils {
    /**
     * 获取本地当前的 VersionCode (Long)
     */
    fun getLocalVersionCode(context: Context): Long {
        return try {
            val packageManager = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)).longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(context.packageName, 0).versionCode.toLong()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            1L // 发生异常时的兜底版本号
        }
    }
}
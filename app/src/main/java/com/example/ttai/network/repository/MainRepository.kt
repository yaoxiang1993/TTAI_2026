package com.example.ttai.network.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.ttai.bean.AppUpdateInfo
import com.example.ttai.bean.ConversationsData
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.NetworkModule

class MainRepository(private val context: Context) {
    private val apiService: ApiService = NetworkModule.createService()

    // 获取版本名称 (例如: "1.2.0")
    val versionName: String
        get() = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }

    // 获取版本号 (例如: 120)
    // 获取版本号 (Int 类型，例如: 120)
    val versionCode: Int
        get() = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // longVersionCode 是 Long，我们强制转为 Int
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (e: Exception) {
            -1
        }

    suspend fun getConversations(page: Int = 1, limit: Int = 20): ConversationsData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取对话列表",
            apiCall = { apiService.getConversations() }
        )
    }
    suspend fun getVersionCheck(): AppUpdateInfo {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取对话列表",
            apiCall = { apiService.getVersionCheck("android",versionCode,versionName) }
        )
    }

}
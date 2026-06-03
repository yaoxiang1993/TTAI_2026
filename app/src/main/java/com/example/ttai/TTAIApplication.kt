package com.example.ttai

import android.app.Application
import com.example.ttai.manager.BluetoothDeviceManager
import com.example.ttai.network.NetworkModule
import com.example.ttai.utils.ApiSettings
import com.example.ttai.utils.DebugUtils
import com.example.ttai.utils.EnvironmentConfig
import com.example.ttai.utils.MMKVUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * TTAI应用主类
 * 负责初始化应用级别的配置
 */
class TTAIApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
        
        // 初始化环境配置
        EnvironmentConfig.initialize()
        
        // 初始化MMKV
        MMKVUtils.initialize(this)
        
        // 初始化API设置
        ApiSettings.init(
            autoShowErrorMessages = true,  // 启用自动错误提示
            autoShowSuccessMessages = false  // 禁用自动成功消息（避免过多提示）
        )
        
        // 恢复认证token
        restoreAuthToken()
        
        // 打印当前环境信息
        DebugUtils.logCurrentEnvironment()
        
        // 可以在这里添加其他初始化代码
        // 例如：初始化日志、崩溃报告、性能监控等

        // 初始化全局蓝牙设备管理器
        BluetoothDeviceManager.initialize(this)
    }
    
    /**
     * 从MMKV中恢复认证token
     */
    private fun restoreAuthToken() {
        val savedToken = MMKVUtils.getString(MMKVUtils.Keys.USER_TOKEN)
        if (savedToken.isNotEmpty()) {
            NetworkModule.setAuthToken(savedToken)
            DebugUtils.logInfo("TTAIApplication", "已恢复认证token: ${savedToken.take(10)}...")
        } else {
            DebugUtils.logInfo("TTAIApplication", "未找到保存的认证token")
        }
    }
} 
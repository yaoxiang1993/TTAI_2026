package com.example.ttai.utils

import android.util.Log
import com.example.ttai.utils.NetworkLogConfig.sensitiveFields
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.*

/**
 * 调试工具类
 * 用于开发时的调试功能
 */
object DebugUtils {
    
    private const val TAG = "TTAI_Debug"
    private const val NETWORK_TAG = "TTAI_Network"
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * 打印当前环境信息
     */
    fun logCurrentEnvironment() {
        val envName = EnvironmentConfig.getCurrentEnvironmentName()
        Log.i(TAG, "当前环境: $envName")
        
        // 打印网络配置信息
        Log.i(TAG, "网络配置已初始化")
        
        // 配置日志
        NetworkLogConfig.configureByEnvironment()
    }
    
    /**
     * 切换环境并打印日志
     */
    fun toggleEnvironmentWithLog() {
        val oldEnv = EnvironmentConfig.getCurrentEnvironmentName()
        EnvironmentConfig.toggleEnvironment()
        val newEnv = EnvironmentConfig.getCurrentEnvironmentName()
        
        Log.i(TAG, "环境已切换: $oldEnv -> $newEnv")
        
        // 重新配置日志
        NetworkLogConfig.configureByEnvironment()
    }
    
    /**
     * 设置为测试环境并打印日志
     */
    fun setTestEnvironmentWithLog() {
        EnvironmentConfig.setTestEnvironment()
        Log.i(TAG, "已设置为测试环境")
        NetworkLogConfig.configureByEnvironment()
    }
    
    /**
     * 设置为生产环境并打印日志
     */
    fun setProductionEnvironmentWithLog() {
        EnvironmentConfig.setProductionEnvironment()
        Log.i(TAG, "已设置为生产环境")
        NetworkLogConfig.configureByEnvironment()
    }
    
    /**
     * 打印网络请求信息
     */
    fun logNetworkRequest(url: String, method: String) {
        if (!NetworkLogConfig.isNetworkLogEnabled) return
        Log.d(NETWORK_TAG, "网络请求: $method $url")
    }
    
    /**
     * 打印详细的网络请求信息
     */
    fun logNetworkRequest(url: String, method: String, headers: String, body: String) {
        if (!NetworkLogConfig.isNetworkLogEnabled) return
        
        val timestamp = dateFormat.format(Date())
        val processedHeaders = NetworkLogConfig.hideSensitiveInfo(headers)
        val processedBody = if (NetworkLogConfig.isDetailedLogEnabled) {
            NetworkLogConfig.hideSensitiveInfo(body)
        } else {
            ""
        }
        
        Log.d(NETWORK_TAG, "═══════════════════════════════════════════════════════════════")
        Log.d(NETWORK_TAG, "🌐 网络请求 [$timestamp]")
        Log.d(NETWORK_TAG, "📍 URL: $url")
        Log.d(NETWORK_TAG, "🔧 方法: $method")
        Log.d(NETWORK_TAG, "📋 请求头:")
        Log.d(NETWORK_TAG, processedHeaders)
        if (processedBody.isNotEmpty() && processedBody != "无请求体") {
            Log.d(NETWORK_TAG, "📦 请求体:")
            Log.d(NETWORK_TAG, NetworkLogConfig.truncateLog(processedBody))
        }
        Log.d(NETWORK_TAG, "═══════════════════════════════════════════════════════════════")
    }
    
    /**
     * 打印网络响应信息
     */
    fun logNetworkResponse(url: String, method: String, code: Int, message: String) {
        if (!NetworkLogConfig.isNetworkLogEnabled) return
        Log.d(NETWORK_TAG, "网络响应: $code $message ($url)")
    }
    
    /**
     * 打印详细的网络响应信息
     */
    fun logNetworkResponse(
        url: String, 
        method: String, 
        code: Int, 
        message: String, 
        duration: Long,
        headers: String,
        body: String
    ) {
        if (!NetworkLogConfig.isNetworkLogEnabled) return
        
        val timestamp = dateFormat.format(Date())
        val statusIcon = if (code in 200..299) "✅" else "❌"
        val processedHeaders = NetworkLogConfig.hideSensitiveInfo(headers)
        val processedBody = if (NetworkLogConfig.isDetailedLogEnabled) {
            NetworkLogConfig.hideSensitiveInfo(body)
        } else {
            ""
        }
        
        Log.d(NETWORK_TAG, "═══════════════════════════════════════════════════════════════")
        Log.d(NETWORK_TAG, "📡 网络响应 [$timestamp]")
        Log.d(NETWORK_TAG, "📍 URL: $url")
        Log.d(NETWORK_TAG, "🔧 方法: $method")
        Log.d(NETWORK_TAG, "⏱️  耗时: ${duration}ms")
        Log.d(NETWORK_TAG, "📊 状态: $statusIcon $code $message")
        Log.d(NETWORK_TAG, "📋 响应头:")
        Log.d(NETWORK_TAG, processedHeaders)
        if (processedBody.isNotEmpty()) {
            Log.d(NETWORK_TAG, "📦 响应体:")
            // 格式化JSON输出
            try {
                val formattedJson = if (NetworkLogConfig.isJsonFormatEnabled) {
                    formatJson(processedBody)
                } else {
                    processedBody
                }
                Log.d(NETWORK_TAG, NetworkLogConfig.truncateLog(formattedJson))
            } catch (e: Exception) {
                Log.d(NETWORK_TAG, NetworkLogConfig.truncateLog(processedBody))
            }
        }
        Log.d(NETWORK_TAG, "═══════════════════════════════════════════════════════════════")
    }
    
    /**
     * 格式化JSON字符串
     */
    private fun formatJson(jsonString: String): String {
        return try {
            val gson = com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping() // 禁用HTML转义，避免Unicode编码
                .create()
            var jsonElement = JsonParser.parseString(jsonString)
            var result =  gson.toJson(jsonElement)
            // *** 关键修正 ***
            // 1. 使用 DOT_MATCHES_ALL 让 . 匹配换行符
            // 2. 使用非贪婪匹配 (.*?)
            val options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            sensitiveFields.forEach { field ->
                // 正则表达式：匹配 "field": "value" 结构
                // "field" \s* : \s* "(.*?)"
                val regex = "\"$field\"\\s*:\\s*\"(.*?)\"".toRegex(options)
                // 替换为 "***"，保持 JSON 结构
                // 注意：替换值必须保持 JSON 字符串格式，即包含双引号
                result = result.replace(regex, "\"$field\": \"***\"")
            }
            result
        } catch (e: Exception) {
            jsonString
        }
    }
    
    /**
     * 打印API调用信息
     */
    fun logApiCall(apiName: String, params: Map<String, Any>? = null) {
        if (!NetworkLogConfig.isApiCallLogEnabled) return
        
        val timestamp = dateFormat.format(Date())
        Log.d(TAG, "🔗 API调用 [$timestamp]: $apiName")
        if (!params.isNullOrEmpty()) {
            val processedParams = params.mapValues { (key, value) ->
                if (NetworkLogConfig.sensitiveFields.contains(key.lowercase())) {
                    "***"
                } else {
                    value.toString()
                }
            }
            Log.d(TAG, "📝 参数: $processedParams")
        }
    }
    
    /**
     * 打印API响应信息
     */
    fun logApiResponse(apiName: String, response: Any?, isSuccess: Boolean) {
        if (!NetworkLogConfig.isApiCallLogEnabled) return
        
        val timestamp = dateFormat.format(Date())
        val statusIcon = if (isSuccess) "✅" else "❌"
        Log.d(TAG, "📡 API响应 [$timestamp]: $statusIcon $apiName")
        if (response != null && NetworkLogConfig.isDetailedLogEnabled) {
            // 使用Gson格式化输出，避免Unicode编码问题
            try {
                val gson = com.google.gson.GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping() // 禁用HTML转义，避免Unicode编码
                    .create()
                val jsonString = gson.toJson(response)
                Log.d(TAG, "📦 响应数据:")
                Log.d(TAG, jsonString)
            } catch (e: Exception) {
                // 如果Gson格式化失败，使用原始toString
                Log.d(TAG, "📦 响应数据: $response")
            }
        }
    }
    
    /**
     * 打印错误信息
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        if (!NetworkLogConfig.isErrorLogEnabled) return
        Log.e(TAG, "❌ [$tag] $message", throwable)
    }
    
    /**
     * 打印警告信息
     */
    fun logWarning(tag: String, message: String) {
        Log.w(TAG, "⚠️  [$tag] $message")
    }
    
    /**
     * 打印信息
     */
    fun logInfo(tag: String, message: String) {
        Log.i(TAG, "ℹ️  [$tag] $message")
    }
    
    /**
     * 打印调试信息
     */
    fun logDebug(tag: String, message: String) {
        Log.d(TAG, "🔍 [$tag] $message")
    }
} 
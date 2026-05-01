package com.example.ttai.utils

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * 网络日志配置类
 * 用于控制网络请求日志的开关和详细程度
 */
object NetworkLogConfig {
    
    /**
     * 是否启用网络日志
     */
    var isNetworkLogEnabled: Boolean = true
    
    /**
     * 是否启用详细日志（包含请求体和响应体）
     */
    var isDetailedLogEnabled: Boolean = true
    
    /**
     * 是否启用API调用日志
     */
    var isApiCallLogEnabled: Boolean = true
    
    /**
     * 是否启用错误日志
     */
    var isErrorLogEnabled: Boolean = true
    
    /**
     * 是否格式化JSON输出
     */
    var isJsonFormatEnabled: Boolean = true
    
    /**
     * 最大日志长度（防止日志过长）
     */
    var maxLogLength: Int = 10000
    
    /**
     * 敏感字段列表（这些字段的值会被隐藏）
     */
    val sensitiveFields = setOf(
        "password",
        "token",
        "authorization",
        "secret",
        "system_prompt",
        "description",
        "brief_intro",
        "briefIntro",
        "openingLine",
        "opening_line",
        "systemPrompt"

    )
    
    /**
     * 根据环境自动配置日志
     */
    fun configureByEnvironment() {
        when (EnvironmentConfig.getCurrentEnvironmentName()) {
            "测试环境" -> {
                // 测试环境：启用所有日志
                isNetworkLogEnabled = true
                isDetailedLogEnabled = true
                isApiCallLogEnabled = true
                isErrorLogEnabled = true
                isJsonFormatEnabled = true
            }
            "生产环境" -> {
                // 生产环境：只启用错误日志
                isNetworkLogEnabled = false
                isDetailedLogEnabled = false
                isApiCallLogEnabled = false
                isErrorLogEnabled = true
                isJsonFormatEnabled = false
            }
        }
    }
    
    /**
     * 隐藏敏感信息
     */
    fun hideSensitiveInfoss(text: String): String {
        var result = text

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

        // 如果您在日志中观察到非字符串值的敏感字段（如 "secret": 123），需要额外处理
        // 例如处理 "secret": 123 的情况，如果这些敏感字段值不是字符串：
        // val nonStringRegex = "\"$field\"\\s*:\\s*(\\d+|true|false|null)".toRegex(options)
        // result = result.replace(nonStringRegex, "\"$field\": ***")

        return result
    }

    /**
     * 清理 JSON 字符串中的所有敏感字段值，无论其类型如何（字符串、数字、布尔等）。
     *
     * @param jsonString 原始 JSON 字符串。
     * @return 清理后的 JSON 字符串，敏感字段的值被替换为 "***"。
     */
    fun hideSensitiveInfo(jsonString: String): String {
        // 1. 初始化 Moshi 实例和适配器
        val moshi = Moshi.Builder().build()

        // 目标类型：Map<String, Any?>，可以容纳各种 JSON 值的 Map
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
        val adapter = moshi.adapter<Map<String, Any?>>(type)

        // 2. 解析 JSON 字符串到 Map
        val rootMap = try {
            // 使用 toMutableMap() 方便后续修改
            adapter.fromJson(jsonString)?.toMutableMap()
        } catch (e: Exception) {
            // 解析失败时，直接返回原始字符串，并记录错误
            System.err.println("JSON 解析失败，无法隐藏敏感信息: ${e.message}")
            return jsonString
        }

        if (rootMap.isNullOrEmpty()) {
            return jsonString
        }

        // 3. 递归地处理 Map 中的所有键值对
        maskSensitiveFields(rootMap)

        // 4. 将修改后的 Map 重新序列化为 JSON 字符串
        return adapter.toJson(rootMap)
    }
    private const val MASK_VALUE = "***"
    /**
     * 递归遍历 Map/List 结构，替换敏感字段的值。
     */
    private fun maskSensitiveFields(data: Any?) {
        when (data) {
            is MutableMap<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val map = data as MutableMap<String, Any?>

                // 遍历并修改/递归
                val keys = map.keys.toList() // 创建副本以安全地迭代
                for (key in keys) {
                    // 检查当前键是否在敏感列表中（忽略大小写）
                    if (sensitiveFields.any { it.equals(key, ignoreCase = true) }) {
                        // 替换敏感字段的值
                        map[key] = MASK_VALUE
                    } else {
                        // 对非敏感字段的值进行递归处理（以防嵌套结构）
                        maskSensitiveFields(map[key])
                    }
                }
            }
            is List<*> -> {
                // 遍历列表中的每个元素
                for (item in data) {
                    maskSensitiveFields(item)
                }
            }
            // 对于 String, Int, Boolean 等基本类型，不做处理
            else -> Unit
        }
    }
    
    /**
     * 截断过长的日志
     */
    fun truncateLog(text: String): String {
        return if (text.length > maxLogLength) {
            text.substring(0, maxLogLength) + "\n... (日志已截断)"
        } else {
            text
        }
    }
} 
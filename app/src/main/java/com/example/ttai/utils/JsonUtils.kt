package com.example.ttai.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

/**
 * 通用 JSON 工具类（基于 Gson）
 * - toJson: 对象转 JSON 字符串
 * - toPrettyJson: 美化 JSON 输出
 * - fromJson: JSON 转对象（支持泛型）
 * - fromJsonList: JSON 转 List<T>
 * - fromJsonMap: JSON 转 Map<K,V>
 * - isJson: 校验字符串是否为有效 JSON
 */
object JsonUtils {
    @PublishedApi
    internal val gson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .create()

    @PublishedApi
    internal val prettyGson: Gson = GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    /**
     * 对象转 JSON 字符串
     * @param serializeNulls 是否序列化 null 字段
     */
    @JvmStatic
    fun toJson(value: Any?, serializeNulls: Boolean = false): String {
        return try {
            val g = if (serializeNulls) GsonBuilder()
                .disableHtmlEscaping()
                .serializeNulls()
                .create() else gson
            g.toJson(value)
        } catch (_: Exception) {
            ""
        }
    }

    /** 美化 JSON 输出 */
    @JvmStatic
    fun toPrettyJson(value: Any?): String {
        return try {
            prettyGson.toJson(value)
        } catch (_: Exception) {
            ""
        }
    }

    /** JSON 转对象（支持泛型类型推断） */
    @JvmStatic
    inline fun <reified T> fromJson(json: String?): T? {
        if (json.isNullOrBlank()) return null
        return try {
            val type = object : TypeToken<T>() {}.type
            gson.fromJson<T>(json, type)
        } catch (_: Exception) {
            null
        }
    }

    /** JSON 转 List<T> */
    @JvmStatic
    inline fun <reified T> fromJsonList(json: String?): List<T> {
        return fromJson<List<T>>(json) ?: emptyList()
    }

    /** JSON 转 Map<K,V> */
    @JvmStatic
    inline fun <reified K, reified V> fromJsonMap(json: String?): Map<K, V> {
        return fromJson<Map<K, V>>(json) ?: emptyMap()
    }

    /** 校验是否为有效 JSON */
    @JvmStatic
    fun isJson(json: String?): Boolean {
        if (json.isNullOrBlank()) return false
        return try {
            JsonParser.parseString(json)
            true
        } catch (_: Exception) {
            false
        }
    }
}

/** 简便扩展方法 */
inline fun <reified T> String.fromJson(): T? = JsonUtils.fromJson(this)
fun Any?.toJson(): String = JsonUtils.toJson(this)



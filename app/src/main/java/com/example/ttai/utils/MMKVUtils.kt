package com.example.ttai.utils

import android.content.Context
import android.os.Parcelable
import com.example.ttai.bean.MembershipInfo
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.UserProfile
import com.example.ttai.network.NetworkModule
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * MMKV工具类
 * 提供高性能的键值对存储功能
 */
object MMKVUtils {

    private const val DEFAULT_MMKV_ID = "default_mmkv"
    const val PHONE = "phone"
    const val IS_FIRST_USE = "is_first_use"
    const val USER_PROFILE = "user_profile"


    
    // 默认MMKV实例
    val defaultMMKV: MMKV by lazy {
        MMKV.defaultMMKV()
    }
    

    /**
     * 初始化MMKV
     * 在Application中调用
     */
    fun initialize(context: Context) {
        MMKV.initialize(context)
    }

    
    /**
     * 存储字符串
     */
    fun putString(key: String, value: String) {
        defaultMMKV.encode(key, value)
    }
    
    /**
     * 获取字符串
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return defaultMMKV.decodeString(key, defaultValue) ?: defaultValue
    }
    
    /**
     * 存储整数
     */
    fun putInt(key: String, value: Int) {
        defaultMMKV.encode(key, value)
    }
    
    /**
     * 获取整数
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return defaultMMKV.decodeInt(key, defaultValue)
    }
    
    /**
     * 存储长整数
     */
    fun putLong(key: String, value: Long) {
        defaultMMKV.encode(key, value)
    }
    
    /**
     * 获取长整数
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return defaultMMKV.decodeLong(key, defaultValue)
    }
    
    /**
     * 存储浮点数
     */
    fun putFloat(key: String, value: Float) {
        defaultMMKV.encode(key, value)
    }
    
    /**
     * 获取浮点数
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return defaultMMKV.decodeFloat(key, defaultValue)
    }
    
    /**
     * 存储双精度浮点数
     */
    fun putDouble(key: String, value: Double) {
        defaultMMKV.encode(key, value)
    }
    
    /**
     * 获取双精度浮点数
     */
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return defaultMMKV.decodeDouble(key, defaultValue)
    }
    
    /**
     * 存储布尔值
     */
    fun putBoolean(key: String, value: Boolean) {
        defaultMMKV.encode(key, value)
    }
    
    /**
     * 获取布尔值
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return defaultMMKV.decodeBool(key, defaultValue)
    }
    
    /**
     * 存储字节数组
     */
    fun putBytes(key: String, value: ByteArray) {
        defaultMMKV.encode(key, value)
    }
    
    /**
     * 获取字节数组
     */
    fun getBytes(key: String, defaultValue: ByteArray = ByteArray(0)): ByteArray {
        return defaultMMKV.decodeBytes(key, defaultValue) ?: defaultValue
    }

    /**
     * 获取对象
     */
    inline fun <reified T : Parcelable> getParcelable(key: String): T? {
        return defaultMMKV.decodeParcelable(key, T::class.java)
    }
    
    /**
     * 检查是否包含指定键
     */
    fun containsKey(key: String): Boolean {
        return defaultMMKV.containsKey(key)
    }
    
    /**
     * 删除指定键
     */
    fun remove(key: String) {
        defaultMMKV.removeValueForKey(key)
    }

    /**
     * 清空所有数据
     */
    fun clearAll() {
        defaultMMKV.clearAll()
    }

    /**
     * 异步存储数据
     */
    suspend fun putStringAsync(key: String, value: String) {
        withContext(Dispatchers.IO) {
            putString(key, value)
        }
    }
    
    /**
     * 异步获取数据
     */
    suspend fun getStringAsync(key: String, defaultValue: String = ""): String {
        return withContext(Dispatchers.IO) {
            getString(key, defaultValue)
        }
    }
    
    // ==================== 用户资料相关方法 ====================


    /**
     * 获取用户资料数据
     * 如果本地数据为空，会自动调用API获取数据并保存到MMKV
     */
    fun getUserProfile(): UserProfile? {
        return getParcelable<UserProfile>(USER_PROFILE)
    }
    
    /**
     * 存储用户基本信息
     */
    fun putUserProfile(userProfile: UserProfile?) {
        defaultMMKV.encode(USER_PROFILE, userProfile)
    }

    /**
     * 清除用户资料数据
     */
    fun clearUserProfile() {
        defaultMMKV.removeValueForKey("user_profile")
        defaultMMKV.removeValueForKey("user_profile_basic")
        defaultMMKV.removeValueForKey("membership_info")
    }


    /**

     * @param versionServer 服务器最新的版本号（可以是 VersionCode 字符串或 VersionName，如 "1.0.2"）
     */
    fun saveVersionPrompted(onlineVersionCode: Long) {
        putLong(Keys.IGNORED_VERSION, onlineVersionCode)

    }
    /**
     * @param versionServer 该线上版本是否提示过
     */
    fun isVersionPrompted(latestBuild: Long): Boolean {
       return getLong(Keys.IGNORED_VERSION,0L) >=latestBuild
    }


    // ==================== 常量定义 ====================
    
    object Keys {
        // 用户相关
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_NAME = "user_name"
        const val USER_PHONE = "user_phone"
        const val USER_DEVICE_ID = "user_device_id"
        const val IS_FIRST_LAUNCH = "is_first_launch"
        const val IS_LOGIN = "is_login"
        
        // 设置相关
        const val THEME_MODE = "theme_mode"
        const val LANGUAGE = "language"
        const val NOTIFICATION_ENABLED = "notification_enabled"
        const val AUTO_PLAY = "auto_play"
        const val AI_MODEL_PREFERENCE = "ai_model_preference"
        
        // 缓存相关
        const val CHARACTER_LIST_CACHE = "character_list_cache"
        const val CONVERSATION_LIST_CACHE = "conversation_list_cache"
        const val CACHE_TIMESTAMP = "cache_timestamp"
        
        // 应用相关
        const val APP_VERSION = "app_version"
        const val LAST_UPDATE_TIME = "last_update_time"
        const val CRASH_COUNT = "crash_count"

        // 专门用于版本更新控制的 Key
        const val IGNORED_VERSION = "ignored_version"       // 上次点击暂不更新的版本号
        const val IGNORE_TIMESTAMP = "ignore_timestamp"     // 点击暂不更新时的时间戳
    }
    
    object DefaultValues {
        const val THEME_MODE_DARK = "dark"
        const val THEME_MODE_LIGHT = "light"
        const val THEME_MODE_SYSTEM = "system"
        const val LANGUAGE_ZH = "zh"
        const val LANGUAGE_EN = "en"
        const val AI_MODEL_DEFAULT = "gpt-3.5-turbo"
    }
} 
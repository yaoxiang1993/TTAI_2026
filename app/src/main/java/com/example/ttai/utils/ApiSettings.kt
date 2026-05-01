package com.example.ttai.utils

import com.example.ttai.network.ApiHelper

/**
 * API设置管理类
 * 用于配置API请求的全局行为
 */
object ApiSettings {
    
    /**
     * 初始化API设置
     * 建议在Application类的onCreate方法中调用
     * 
     * @param autoShowErrorMessages 是否自动显示错误提示，默认为true
     * @param autoShowSuccessMessages 是否自动显示成功消息，默认为true
     */
    fun init(
        autoShowErrorMessages: Boolean = true,
        autoShowSuccessMessages: Boolean = true
    ) {
        ApiHelper.autoShowErrorMessages = autoShowErrorMessages
        ApiHelper.autoShowSuccessMessages = autoShowSuccessMessages
    }
    
    /**
     * 启用自动错误提示
     */
    fun enableAutoErrorMessages() {
        ApiHelper.autoShowErrorMessages = true
    }
    
    /**
     * 禁用自动错误提示
     */
    fun disableAutoErrorMessages() {
        ApiHelper.autoShowErrorMessages = false
    }
    
    /**
     * 启用自动成功消息
     */
    fun enableAutoSuccessMessages() {
        ApiHelper.autoShowSuccessMessages = true
    }
    
    /**
     * 禁用自动成功消息
     */
    fun disableAutoSuccessMessages() {
        ApiHelper.autoShowSuccessMessages = false
    }
    
    /**
     * 获取当前是否启用自动错误提示
     */
    fun isAutoErrorMessagesEnabled(): Boolean {
        return ApiHelper.autoShowErrorMessages
    }
    
    /**
     * 获取当前是否启用自动成功消息
     */
    fun isAutoSuccessMessagesEnabled(): Boolean {
        return ApiHelper.autoShowSuccessMessages
    }
}

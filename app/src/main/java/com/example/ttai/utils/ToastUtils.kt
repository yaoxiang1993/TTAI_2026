package com.example.ttai.utils

import android.content.Context
import android.widget.Toast

/**
 * Toast工具类，用于管理Toast的显示
 * 确保新的Toast会覆盖旧的Toast，避免多个Toast同时显示
 * 
 * 使用示例：
 * // 显示短时间Toast
 * ToastUtils.showShort(this, "操作成功")
 * 
 * // 显示长时间Toast
 * ToastUtils.showLong(this, "这是一个较长的提示信息")
 * 
 * // 取消当前显示的Toast
 * ToastUtils.cancel()
 */
object ToastUtils {
    
    private var currentToast: Toast? = null
    
    /**
     * 显示短时间Toast，会覆盖之前的Toast
     */
    fun showShort(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_SHORT)
    }
    
    /**
     * 显示长时间Toast，会覆盖之前的Toast
     */
    fun showLong(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_LONG)
    }
    
    /**
     * 显示Toast的核心方法
     * 会取消之前的Toast，然后显示新的Toast
     */
    private fun showToast(context: Context, message: String, duration: Int) {
        // 取消之前的Toast
        currentToast?.cancel()
        
        // 创建新的Toast
        currentToast = Toast.makeText(context, message, duration)
        currentToast?.show()
    }
    
    /**
     * 取消当前显示的Toast
     */
    fun cancel() {
        currentToast?.cancel()
        currentToast = null
    }
    
    /**
     * 显示API返回的消息
     */
    fun showApiMessage(context: Context, message: String) {
        showShort(context, message)
    }
    
    /**
     * 显示错误消息
     */
    fun showError(context: Context, message: String) {
        showShort(context, message)
    }
} 
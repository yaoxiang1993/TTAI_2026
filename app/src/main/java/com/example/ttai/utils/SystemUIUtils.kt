package com.example.ttai.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 系统UI工具类
 * 用于统一管理系统UI的显示和隐藏
 */
object SystemUIUtils {
    
    /**
     * 设置沉浸式模式 - 隐藏状态栏和导航栏
     */
    fun setImmersiveMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11及以上使用新的API
            activity.window.setDecorFitsSystemWindows(false)
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 11以下使用传统方法
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    
    /**
     * 设置透明状态栏但保持显示 - 状态栏透明但可见，保留导航栏
     */
    fun setTransparentStatusBarVisible(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            activity.window.insetsController?.let { controller ->
                controller.show(WindowInsets.Type.statusBars())
                controller.show(WindowInsets.Type.navigationBars())
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // 设置状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }
    
    /**
     * 设置透明状态栏和导航栏 - 状态栏透明，导航栏隐藏
     */
    fun setTransparentStatusBarAndHideNavigation(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(false)
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
        
        // 设置状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }
    
    /**
     * 显示系统UI - 显示状态栏和导航栏
     */
    fun showSystemUI(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(true)
            activity.window.insetsController?.let { controller ->
                controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
    
    /**
     * 设置状态栏图标颜色
     * @param isLight true为白色图标（深色背景），false为黑色图标（浅色背景）
     */
    fun setStatusBarIconColor(activity: Activity, isLight: Boolean) {
        try {
            // 直接使用WindowInsetsControllerCompat，它更兼容
            WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = !isLight
            }
        } catch (e: Exception) {
            // 如果API不可用，使用传统方法
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                @Suppress("DEPRECATION")
                var flags = activity.window.decorView.systemUiVisibility
                if (isLight) {
                    flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                activity.window.decorView.systemUiVisibility = flags
            }
        }
    }
    


    
    /**
     * 检查是否有虚拟导航栏
     */
    fun hasNavigationBar(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = activity.window.decorView.rootWindowInsets
            return windowInsets?.isVisible(WindowInsets.Type.navigationBars()) == true
        } else {
            @Suppress("DEPRECATION")
            val uiOptions = activity.window.decorView.systemUiVisibility
            return (uiOptions and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0
        }
    }
    
    /**
     * 获取导航栏高度
     */
    fun getNavigationBarHeight(activity: Activity): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = activity.window.decorView.rootWindowInsets
            return windowInsets?.getInsets(WindowInsets.Type.navigationBars())?.bottom ?: 0
        } else {
            val resourceId = activity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) activity.resources.getDimensionPixelSize(resourceId) else 0
        }
    }
}

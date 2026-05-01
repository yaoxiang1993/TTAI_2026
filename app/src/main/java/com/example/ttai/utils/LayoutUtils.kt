package com.example.ttai.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.ttai.R

/**
 * 布局工具类
 * 用于处理虚拟导航栏高度适配等布局相关功能
 */
object LayoutUtils {
    
    /**
     * 为Activity的根布局添加虚拟导航栏高度适配
     * 如果设备有虚拟导航栏，会自动为底部内容添加对应的padding
     */
    fun setupNavigationBarPadding(activity: Activity) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        setupNavigationBarPadding(rootView)
    }
    
    /**
     * 为指定View添加虚拟导航栏高度适配
     */
    fun setupNavigationBarPadding(view: View) {
        // 检查是否已经设置过，避免重复设置
        if (view.getTag(R.id.navigation_bar_adapter_tag) == true) {
            return
        }
        
        // 标记为已设置
        view.setTag(R.id.navigation_bar_adapter_tag, true)
        
        // 记录原始的padding值
        val originalPaddingBottom = view.paddingBottom
        
        // 统一使用ViewCompat来处理，它会在内部处理版本差异
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val navigationBarHeight = insets.bottom
            
            // 只有当导航栏高度大于0时才添加padding，使用原始padding值
            if (navigationBarHeight > 0) {
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, originalPaddingBottom + navigationBarHeight)
            } else {
                // 如果没有虚拟导航栏，恢复原始padding
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, originalPaddingBottom)
            }
            WindowInsetsCompat.CONSUMED
        }
    }
    
    /**
     * 为指定View添加虚拟导航栏高度适配（使用margin）
     */
    fun setupNavigationBarMargin(view: View) {
        // 检查是否已经设置过，避免重复设置
        if (view.getTag(R.id.navigation_bar_adapter_tag) == true) {
            return
        }
        
        // 标记为已设置
        view.setTag(R.id.navigation_bar_adapter_tag, true)
        
        // 记录原始的margin值
        val originalBottomMargin = (view.layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
        
        // 统一使用ViewCompat来处理，它会在内部处理版本差异
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val navigationBarHeight = insets.bottom
            
            // 只有当导航栏高度大于0时才添加margin
            if (navigationBarHeight > 0) {
                val layoutParams = v.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.let {
                    it.bottomMargin = originalBottomMargin + navigationBarHeight
                    v.layoutParams = it
                }
            } else {
                // 如果没有虚拟导航栏，恢复原始margin
                val layoutParams = v.layoutParams as? ViewGroup.MarginLayoutParams
                layoutParams?.let {
                    it.bottomMargin = originalBottomMargin
                    v.layoutParams = it
                }
            }
            
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * 为底部导航栏添加虚拟导航栏高度适配
     * 专门用于处理底部导航栏的适配
     */
    fun setupBottomNavigationPadding(activity: Activity, bottomNavigationId: Int) {
        val bottomNavigation = activity.findViewById<View>(bottomNavigationId)
        bottomNavigation?.let { view ->
            setupNavigationBarPadding(view)
        }
    }
    
    /**
     * 为输入框区域添加虚拟导航栏高度适配
     * 专门用于处理输入框区域的适配
     */
    fun setupInputContainerPadding(activity: Activity, inputContainerId: Int) {
        val inputContainer = activity.findViewById<View>(inputContainerId)
        inputContainer?.let { view ->
            setupNavigationBarPadding(view)
        }
    }
    
    /**
     * 为指定ID的View添加虚拟导航栏高度适配
     * 通用方法，可以用于任何View的适配
     */
    fun setupViewNavigationBarPadding(activity: Activity, viewId: Int) {
        val view = activity.findViewById<View>(viewId)
        view?.let { v ->
            setupNavigationBarPadding(v)
        }
    }
    
    /**
     * 为指定ID的View添加虚拟导航栏高度适配（使用margin）
     * 通用方法，可以用于任何View的适配
     */
    fun setupViewNavigationBarMargin(activity: Activity, viewId: Int) {
        val view = activity.findViewById<View>(viewId)
        view?.let { v ->
            setupNavigationBarMargin(v)
        }
    }
    
    /**
     * 清除View的虚拟导航栏适配
     * 移除WindowInsets监听器并清除Tag标记
     */
    fun clearNavigationBarAdapter(view: View) {
        // 清除Tag标记
        view.setTag(R.id.navigation_bar_adapter_tag, null)
        
        // 移除WindowInsets监听器
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
    }
    
    /**
     * 清除指定ID的View的虚拟导航栏适配
     */
    fun clearViewNavigationBarAdapter(activity: Activity, viewId: Int) {
        val view = activity.findViewById<View>(viewId)
        view?.let { v ->
            clearNavigationBarAdapter(v)
        }
    }
}

package com.example.ttai.utils

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * 简化版键盘管理工具类
 * 专门用于解决点击外部区域隐藏键盘的问题
 */
object SimpleKeyboardUtils {
    
    /**
     * 为Activity设置点击外部区域隐藏键盘的功能
     * @param activity 当前Activity
     */
    fun setupHideKeyboardOnTouchOutside(activity: Activity) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        com.example.ttai.utils.SimpleKeyboardUtils.setupHideKeyboardOnTouchOutside(
            activity,
            rootView
        )
    }
    
    /**
     * 为Activity设置点击外部区域隐藏键盘的功能
     * @param activity 当前Activity
     * @param rootView 根视图
     */
    fun setupHideKeyboardOnTouchOutside(activity: Activity, rootView: View) {
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentFocus = activity.currentFocus
                if (currentFocus is EditText) {
                    // 简化逻辑：直接检查触摸位置是否在EditText范围内
                    val outLocation = IntArray(2)
                    currentFocus.getLocationInWindow(outLocation)
                    val x = event.rawX.toInt()
                    val y = event.rawY.toInt()
                    
                    // 如果点击位置不在EditText范围内，则隐藏键盘
                    if (x < outLocation[0] || x > outLocation[0] + currentFocus.width ||
                        y < outLocation[1] || y > outLocation[1] + currentFocus.height) {
                        com.example.ttai.utils.SimpleKeyboardUtils.hideSoftInput(activity)
                        currentFocus.clearFocus()
                        android.util.Log.d("SimpleKeyboardUtils", "隐藏键盘：点击位置($x, $y)不在EditText范围内")
                    } else {
                        android.util.Log.d("SimpleKeyboardUtils", "保持键盘：点击位置($x, $y)在EditText范围内")
                    }
                } else {
                    android.util.Log.d("SimpleKeyboardUtils", "当前没有获得焦点的EditText")
                }
            }
            false // 返回false让其他触摸事件继续处理
        }
    }
    
    /**
     * 为Activity设置点击外部区域隐藏键盘的功能（保护指定的EditText）
     * @param activity 当前Activity
     * @param rootView 根视图
     * @param protectedEditTexts 需要保护的EditText列表（点击这些EditText不会隐藏键盘）
     */
    fun setupHideKeyboardOnTouchOutside(
        activity: Activity,
        rootView: View,
        vararg protectedEditTexts: EditText
    ) {
        val protectedSet = protectedEditTexts.toSet()
        
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentFocus = activity.currentFocus
                if (currentFocus is EditText && !protectedSet.contains(currentFocus)) {
                    val outLocation = IntArray(2)
                    currentFocus.getLocationInWindow(outLocation)
                    val x = event.rawX.toInt()
                    val y = event.rawY.toInt()
                    
                    // 如果点击位置不在EditText范围内，则隐藏键盘
                    if (x < outLocation[0] || x > outLocation[0] + currentFocus.width ||
                        y < outLocation[1] || y > outLocation[1] + currentFocus.height) {
                        com.example.ttai.utils.SimpleKeyboardUtils.hideSoftInput(activity)
                        currentFocus.clearFocus()
                        android.util.Log.d("SimpleKeyboardUtils", "隐藏键盘：点击位置($x, $y)不在EditText范围内")
                    } else {
                        android.util.Log.d("SimpleKeyboardUtils", "保持键盘：点击位置($x, $y)在EditText范围内")
                    }
                } else if (currentFocus is EditText && protectedSet.contains(currentFocus)) {
                    android.util.Log.d("SimpleKeyboardUtils", "保护EditText，不隐藏键盘")
                } else {
                    android.util.Log.d("SimpleKeyboardUtils", "当前没有获得焦点的EditText")
                }
            }
            false // 返回false让其他触摸事件继续处理
        }
    }
    
    /**
     * 隐藏软键盘
     * @param activity 当前Activity
     */
    fun hideSoftInput(activity: Activity) {
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            android.util.Log.d("SimpleKeyboardUtils", "执行隐藏键盘操作")
        } else {
            android.util.Log.w("SimpleKeyboardUtils", "没有当前焦点，无法隐藏键盘")
        }
    }
    
    /**
     * 显示软键盘
     * @param context 上下文
     * @param view 需要获得焦点的视图
     */
    fun showSoftInput(context: Context, view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
    
    /**
     * 强制隐藏软键盘（更直接的方法）
     * @param activity 当前Activity
     */
    fun forceHideSoftInput(activity: Activity) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus ?: activity.window.decorView
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        android.util.Log.d("SimpleKeyboardUtils", "强制隐藏键盘")
    }
}

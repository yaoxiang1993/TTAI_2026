package com.example.ttai.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

/**
 * 键盘管理工具类
 * 提供键盘显示、隐藏等常用功能
 */
object KeyboardUtils {
    
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
     * 隐藏软键盘
     * @param context 上下文
     * @param view 当前获得焦点的视图
     */
    fun hideSoftInput(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    
    /**
     * 隐藏软键盘（通过Activity）
     * @param activity 当前Activity
     */
    fun hideSoftInput(activity: Activity) {
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            hideSoftInput(activity, currentFocus)
        }
    }
    
    /**
     * 切换软键盘显示状态
     * @param context 上下文
     */
    fun toggleSoftInput(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }
    
    /**
     * 判断软键盘是否显示
     * @param context 上下文
     * @return true表示软键盘显示，false表示软键盘隐藏
     */
    fun isSoftInputVisible(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.isAcceptingText
    }
    
    /**
     * 为EditText设置点击外部区域隐藏键盘的功能
     * @param activity 当前Activity
     * @param rootView 根视图，通常是Activity的根布局
     * @param editTexts 需要保护的EditText列表（点击这些EditText不会隐藏键盘）
     */
    fun setupHideKeyboardOnTouchOutside(
        activity: Activity,
        rootView: View,
        vararg editTexts: EditText
    ) {
        val editTextSet = editTexts.toSet()
        
        rootView.setOnTouchListener { _, event ->
            // 检查触摸事件是否在EditText区域内
            val touchedView = findViewAtPosition(rootView, event.rawX.toInt(), event.rawY.toInt())
            
            // 如果触摸的不是EditText，则隐藏键盘
            if (touchedView !is EditText || !editTextSet.contains(touchedView)) {
                hideSoftInput(activity)
                // 清除当前焦点
                val currentFocus = activity.currentFocus
                if (currentFocus != null) {
                    currentFocus.clearFocus()
                }
            }
            
            false // 返回false让其他触摸事件继续处理
        }
    }
    
    /**
     * 查找指定坐标位置的视图
     * @param root 根视图
     * @param x X坐标
     * @param y Y坐标
     * @return 找到的视图，如果没找到则返回null
     */
    private fun findViewAtPosition(root: View, x: Int, y: Int): View? {
        val location = IntArray(2)
        root.getLocationOnScreen(location)
        
        // 检查根视图是否包含该坐标
        if (x < location[0] || x > location[0] + root.width ||
            y < location[1] || y > location[1] + root.height) {
            return null
        }
        
        // 递归查找子视图
        return findViewAtPositionRecursive(root, x, y)
    }
    
    /**
     * 递归查找指定坐标位置的视图
     * @param view 当前视图
     * @param x X坐标
     * @param y Y坐标
     * @return 找到的视图，如果没找到则返回null
     */
    private fun findViewAtPositionRecursive(view: View, x: Int, y: Int): View? {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        
        // 检查当前视图是否包含该坐标
        if (x >= location[0] && x <= location[0] + view.width &&
            y >= location[1] && y <= location[1] + view.height) {
            
            // 如果是ViewGroup，继续查找子视图
            if (view is android.view.ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    val childResult = findViewAtPositionRecursive(child, x, y)
                    if (childResult != null) {
                        return childResult
                    }
                }
            }
            
            return view
        }
        
        return null
    }
    

}

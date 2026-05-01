package com.example.ttai.utils

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyboardManager {
    
    fun setupHideKeyboardOnTouchOutside(activity: Activity) {
        val rootView = activity.findViewById<View>(android.R.id.content)
        setupHideKeyboardOnTouchOutside(activity, rootView)
    }
    
    fun setupHideKeyboardOnTouchOutside(activity: Activity, rootView: View) {
        rootView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val currentFocus = activity.currentFocus
                if (currentFocus is EditText) {
                    val touchedView = findViewAtPosition(rootView, event.rawX.toInt(), event.rawY.toInt())
                    
                    if (touchedView !is EditText || !isChildOf(touchedView, currentFocus)) {
                        hideSoftInput(activity)
                        currentFocus.clearFocus()
                        android.util.Log.d("KeyboardManager", "隐藏键盘：点击了EditText外部区域")
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }
    
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
                    val touchedView = findViewAtPosition(rootView, event.rawX.toInt(), event.rawY.toInt())
                    
                    if (touchedView !is EditText || !isChildOf(touchedView, currentFocus)) {
                        hideSoftInput(activity)
                        currentFocus.clearFocus()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }
    
    private fun findViewAtPosition(root: View, x: Int, y: Int): View? {
        val location = IntArray(2)
        root.getLocationOnScreen(location)
        
        if (x < location[0] || x > location[0] + root.width ||
            y < location[1] || y > location[1] + root.height) {
            return null
        }
        
        return findViewAtPositionRecursive(root, x, y)
    }
    
    private fun findViewAtPositionRecursive(view: View, x: Int, y: Int): View? {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        
        if (x >= location[0] && x <= location[0] + view.width &&
            y >= location[1] && y <= location[1] + view.height) {
            
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
    
    private fun isChildOf(view: View, parent: View): Boolean {
        if (view == parent) return true
        
        var currentParent = view.parent
        while (currentParent is View) {
            if (currentParent == parent) return true
            currentParent = currentParent.parent
        }
        
        return false
    }
    
    fun hideSoftInput(activity: Activity) {
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
    
    fun showSoftInput(context: Context, view: View) {
        view.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

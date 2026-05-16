package com.example.ttai.base

import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.ttai.utils.LayoutUtils
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class BaseMviActivity<I : MviIntent, S : MviState, VM : MviViewModel<I, S>, VB : ViewBinding> : AppCompatActivity() {
    abstract val viewModel: VM
    abstract val binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 设置系统UI
//        setupSystemUI()
        setupViews()
        setupBackButton()
        observeState()
        
        // 设置虚拟导航栏适配（如果子类需要的话）
        if (shouldSetupNavigationBarAdapter()) {
            setupNavigationBarAdapter()
        }
    }

    abstract fun setupViews()

    /**
     * 设置返回按钮
     * 自动查找id为ivBack的ImageView并设置点击事件
     */
    private fun setupBackButton() {
        try {
            val backButton = findViewById<View>(android.R.id.home)
            if (backButton != null) {
                backButton.setOnClickListener {
                    onBackPressed()
                }
            }
            
            // 查找自定义的ivBack控件
            val ivBack = findViewById<View>(resources.getIdentifier("ivBack", "id", packageName))
            if (ivBack != null) {
                ivBack.setOnClickListener {
                    onBackPressed()
                }
            }
        } catch (e: Exception) {
            // 忽略找不到控件的异常
        }
    }
    
    /**
     * 重写返回按钮行为
     * 子类可以重写此方法来自定义返回逻辑
     */
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    
    open fun render(state: S) {
    }

    public fun sendIntent(intent: I) {
        viewModel.processIntent(intent)
    }

    protected fun navigateTo(activityClass: Class<*>, finishCurrent: Boolean = false) {
        startActivity(Intent(this, activityClass))
        if (finishCurrent) finish()
    }
    
    /**
     * 跳转到新页面，启动模式设置为新栈
     * @param activityClass 目标Activity类
     * @param finishCurrent 是否结束当前Activity
     */
    protected fun navigateToNewTask(activityClass: Class<*>, finishCurrent: Boolean = false) {
        val intent = Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
        if (finishCurrent) finish()
    }
    
    /**
     * 跳转到新页面，启动模式设置为新栈，并传递数据
     * @param activityClass 目标Activity类
     * @param extras 要传递的数据
     * @param finishCurrent 是否结束当前Activity
     */
    protected fun navigateToNewTask(activityClass: Class<*>, extras: Bundle, finishCurrent: Boolean = false) {
        val intent = Intent(this, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtras(extras)
        }
        startActivity(intent)
        if (finishCurrent) finish()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                render(state)
            }
        }
    }

    
    /**
     * 判断是否应该设置虚拟导航栏适配
     * 子类可以重写此方法来指定是否需要虚拟导航栏适配
     */
    protected open fun shouldSetupNavigationBarAdapter(): Boolean {
        return false
    }
    
    /**
     * 设置虚拟导航栏适配
     * 子类可以重写此方法来自定义虚拟导航栏适配逻辑
     */
    protected open fun setupNavigationBarAdapter() {
        // 默认实现：为根布局添加虚拟导航栏高度适配
        LayoutUtils.setupNavigationBarPadding(this)
    }

}

inline fun <reified VB : ViewBinding> viewBinding(
    crossinline inflate: (LayoutInflater) -> VB
): ReadOnlyProperty<AppCompatActivity, VB> = object : ReadOnlyProperty<AppCompatActivity, VB> {
    private var binding: VB? = null

    override fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): VB {
        if (binding == null) {
            binding = inflate(thisRef.layoutInflater)
        }
        return binding!!
    }
}
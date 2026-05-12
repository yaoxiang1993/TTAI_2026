package com.example.ttai.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import com.example.ttai.databinding.ActivityMainBinding
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ttai.R
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.AppUpdateInfo
import com.example.ttai.bean.ShopItem
import com.example.ttai.event.SwitchToMyFragmentEvent
import com.example.ttai.ui.fragment.ControlFragment
import com.example.ttai.ui.fragment.DreamFragment
import com.example.ttai.ui.fragment.MainFragment
import com.example.ttai.ui.fragment.MyFragment
import com.example.ttai.intent.MainActivityIntent
import com.example.ttai.state.MainActivityState
import com.example.ttai.ui.dialog.OneButtonDialogFragment
import com.example.ttai.ui.dialog.UpdateDialogFragment
import com.example.ttai.utils.Constants
import com.example.ttai.utils.LayoutUtils
import com.example.ttai.ui.vm.MainActivityViewModel
import com.example.ttai.ui.vm.MainActivityViewModelFactory
import com.example.ttai.ui.vm.MainFragmentViewModelFactory
import com.example.ttai.utils.AppUpdater
import com.example.ttai.utils.ToastUtils
import java.util.ArrayList

class MainActivity : BaseMviActivity<MainActivityIntent, MainActivityState, MainActivityViewModel, ActivityMainBinding>() {
    override val viewModel: MainActivityViewModel by viewModels{ MainActivityViewModelFactory(this) }
    override val binding by viewBinding { ActivityMainBinding.inflate(it) }
    
    // 跟踪当前选中的 tab 索引
    private var currentTabIndex = 0

    override fun setupViews() {
        // 设置 ViewPager2 适配器
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 4

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> MainFragment()
                    1 -> {
                        // 创建MainFragment并传递性格标签
                        val fragment = DreamFragment()
                        // 从Intent中获取性格标签
                        val personalityTag: ArrayList<String>? =
                            intent.getStringArrayListExtra(Constants.PERSONALITY_TAG)
                        if (personalityTag != null) {
                            val bundle = Bundle()
                            bundle.putStringArrayList(Constants.PERSONALITY_TAG, personalityTag)
                            fragment.arguments = bundle
                        }
                        fragment
                    }
                    2 -> ControlFragment()
                    3 -> MyFragment()
                    else -> MainFragment()
                }
            }
        }
        // 禁用 ViewPager2 滑动
        binding.viewPager.isUserInputEnabled = false

        // 设置自定义底部导航栏点击事件
        setupCustomBottomNavigation()
        
        // 初始化选中状态
        updateTabSelection(0)
        if (intent.getBooleanExtra(Constants.SHOW_DIALOG,false)){
            show18DialogClick()
        }
        sendIntent(MainActivityIntent.versionCheck)
    }

    private fun showUpdateDialog(appUpdateInfo : AppUpdateInfo?) {
        val notesDisplay = appUpdateInfo?.releaseNotes?.joinToString("\n") { "• $it" }
        val dialog = UpdateDialogFragment.newInstance(appUpdateInfo?.title,appUpdateInfo?.subtitle, message = notesDisplay,appUpdateInfo?.buttonText,appUpdateInfo?.cancelText)
            .setOnButtonClickListener(object : UpdateDialogFragment.OnButtonClickListener {
                override fun onPositiveClick() {
                    // 开始更新
                    val apkUrl : String? =  appUpdateInfo?.actionUrl
                      apkUrl?.let {
                        AppUpdater.startDownload(this@MainActivity, apkUrl )
                    }?:{
                        ToastUtils.showShort(this@MainActivity,"下载链接获取失败")
                    }
                }

                override fun onNegativeClick() {
                    // 暂不更新
                   if (appUpdateInfo?.forceUpdate == true) {
                       finishAffinity()
                   }
                }
            })

        // 如果是强制更新，禁止点击弹窗外部取消
        dialog.setCancelable(false)
        dialog.show(supportFragmentManager, "UpdateDialogFragment")
    }

    private fun show18DialogClick( ) {
        val dialog = OneButtonDialogFragment.newInstance(
            message = "TTAI平台可链接的玩具涉及成年人\n" +
                    "才可体验的玩具，TTAI平台为仅适\n" +
                    "用于18+年龄用户的AI聊天平台, 未\n" +
                    "成年请严禁涉足本平台！\n" +
                    "若您是未成年个人的监护人发现手\n" +
                    "机中下载了本应用，请您立刻删除\n" +
                    "本app并对未成年人严加管教！我\n" +
                    "们从不支持和引导未成年下载TTAI\n" +
                    "，感谢您的理解和支持！\n" +
                    "TTAI不支持用户创作以及交互违背\n" +
                    "伦理道德、涉及儿童色情、背离社\n" +
                    "会主流核心价值观、政治相关、暴\n" +
                    "力血腥等不合伦理纲常的角色和言\n" +
                    "论！我们相信情趣是阳光和快乐的\n" +
                    "，在TTAI平台，请勿把淫秽与情趣\n" +
                    "划等号！再次提醒，未成年止步，\n" +
                    "否则，后果自负！\n" +
                    "请阅读完以上内容后再进入TTAI，\n" +
                    "望所有用户悉知！\n",
            positiveText = "好的"
        ).setOnButtonClickListener(object : OneButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {

            }
        })
        dialog.show(supportFragmentManager, "OneButtonDialogFragment")
    }

    override fun render(state: MainActivityState) {
        super.render(state)
        // 更新当前 tab 索引
        currentTabIndex = state.currentTabIndex
        // 切换 ViewPager2 页面
        binding.viewPager.setCurrentItem(state.currentTabIndex, false)
        // 更新自定义底部导航栏选中状态
        updateTabSelection(state.currentTabIndex)
        state.appUpdateInfo?.let {
            if (it.hasUpdate){
                showUpdateDialog(it)
                sendIntent(MainActivityIntent.ClearUpdateInfo)
            }
        }
    }
    
    /**
     * 设置自定义底部导航栏点击事件
     */
    private fun setupCustomBottomNavigation() {
        // 消息
        binding.navHome.setOnClickListener {
            sendIntent(MainActivityIntent.SwitchTab(0))
        }
        
        // 梦境
        binding.navDream.setOnClickListener {
            sendIntent(MainActivityIntent.SwitchTab(1))
        }
        
        // 创建AI
        binding.navCreateAi.setOnClickListener {
            // 跳转到创建智能体页面
            val intent = android.content.Intent(this, CreateAIActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_CREATE_AI)
        }
        
        // 控制
        binding.navControl.setOnClickListener {
            sendIntent(MainActivityIntent.SwitchTab(2))
        }
        
        // 我的
        binding.navMy.setOnClickListener {
            sendIntent(MainActivityIntent.SwitchTab(3))
        }
    }
    
    /**
     * 更新底部导航栏选中状态
     */
    private fun updateTabSelection(selectedIndex: Int) {
        // 重置所有tab的状态
        resetAllTabs()
        
        // 设置选中tab的状态
        when (selectedIndex) {
            0 -> setTabSelected(binding.navHome, true)
            1 -> setTabSelected(binding.navDream, true)
            2 -> setTabSelected(binding.navControl, true)
            3 -> setTabSelected(binding.navMy, true)
        }
    }
    
    /**
     * 重置所有tab的状态
     */
    private fun resetAllTabs() {
        setTabSelected(binding.navHome, false)
        setTabSelected(binding.navDream, false)
        setTabSelected(binding.navControl, false)
        setTabSelected(binding.navMy, false)
    }
    
    /**
     * 设置单个tab的选中状态
     */
    private fun setTabSelected(tabLayout: android.view.View, isSelected: Boolean) {
        // 根据tabLayout的ID获取对应的图标和文字ID
        val iconId = when (tabLayout.id) {
            R.id.nav_home -> R.id.nav_home_icon
            R.id.nav_dream -> R.id.nav_dream_icon
            R.id.nav_control -> R.id.nav_control_icon
            R.id.nav_my -> R.id.nav_my_icon
            else -> return
        }
        
        val textId = when (tabLayout.id) {
            R.id.nav_home -> R.id.nav_home_text
            R.id.nav_dream -> R.id.nav_dream_text
            R.id.nav_control -> R.id.nav_control_text
            R.id.nav_my -> R.id.nav_my_text
            else -> return
        }
        
        val iconView = tabLayout.findViewById<android.widget.ImageView>(iconId)
        val textView = tabLayout.findViewById<android.widget.TextView>(textId)
        
        if (isSelected) {
            // 选中状态：白色图标和文字
            iconView?.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.color_ffffff))
            textView?.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.color_ffffff))
        } else {
            // 未选中状态：灰色图标和文字
            iconView?.setColorFilter(androidx.core.content.ContextCompat.getColor(this, R.color.color_6b6b6b))
            textView?.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.color_6b6b6b))
        }
    }

    /**
     * 判断是否应该设置虚拟导航栏适配
     */
    override fun shouldSetupNavigationBarAdapter(): Boolean {
        return true
    }
    
    /**
     * 设置虚拟导航栏高度适配
     */
    override fun setupNavigationBarAdapter() {
        // 为底部导航栏添加虚拟导航栏高度适配
        LayoutUtils.setupBottomNavigationPadding(this, R.id.bottom_navigation)
    }
    
    override fun onStart() {
        super.onStart()
        // 注册EventBus
        try {
            org.greenrobot.eventbus.EventBus.getDefault().register(this)
            android.util.Log.d("MainActivity", "EventBus注册成功")
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "EventBus注册失败: ${e.message}")
        }
    }
    
    override fun onStop() {
        super.onStop()
        // 注销EventBus
        try {
            org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "EventBus注销失败: ${e.message}")
        }
    }
    
    /**
     * 处理切换到MyFragment事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onSwitchToMyFragmentEvent(event: SwitchToMyFragmentEvent) {
        android.util.Log.d("MainActivity", "收到切换到MyFragment事件: ${event.message}, targetTabIndex: ${event.targetTabIndex}")
        try {
            sendIntent(MainActivityIntent.SwitchTab(event.targetTabIndex))
            android.util.Log.d("MainActivity", "成功发送SwitchTab Intent")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "发送SwitchTab Intent失败", e)
        }
    }
    
    /**
     * 处理Activity结果
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_CREATE_AI && resultCode == android.app.Activity.RESULT_OK) {
            data?.let { intent ->
                val aiCreated = intent.getBooleanExtra("ai_created", false)
                val switchToMyFragment = intent.getBooleanExtra("switch_to_my_fragment", false)
                
                android.util.Log.d("MainActivity", "收到CreateAIActivity结果: aiCreated=$aiCreated, switchToMyFragment=$switchToMyFragment")
                
                if (aiCreated && switchToMyFragment) {
                    // 切换到MyFragment
                    sendIntent(MainActivityIntent.SwitchTab(3))
                    android.util.Log.d("MainActivity", "通过ActivityResult切换到MyFragment")
                }
            }
        }
    }
    
    companion object {
        private const val REQUEST_CODE_CREATE_AI = 1001
    }
}
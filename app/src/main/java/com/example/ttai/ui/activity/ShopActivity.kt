package com.example.ttai.ui.activity

import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ttai.R
import com.example.ttai.databinding.ActivityShopBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.ShopItem
import com.example.ttai.ui.fragment.ShopItemsFragment
import com.example.ttai.ui.fragment.ShopOwnedFragment
import com.example.ttai.intent.ShopActivityIntent
import com.example.ttai.state.ShopActivityState
import com.example.ttai.ui.fragment.ShopBeautifyFragment
import com.example.ttai.ui.fragment.ShopToyFragment
import com.example.ttai.ui.fragment.ShopWroldBookFragment
import com.example.ttai.ui.vm.ShopActivityViewModel
import com.example.ttai.utils.LayoutUtils

/**
 * 商店页面Activity
 * 包含商品列表和已拥有商品两个Tab页面
 */
class ShopActivity : BaseMviActivity<ShopActivityIntent, ShopActivityState, ShopActivityViewModel, ActivityShopBinding>() {

    override val viewModel: ShopActivityViewModel by viewModels()
    override val binding by viewBinding { ActivityShopBinding.inflate(it) }

    // 跟踪当前选中的 tab 索引
    private var currentTabIndex = 0

    override fun setupViews() {
        // 设置 ViewPager2 适配器
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 5

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> ShopToyFragment()
                    1 -> ShopItemsFragment()
                    2 -> ShopBeautifyFragment()
                    3 -> ShopWroldBookFragment()
                    4 -> ShopOwnedFragment()
                    else -> ShopItemsFragment()
                }
            }
        }
        // 禁用 ViewPager2 滑动
        binding.viewPager.isUserInputEnabled = false
        setupCustomBottomNavigation()
        // 设置事件监听
        setupEventListeners()

        // 发送初始化Intent
        sendIntent(ShopActivityIntent.Initialize)
    }

    override fun render(state: ShopActivityState) {
        // 更新当前 tab 索引
        currentTabIndex = state.currentTabIndex
        // 切换 ViewPager2 页面
        binding.viewPager.setCurrentItem(state.currentTabIndex, false)
        // 更新自定义底部导航栏选中状态
        updateTabSelection(state.currentTabIndex)

        // 处理错误信息
        state.errorMessage?.let { errorMessage ->
            showError(errorMessage)
        }
    }



    /**
     * 设置事件监听器
     */
    private fun setupEventListeners() {
        // 返回按钮点击事件
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    /**
     * 更新商店商品列表UI
     */
    private fun updateShopItemsUI(shopItems: List<ShopItem>) {
        // 通知当前Fragment更新数据
        val currentFragment = supportFragmentManager.findFragmentByTag("f0")
        if (currentFragment is ShopItemsFragment) {
            currentFragment.updateShopItems(shopItems)
        }
    }

    /**
     * 显示空状态
     */
    private fun showEmptyState() {
        Toast.makeText(this, "暂无商品", Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示错误信息
     */
    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
    /**
     * 设置自定义底部导航栏点击事件
     */
    private fun setupCustomBottomNavigation() {
        // 玩具
        binding.tvShopToy.setOnClickListener {
            sendIntent(ShopActivityIntent.SwitchTab(0))
        }

        // 商品
        binding.tvShopItems.setOnClickListener {
            sendIntent(ShopActivityIntent.SwitchTab(1))
        }

        // 美化
        binding.tvShopBeautify.setOnClickListener {
            sendIntent(ShopActivityIntent.SwitchTab(2))
        }

        // 世界书
        binding.tvShopWorldBook.setOnClickListener {
            sendIntent(ShopActivityIntent.SwitchTab(3))
        }

        // 已拥有
        binding.tvShopOwned.setOnClickListener {
            sendIntent(ShopActivityIntent.SwitchTab(4))
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
            0 -> setTabSelected(binding.tvShopToy, true)
            1 -> setTabSelected(binding.tvShopItems, true)
            2 -> setTabSelected(binding.tvShopBeautify, true)
            3 -> setTabSelected(binding.tvShopWorldBook, true)
            4 -> setTabSelected(binding.tvShopOwned, true)
        }
    }

    /**
     * 重置所有tab的状态
     */
    private fun resetAllTabs() {
        setTabSelected(binding.tvShopToy, false)
        setTabSelected(binding.tvShopItems, false)
        setTabSelected(binding.tvShopBeautify, false)
        setTabSelected(binding.tvShopWorldBook, false)
        setTabSelected(binding.tvShopOwned, false)
    }

    /**
     * 设置单个tab的选中状态
     */
    private fun setTabSelected(textView: TextView, isSelected: Boolean) {
        if (isSelected) {
            // 选中状态：白色文字
            textView.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.color_ffffff))
        } else {
            // 未选中状态：灰色文字
            textView.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.color_6b6b6b))
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
        // 为输入框区域添加虚拟导航栏高度适配
        LayoutUtils.setupInputContainerPadding(this, R.id.viewPager)
    }

}
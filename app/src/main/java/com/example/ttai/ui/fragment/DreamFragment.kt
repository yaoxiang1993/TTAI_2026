package com.example.ttai.ui.fragment

import android.content.Intent
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.ttai.databinding.FragmentDreamBinding
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.state.DreamFragmentState

import androidx.fragment.app.viewModels
import com.example.ttai.R
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.DreamFragmentIntent
import com.example.ttai.ui.vm.DreamFragmentViewModel
import com.example.ttai.ui.vm.SharedDataViewModel

class DreamFragment : BaseMviFragment<DreamFragmentIntent, DreamFragmentState, DreamFragmentViewModel, FragmentDreamBinding>() {
    override val viewModel: DreamFragmentViewModel by viewModels()

    private val sharedViewModel: SharedDataViewModel by viewModels()
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentDreamBinding.inflate(inflater, container, attachToParent)
    }

    var isUnlimited = false
    // 跟踪当前选中的 tab 索引
    private var currentTabIndex = 0
    override fun setupViews() {
        setupCustomNavigation()
        setupViewPager()
        setupClickListeners()
        sendIntent(DreamFragmentIntent.Initialize)
        sendIntent(DreamFragmentIntent.SwitchTab(1))
    }

    private fun setupClickListeners() {
        binding.ivSearch.setOnClickListener {
            // 跳转到搜索页面
            val intent = Intent(requireContext(), com.example.ttai.ui.activity.SearchActivity::class.java)
            startActivity(intent)
        }
        binding.ivUnlimited.setOnClickListener {
            // 跳转到搜索页面
            isUnlimited = !isUnlimited
            sharedViewModel.setIsUnlimited(isUnlimited)
            binding.ivUnlimited.setImageResource(
                if(isUnlimited){
                    R.mipmap.icon_unlimited
                }else{
                    R.mipmap.icon_limited
                }
            )
        }
    }

    private fun setupViewPager() {
        // 创建 ViewPager 适配器
        val adapter = DreamPagerAdapter(this)
        binding.viewPager.adapter = adapter

        // 禁用ViewPager的左右滑动
        binding.viewPager.isUserInputEnabled = false

    }

    override fun render(state: DreamFragmentState) {
        super.render(state)
        currentTabIndex = state.currentTabIndex
        // 切换 ViewPager2 页面
        binding.viewPager.setCurrentItem(state.currentTabIndex, false)
        // 更新自定义底部导航栏选中状态
        updateTabSelection(state.currentTabIndex)
    }

    // ViewPager 适配器
    private inner class DreamPagerAdapter(fragment: DreamFragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SynthesizeFragment()
                1 -> FeaturedFragment()
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }
    }

    /**
     * 设置自定义底部导航栏点击事件
     */
    private fun setupCustomNavigation() {
        // 消息
        binding.tvSynthesize.setOnClickListener {
            sendIntent(DreamFragmentIntent.SwitchTab(0))
        }

        // 梦境
        binding.tvFeatured.setOnClickListener {
            sendIntent(DreamFragmentIntent.SwitchTab(1))
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
            0 -> setTabSelected(binding.tvSynthesize, true)
            1 -> setTabSelected(binding.tvFeatured, true)
        }
    }

    /**
     * 重置所有tab的状态
     */
    private fun resetAllTabs() {
        setTabSelected(binding.tvSynthesize, false)
        setTabSelected(binding.tvFeatured, false)
    }

    /**
     * 设置单个tab的选中状态
     */
    private fun setTabSelected(textView: TextView, isSelected: Boolean) {
        if (isSelected) {
            // 选中状态：白色文字
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_ffffff))
        } else {
            // 未选中状态：灰色文字
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_6b6b6b))
        }
    }

}
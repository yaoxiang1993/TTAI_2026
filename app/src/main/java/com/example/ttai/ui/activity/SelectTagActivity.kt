package com.example.ttai.ui.activity

import android.content.Intent
import android.widget.TextView
import com.example.ttai.utils.ToastUtils
import androidx.activity.viewModels
import com.example.ttai.databinding.ActivitySelectTagBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.SelectTagIntent
import com.example.ttai.state.SelectTagState
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.SelectTagViewModel
import com.example.ttai.ui.vm.SelectTagViewModelFactory

class SelectTagActivity : BaseMviActivity<SelectTagIntent, SelectTagState, SelectTagViewModel, ActivitySelectTagBinding>() {
    override val viewModel: SelectTagViewModel by viewModels { SelectTagViewModelFactory(this) }
    override val binding by viewBinding { ActivitySelectTagBinding.inflate(it) }

    override fun setupViews() {
        sendIntent(SelectTagIntent.Initialize)
    }

    override fun render(state: SelectTagState) {
        super.render(state)
        
        // 显示错误信息
        if (state.error != null) {
            ToastUtils.showShort(this, state.error)
        }
        
        // 显示偏好标签更新错误
        if (state.preferredTagsUpdateError != null) {
            ToastUtils.showLong(this, state.preferredTagsUpdateError)
        }
        
        // 显示加载状态
        if (state.isUpdatingPreferredTags) {
            binding.btnPlay.text = "更新中..."
            binding.btnPlay.isEnabled = false
        } else {
            binding.btnPlay.isEnabled = true
        }
        
        // 清空标签网格布局
        binding.tagsGridLayout.removeAllViews()
        
        // 根据API返回的标签动态添加到GridLayout
        state.tags?.forEachIndexed { index, tag ->
            // 创建标签TextView
            val tagView = TextView(this).apply {
                text = tag.name
                setBackgroundResource(com.example.ttai.R.drawable.bg_select_tag_selector)
                setTextColor(resources.getColorStateList(com.example.ttai.R.color.selector_ac00ff_ffffff, null))
                setPadding(16, 10, 16, 10)
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                isSelected = state.selectedTags.contains(tag.name)
                setOnClickListener {
                    sendIntent(SelectTagIntent.SelectTag(tag.name))
                }
            }
            
            // 添加到GridLayout
            val row = index / 4 // 4列布局，计算行索引
            val column = index % 4 // 计算列索引
            
            // 创建布局参数，使每个item均分宽度
            val params = androidx.gridlayout.widget.GridLayout.LayoutParams().apply {
                width = 0 // 使用0dp让GridLayout根据weight分配宽度
                height = androidx.gridlayout.widget.GridLayout.LayoutParams.WRAP_CONTENT
                setMargins(10, 15, 10, 15) // 设置10dp间隔：左右各5dp，上下各10dp
                rowSpec = androidx.gridlayout.widget.GridLayout.spec(row)
                columnSpec = androidx.gridlayout.widget.GridLayout.spec(column, 1f) // 使用weight=1f均分宽度
            }
            
            tagView.layoutParams = params
            binding.tagsGridLayout.addView(tagView)
        }

        // 绑定确认按钮
        binding.btnPlay.setOnClickListener {
            sendIntent(SelectTagIntent.ConfirmSelection)
        }
        
        // 处理跳转
        if (state.navigateToNext) {
            // 创建Intent并跳转到ChatActivity，标记为从SelectTagActivity进入
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(Constants.FROM_SELECT_TAG, true)
            // 如果有选中的标签，则传递选中的标签
            if (state.selectedTags.isNotEmpty()) {
                intent.putExtra(Constants.PERSONALITY_TAG, state.selectedTags)
            }
            startActivity(intent)
            finish()
        }
    }
}
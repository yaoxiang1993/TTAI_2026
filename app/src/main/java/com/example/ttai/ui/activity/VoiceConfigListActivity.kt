package com.example.ttai.ui.activity

import androidx.activity.viewModels
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.adapter.VoiceConfigAdapter
import com.example.ttai.bean.Character
import com.example.ttai.databinding.ActivityVoiceConfigListBinding
import com.example.ttai.intent.VoiceConfigIntent
import com.example.ttai.state.VoiceConfigState
import com.example.ttai.ui.vm.VoiceConfigModelFactory
import com.example.ttai.ui.vm.VoiceConfigViewModel
import com.example.ttai.utils.Constants
import com.example.ttai.utils.GridSpacingItemDecoration
import com.example.ttai.utils.ToastUtils

/**
 * 粉丝列表
 * */
class VoiceConfigListActivity : BaseMviActivity<VoiceConfigIntent, VoiceConfigState, VoiceConfigViewModel, ActivityVoiceConfigListBinding>() {
    override val viewModel: VoiceConfigViewModel by viewModels { VoiceConfigModelFactory(this) }
    override val binding by viewBinding { ActivityVoiceConfigListBinding.inflate(it) }

    private val characterId by lazy { intent.getStringExtra (Constants.CHARACTER_ID_KEY) }


    private lateinit var adapter:  VoiceConfigAdapter

    override fun setupViews() {
        setupRecyclerView()
        setupClickListeners()
        sendIntent(VoiceConfigIntent.Initialize(characterId) )
    }

    private fun setupRecyclerView() {
        // 设置模型2的RecyclerView
        adapter = VoiceConfigAdapter(emptyList()) { item ->
            sendIntent(VoiceConfigIntent.SelectItem(item.voiceCode ))
        }
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@VoiceConfigListActivity.adapter  // 设置adapter
            val spacing = (4 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
            // 添加滚动监听器来处理上拉加载更多

        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.tvSave.setOnClickListener {
            sendIntent(VoiceConfigIntent.SaveVoiceConfig)
        }
    }

    override fun render(state: VoiceConfigState) {
        super.render(state)
        // 更新搜索结果
        adapter.updateItems(state.voiceConfigData?.voiceOptions)
        if(state.saveVoiceSuccess){
            ToastUtils.showError(this,"保存角色音色配置成功")
            finish()
        }
    }


}

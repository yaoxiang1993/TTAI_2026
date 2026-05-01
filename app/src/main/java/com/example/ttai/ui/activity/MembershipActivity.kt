package com.example.ttai.ui.activity

import com.example.ttai.utils.ToastUtils
import androidx.activity.viewModels
import com.example.ttai.databinding.ActivityMembershipBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.MembershipIntent
import com.example.ttai.state.MembershipState
import com.example.ttai.ui.vm.MembershipViewModel
import com.example.ttai.ui.vm.MembershipViewModelFactory
import com.example.ttai.adapter.MembershipAdapter
import com.example.ttai.adapter.HorizontalSpaceItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.R
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.event.UpdateUserProfileEvent
import org.greenrobot.eventbus.EventBus

class MembershipActivity : BaseMviActivity<MembershipIntent, MembershipState, MembershipViewModel, ActivityMembershipBinding>() {
    override val viewModel: MembershipViewModel by viewModels { 
        MembershipViewModelFactory(this)
    }
    override val binding by viewBinding { ActivityMembershipBinding.inflate(it) }
    
    // 适配器
    private lateinit var model1Adapter: MembershipAdapter
    private lateinit var model2Adapter: MembershipAdapter

    override fun setupViews() {
        
        // 设置返回按钮点击事件
        binding.ivBack.setOnClickListener {
            finish()
        }
        
        // 设置开通按钮点击事件
        binding.btnSubscribe.setOnClickListener {
            subscribeToMembership()
        }
        
        // 初始化RecyclerView
        setupRecyclerViews()
        
        sendIntent(MembershipIntent.Initialize)
        sendIntent(MembershipIntent.LoadUserProfile)
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerViews() {
        // 设置模型1的RecyclerView
        model1Adapter = MembershipAdapter(emptyList()) { item ->
            sendIntent(MembershipIntent.SelectItem(item.id, "model1"))
        }
        binding.mRecyclerViewModel1.apply {
            layoutManager = LinearLayoutManager(this@MembershipActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = model1Adapter
            // 添加调试日志
            android.util.Log.d("MembershipActivity", "设置model1 RecyclerView")
        }
        
        // 设置模型2的RecyclerView
        model2Adapter = MembershipAdapter(emptyList()) { item ->
            sendIntent(MembershipIntent.SelectItem(item.id, "model2"))
        }
        binding.mRecyclerViewModel2.apply {
            layoutManager = LinearLayoutManager(this@MembershipActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = model2Adapter
            // 添加调试日志
            android.util.Log.d("MembershipActivity", "设置model2 RecyclerView")
        }
        
        // 添加10dp的item间距
        val space10dp = (resources.displayMetrics.density * 10).toInt()
        binding.mRecyclerViewModel1.addItemDecoration(HorizontalSpaceItemDecoration(space10dp))
        binding.mRecyclerViewModel2.addItemDecoration(HorizontalSpaceItemDecoration(space10dp))
    }
    
    /**
     * 开通会员
     */
    private fun subscribeToMembership() {
        val currentState = viewModel.state.value
        val selectedItemId = currentState.selectedItemId
        val selectedModelType = currentState.selectedModelType
        
        if (selectedItemId.isNotEmpty()) {
            val dialog = TwoButtonDialogFragment.newInstance(
                message = "是否确认开通会员",
                positiveText = "确认",
                negativeText = "取消"
            ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
                override fun onPositiveClick() {
                    // 执行购买操作
                    sendIntent(MembershipIntent.Subscribe(selectedItemId, "会员套餐"))
                }

                override fun onNegativeClick() {
                }
            })
            dialog.show(supportFragmentManager, "SubscribeDialog")

        } else {
            ToastUtils.showShort(this, "请先选择一个会员套餐")
        }
    }

    override fun render(state: MembershipState) {
        super.render(state)
        
        // 处理加载状态
        binding.btnSubscribe.isEnabled = !state.isLoading
        
        // 处理用户资料加载状态
        if (state.isLoadingUserProfile) {
            // 可以显示加载指示器
            binding.tvName.text = "加载中..."
            binding.tvVipTag.text = "加载中..."
            binding.tvTime.text = "加载中..."
        } else {
            // 更新用户信息
            binding.tvName.text = state.userName
            binding.tvVipTag.text = state.vipTag
            binding.tvTime.text = state.expireTime
            Glide.with(this)
                .load(state.userProfile?.avatarUrl)
                .placeholder(R.mipmap.icon_defult_avatar)
                .error(R.mipmap.icon_defult_avatar)
                .transform(CenterCrop(), CircleCrop())
                .into(binding.ivHeard)

        }
        
        // 更新RecyclerView数据
        android.util.Log.d("MembershipActivity", "render - model1Items数量: ${state.model1Items.size}")
        android.util.Log.d("MembershipActivity", "render - model2Items数量: ${state.model2Items.size}")

        binding.tvModel1.text = state.model1Title
        binding.tvModel2.text = state.model2Title

        if (state.model1Items.isNotEmpty()) {
            android.util.Log.d("MembershipActivity", "更新model1Adapter")
            model1Adapter.updateItems(state.model1Items)
        }
        if (state.model2Items.isNotEmpty()) {
            android.util.Log.d("MembershipActivity", "更新model2Adapter")
            model2Adapter.updateItems(state.model2Items)
        }

        // 显示错误信息
        state.error?.let { error ->
            ToastUtils.showShort(this, error)
        }

        // 处理开通成功
        if (state.isSubscriptionSuccessful) {
            ToastUtils.showLong(this, "会员开通成功！")
            EventBus.getDefault().post(UpdateUserProfileEvent())
            finish()
        }
    }
    
} 
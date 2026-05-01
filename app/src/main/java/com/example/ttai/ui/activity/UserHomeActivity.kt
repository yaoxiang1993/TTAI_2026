package com.example.ttai.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ttai.R
import com.example.ttai.adapter.SynthesizeAdapter
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.databinding.ActivityUserHomeBinding
import com.example.ttai.intent.UserHomeIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.UserHomeState
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.UserHomeViewModel
import com.example.ttai.ui.vm.UserHomeViewModelFactory
import com.example.ttai.utils.GridSpacingItemDecoration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class UserHomeActivity : BaseMviActivity<UserHomeIntent, UserHomeState, UserHomeViewModel, ActivityUserHomeBinding>() {
    override val viewModel: UserHomeViewModel by viewModels {
        UserHomeViewModelFactory(NetworkModule.provideApiService(), this)
    }
    override val binding by viewBinding { ActivityUserHomeBinding.inflate(it) }

    private val adapter = SynthesizeAdapter(
        onItemClick = { item ->
            if (item.conversationId.isNullOrEmpty()){
                val intent = Intent(this, AIDetailsActivity::class.java).apply {
                    putExtras(Bundle().apply{
                        putExtra(Constants.CHARACTER_KEY, item)
                    })
                }
                startActivity(intent)
            }else{
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra(Constants.CHARACTER_ID_KEY, item.id)
                    putExtra(Constants.CONVERSATION_ID_KEY, item.conversationId)
                }
                startActivity(intent)
            }
        }
    )
    private var searchJob: Job? = null

    override fun setupViews() {
        try {
            EventBus.getDefault().register(this)
            Log.d("AISettingActivity", "EventBus注册成功")
        } catch (e: Exception) {
            Log.w("AISettingActivity", "EventBus注册失败: ${e.message}")
        }

        val userId = intent.getStringExtra(Constants.USER_ID_KEY)?:""
        setupRecyclerView()
        setupSearchListener()
        setupClickListeners()
        sendIntent(UserHomeIntent.Initialize(userId))
    }
    private fun setupClickListeners() {

        binding.tvFollow.setOnClickListener {
            sendIntent(UserHomeIntent.FollowSwitch)
        }
    }
    private fun setupSearchListener() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""

                // 取消之前的搜索任务
                searchJob?.cancel()

                if (query.isNotEmpty()) {
                    // 延迟300毫秒后执行搜索
                    searchJob = lifecycleScope.launch {
                        delay(300) // 300毫秒延迟
                        sendIntent(UserHomeIntent.PerformSearch(query))
                    }
                } else {
                    sendIntent(UserHomeIntent.ClearSearch)
                }
            }
        })
    }

    override fun render(state: UserHomeState) {
        super.render(state)
        state.profile?.let {
            binding.tvName.text = it.username
            binding.tvId.text = it.id
            binding.tvBriefingNote.text = it.bio
            binding.tvFans.text = it.stats?.followersCount
            binding.tvFocusNumber.text = it.stats?.followingCount

            if (!TextUtils.isEmpty(it.avatarUrl)){
                Glide.with(this)
                    .load(it.avatarUrl)
                    .placeholder(R.mipmap.icon_heard)
                    .error(R.mipmap.icon_heard)
                    .circleCrop()
                    .into(binding.ivHeard)
            }
        }
        binding.tvFollow.isVisible = state.can_follow == true

        if (state.can_follow == true){
            if (state.is_follow == true){
                binding.tvFollow.text ="已关注"
                binding.tvFollow.setBackgroundResource(R.drawable.bg_9a9b9f_r50)
            }else{
                binding.tvFollow.text ="关注"
                binding.tvFollow.setBackgroundResource(R.drawable.bg_gradient_b2a4f9_e5b1fb)
            }
        }
        // 更新搜索结果
        adapter.submitList(state.items)
        binding.tvRole.text ="角色(${state.items?.size})"

    }

    private fun setupRecyclerView() {
        binding.mRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@UserHomeActivity.adapter  // 设置adapter
            val spacing = (4 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))

            android.util.Log.d("SynthesizeFragment", "RecyclerView setup completed")

            // 添加滚动监听器来处理上拉加载更多
           /* addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItemPosition >= totalItemCount - 3 &&
                        !viewModel.state.value.isLoadingMore &&
                        viewModel.state.value.hasMoreData
                    ) {
                        sendIntent(UserHomeIntent.LoadMoreData)
                    }
                }
            })*/
        }
    }
}
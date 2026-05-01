package com.example.ttai.ui.fragment

import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.databinding.FragmentSynthesizeBinding
import com.example.ttai.ui.activity.AIBriefActivity
import com.example.ttai.adapter.SynthesizeAdapter
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.event.ChatSlotChangedEvent
import com.example.ttai.intent.SynthesizeFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.SynthesizeFragmentState
import com.example.ttai.ui.vm.SharedDataViewModel
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.SynthesizeFragmentViewModel
import com.example.ttai.ui.vm.SynthesizeFragmentViewModelFactory
import com.example.ttai.utils.GridSpacingItemDecoration
import com.google.android.material.snackbar.Snackbar

/**
 *
 * 梦境中的 综合分类
 * */
class SynthesizeFragment :
    BaseMviFragment<SynthesizeFragmentIntent, SynthesizeFragmentState, SynthesizeFragmentViewModel, FragmentSynthesizeBinding>() {
    override val viewModel: SynthesizeFragmentViewModel by viewModels {
        SynthesizeFragmentViewModelFactory(NetworkModule.provideApiService(), requireContext())
    }
    private val sharedViewModel: SharedDataViewModel by viewModels({ requireParentFragment() })
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentSynthesizeBinding.inflate(inflater, container, attachToParent)
    }
    val RANDOM="random"
    val POPULARITY="popularity"
    val sortOrderUp="1"
    val sortOrderDown="-1"
    var sortBy:String= RANDOM
    var sortOrder:String= "" //支持升序（sort_order=1）和降序（sort_order=-1）

    private val synthesizeAdapter = SynthesizeAdapter(
        onItemClick = { item ->
            // 跳转到 AI 简介页面
            try {
                val intent = Intent(requireContext(), AIBriefActivity::class.java).apply {
                    putExtra(Constants.CHARACTER_KEY, item)
                }
                startActivity(intent)
                Log.d("SynthesizeFragment", "AIBriefActivity launched successfully")
            } catch (e: Exception) {
                Log.e("SynthesizeFragment", "Failed to launch AIBriefActivity", e)
                // 显示错误提示
                Snackbar.make(binding.root, "跳转失败: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
        }
    )

    override fun setupViews() {
        try {
            org.greenrobot.eventbus.EventBus.getDefault().register(this)
            Log.d("SynthesizeFragment", "EventBus注册成功")
        } catch (e: Exception) {
            Log.w("SynthesizeFragment", "EventBus注册失败: ${e.message}")
        }
        sharedViewModel.isUnlimited.observe(viewLifecycleOwner) { isUnlimited ->
            // 当 isUnlimited 变化时，将新值转发给 B 自己的 ViewModel
            viewModel.updateIsUnlimitedParam(isUnlimited)
            sendIntent(SynthesizeFragmentIntent.Initialize(sortBy,sortOrder))
        }

        binding.tvAll.setOnClickListener {
            sortBy = RANDOM
            sortOrder = ""
            sendIntent(SynthesizeFragmentIntent.RefreshData(sortBy,sortOrder))
        }
        binding.tvHot.setOnClickListener {
            sortBy = POPULARITY
            sortOrder = sortOrderDown
            sendIntent(SynthesizeFragmentIntent.RefreshData(sortBy,sortOrder))
        }
        setupRecyclerView()
        setupSwipeRefresh()
        sendIntent(SynthesizeFragmentIntent.Initialize(sortBy,sortOrder))
    }

    override fun onResume() {
        super.onResume()
        sendIntent(SynthesizeFragmentIntent.RefreshData(sortBy,sortOrder))
    }

    private fun setupRecyclerView() {
        binding.mRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = synthesizeAdapter

            // 添加 item 间距装饰器 - 使用专门的GridSpacingItemDecoration
            val spacing = (4 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))

            Log.d("SynthesizeFragment", "RecyclerView setup completed")

            // 添加滚动监听器来处理上拉加载更多
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    // 检查是否需要加载更多
                    if (lastVisibleItemPosition >= totalItemCount - 3 &&
                        !viewModel.state.value.isLoadingMore &&
                        !viewModel.state.value.isRefreshing &&
                        viewModel.state.value.hasMoreData
                    ) {
                        Log.d("SynthesizeFragment", "Triggering load more: lastVisible=$lastVisibleItemPosition, total=$totalItemCount")
                        sendIntent(SynthesizeFragmentIntent.LoadMoreData(sortBy = sortBy,sortOrder))
                    }
                }
            })
        }
    }

    private fun setupSwipeRefresh() {
        // 设置下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d("SynthesizeFragment", "Swipe refresh triggered")
            sendIntent(SynthesizeFragmentIntent.RefreshData(sortBy = sortBy,sortOrder))
        }
        
        // 设置刷新颜色
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    override fun render(state: SynthesizeFragmentState) {
        super.render(state)

        android.util.Log.d("SynthesizeFragment", "Rendering state: items=${state.items?.size}, isLoading=${state.isLoading}, isRefreshing=${state.isRefreshing}, isLoadingMore=${state.isLoadingMore}")

        // 更新适配器数据
        if (state.items != null) {
            android.util.Log.d("SynthesizeFragment", "Updating adapter with ${state.items.size} items")
//            synthesizeAdapter.submitList(state.items)
            synthesizeAdapter.submitList(state.items) {
                // 如果当前是第一页（由刷新或切换排序触发），则滚动到顶部
                android.util.Log.d("Synthesize", " YXTEST state.currentPage ${state.currentPage}  ")
                if (state.currentPage == 1) {
                    binding.mRecyclerView.scrollToPosition(0)
                }
            }
            Log.d("SynthesizeFragment", "Adapter updated successfully")
        } else {
            Log.d("SynthesizeFragment", "Items is null, clearing adapter")
            synthesizeAdapter.submitList(emptyList())
        }

        // 处理刷新状态
        if (state.isRefreshing) {
            Log.d("SynthesizeFragment", "Showing refresh loading")
            // 下拉刷新时，SwipeRefreshLayout 会自动显示刷新指示器
        } else {
            // 刷新完成，停止刷新指示器
            binding.swipeRefreshLayout.isRefreshing = false
        }

        if (state.sortBy == RANDOM){
            binding.tvAll.setTextColor(ContextCompat.getColor(requireContext(), com.example.ttai.R.color.color_b4a5f9))
            binding.tvHot.setTextColor(ContextCompat.getColor(requireContext(), com.example.ttai.R.color.color_ffffff))
        }else{
            binding.tvAll.setTextColor(ContextCompat.getColor(requireContext(), com.example.ttai.R.color.color_ffffff))
            binding.tvHot.setTextColor(ContextCompat.getColor(requireContext(), com.example.ttai.R.color.color_b4a5f9))
        }

        if (state.isLoadingMore) {
            android.util.Log.d("SynthesizeFragment", "Showing load more loading")
            // 显示加载更多指示器
            synthesizeAdapter.setLoadingMore(true)
        } else {
            // 隐藏加载更多指示器
            synthesizeAdapter.setLoadingMore(false)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 注销EventBus
        try {
            org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
            Log.d("MainFragment", "EventBus注销成功")
        } catch (e: Exception) {
            Log.w("MainFragment", "EventBus注销失败: ${e.message}")
        }
    }
    /**
     * 处理聊天槽位变化事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onChatSlotChanged(event: ChatSlotChangedEvent) {
        Log.d("MainFragment", "收到聊天槽位变化事件: action=${event.action}")

        // 当聊天槽位发生变化时，重新初始化数据
        sendIntent(SynthesizeFragmentIntent.Initialize(sortBy,sortOrder))
    }
}
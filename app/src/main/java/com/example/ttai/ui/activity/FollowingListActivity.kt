package com.example.ttai.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.utils.Constants
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.adapter.FollowingAdapter
import com.example.ttai.databinding.ActivityFollowingListBinding
import com.example.ttai.intent.FollowingListIntent
import com.example.ttai.state.FollowingListState
import com.example.ttai.ui.vm.FollowingListViewModel
import com.example.ttai.ui.vm.FocusListViewModelFactory
import com.example.ttai.utils.GridSpacingItemDecoration

/**
 * 关注列表
 * */
class FollowingListActivity : BaseMviActivity<FollowingListIntent, FollowingListState, FollowingListViewModel, ActivityFollowingListBinding>() {
    override val viewModel: FollowingListViewModel by viewModels { FocusListViewModelFactory(this) }
    override val binding by viewBinding { ActivityFollowingListBinding.inflate(it) }
    private val adapter = FollowingAdapter(
        onItemClick = { item ->
            item.user_id?.let {
                val intent = Intent(this, UserHomeActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putExtra(Constants.USER_ID_KEY, item.user_id)
                    })
                }
                startActivity(intent)
            }
        },
        onMutualClick = {item ->
            if (item.isFollowing){
                sendIntent(FollowingListIntent.UnFollow(item.user_id))
            }else{
                sendIntent(FollowingListIntent.Follow(item.user_id))
            }
        }
    )
    private var searchJob: Job? = null

    override fun setupViews() {
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        sendIntent(FollowingListIntent.Initialize)
    }

    private fun setupRecyclerView() {
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FollowingListActivity.adapter  // 设置adapter
            val spacing = (4 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))

            Log.d("SynthesizeFragment", "RecyclerView setup completed")

            // 添加滚动监听器来处理上拉加载更多
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItemPosition >= totalItemCount - 3 &&
                        !viewModel.state.value.isLoadingMore &&
                        viewModel.state.value.hasMoreData
                    ) {
                        sendIntent(FollowingListIntent.LoadMoreData)
                    }
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.tvBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearchListener() {
        binding.edtPhone.addTextChangedListener(object : TextWatcher {
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
                        sendIntent(FollowingListIntent.PerformSearch(query))
                    }
                } else {
                    sendIntent(FollowingListIntent.ClearSearch)
                }
            }
        })
    }

    override fun render(state: FollowingListState) {
        super.render(state)

        Log.d("FollowingListActivity", "render: searchResults size = ${state.searchResults.size}")
        Log.d("FollowingListActivity", "render: searchResults = ${state.searchResults}")

        binding.tvNoData.isVisible = state.searchResults.isEmpty()
        // 更新搜索结果
        adapter.submitList(state.searchResults)

        // 显示错误信息
        state.error?.let { error ->
            // 显示错误提示
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 取消所有搜索任务
        searchJob?.cancel()
    }

}

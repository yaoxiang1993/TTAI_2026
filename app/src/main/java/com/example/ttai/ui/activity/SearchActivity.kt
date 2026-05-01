package com.example.ttai.ui.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.example.ttai.databinding.ActivitySearchBinding
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.SearchIntent
import com.example.ttai.state.SearchState
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.SearchViewModel
import com.example.ttai.ui.vm.SearchViewModelFactory
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.adapter.SynthesizeAdapter
import com.example.ttai.utils.GridSpacingItemDecoration

class SearchActivity : BaseMviActivity<SearchIntent, SearchState, SearchViewModel, ActivitySearchBinding>() {
    override val viewModel: SearchViewModel by viewModels { SearchViewModelFactory(this) }
    override val binding by viewBinding { ActivitySearchBinding.inflate(it) }
    

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
        setupRecyclerView()
        setupClickListeners()
        setupSearchListener()
        sendIntent(SearchIntent.Initialize)
    }

    private fun setupRecyclerView() {
        binding.mRecyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@SearchActivity.adapter  // 设置adapter
            val spacing = (4 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))

            android.util.Log.d("SynthesizeFragment", "RecyclerView setup completed")

            // 添加滚动监听器来处理上拉加载更多
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    val totalItemCount = layoutManager.itemCount

                    if (lastVisibleItemPosition >= totalItemCount - 3 &&
                        !viewModel.state.value.isLoadingMore &&
                        viewModel.state.value.hasMoreData
                    ) {
                        sendIntent(SearchIntent.LoadMoreData)
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
                        sendIntent(SearchIntent.PerformSearch(query))
                    }
                } else {
                    sendIntent(SearchIntent.ClearSearch)
                }
            }
        })
    }

    override fun render(state: SearchState) {
        super.render(state)

        android.util.Log.d("SearchActivity", "render: searchResults size = ${state.searchResults.size}")
        android.util.Log.d("SearchActivity", "render: searchResults = ${state.searchResults}")

        binding.tvNoData.isVisible = state.searchResults.isEmpty()
        // 更新搜索结果
        adapter.submitList(state.searchResults)
        
        // 显示加载状态
        if (state.isSearching) {
            // 显示搜索中状态
            binding.edtPhone.hint = "搜索中..."
        } else {
            binding.edtPhone.hint = "搜索关键词"
        }
        
        // 显示错误信息
        state.error?.let { error ->
            // 显示错误提示
            android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 取消所有搜索任务
        searchJob?.cancel()
    }

}

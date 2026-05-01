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
import com.example.ttai.adapter.FollowersAdapter
import com.example.ttai.adapter.FollowingAdapter
import com.example.ttai.adapter.NotifyListAdapter
import com.example.ttai.databinding.ActivityFollowersListBinding
import com.example.ttai.databinding.ActivityNotifyListBinding
import com.example.ttai.intent.FollowersListIntent
import com.example.ttai.intent.NotifyListIntent
import com.example.ttai.state.FollowersListState
import com.example.ttai.state.NotifyListState
import com.example.ttai.ui.vm.FollowersListViewModel
import com.example.ttai.ui.vm.FancesListViewModelFactory
import com.example.ttai.ui.vm.NotifyListViewModel
import com.example.ttai.ui.vm.NotifyListViewModelFactory
import com.example.ttai.utils.GridSpacingItemDecoration

/**
 * 消息通知列表
 * */
class NotifyListActivity : BaseMviActivity<NotifyListIntent, NotifyListState, NotifyListViewModel, ActivityNotifyListBinding>() {
    override val viewModel: NotifyListViewModel by viewModels { NotifyListViewModelFactory(this) }
    override val binding by viewBinding { ActivityNotifyListBinding.inflate(it) }

    private val adapter = NotifyListAdapter(
        onItemClick = { item ->
                val intent = Intent(this, NotifyDetailsActivity::class.java).apply {
                    putExtras(Bundle().apply {
                        putParcelable(Constants.OBJECT_KEY, item)
                    })
                }
                startActivity(intent)
        },
        onMutualClick = { item ->

        }
    )

    override fun setupViews() {
        setupRecyclerView()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        sendIntent(NotifyListIntent.Initialize)
    }

    private fun setupRecyclerView() {
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NotifyListActivity.adapter  // 设置adapter
            val spacing = (4 * resources.displayMetrics.density).toInt()
            addItemDecoration(GridSpacingItemDecoration(2, spacing, true))
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
                        sendIntent(NotifyListIntent.LoadMoreData)
                    }
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun render(state: NotifyListState) {
        super.render(state)
        Log.e("YXTEST"," render list size ${state.list?.size}")
        binding.tvNoData.isVisible = state.list?.isEmpty() == true
        // 更新搜索结果
        adapter.submitList(state.list)
        // 显示错误信息
        state.error?.let { error ->
            // 显示错误提示
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        }
    }
}

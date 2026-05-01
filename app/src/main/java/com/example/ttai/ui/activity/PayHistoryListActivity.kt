package com.example.ttai.ui.activity

import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import kotlinx.coroutines.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.adapter.PayHistoryListAdapter
import com.example.ttai.databinding.ActivityPayHistoryListBinding
import com.example.ttai.intent.PayHistoryListIntent
import com.example.ttai.state.PayHistoryState
import com.example.ttai.ui.fragment.YearMonthPickerDialogFragment
import com.example.ttai.ui.vm.PayHistoryListViewModel
import com.example.ttai.ui.vm.PayHistoryListViewModelFactory
import com.example.ttai.utils.DateUtils
import com.example.ttai.utils.GridSpacingItemDecoration

/**
 * 账单明细
 * */
class PayHistoryListActivity : BaseMviActivity<PayHistoryListIntent, PayHistoryState, PayHistoryListViewModel, ActivityPayHistoryListBinding>() {
    override val viewModel: PayHistoryListViewModel by viewModels {
        PayHistoryListViewModelFactory(
            this
        )
    }
    override val binding by viewBinding { ActivityPayHistoryListBinding.inflate(it) }

    var currentYear:Int =0
    var currentMonth : Int =0
    private val adapter = PayHistoryListAdapter()
    private var searchJob: Job? = null

    override fun setupViews() {
        setupRecyclerView()
        setupClickListeners()
        currentYear = DateUtils.getCurrentYear()
        currentMonth = DateUtils.getCurrentMonth()
        binding.tvDate.text = "${currentYear}年${currentMonth}月"
        sendIntent(PayHistoryListIntent.Initialize(currentYear, currentMonth))
    }

    private fun setupRecyclerView() {
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@PayHistoryListActivity.adapter  // 设置adapter
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
                        sendIntent(PayHistoryListIntent.LoadMoreData(currentYear,currentMonth))
                    }
                }
            })
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.tvDate.setOnClickListener {
            val dialog = YearMonthPickerDialogFragment.newInstance { year, month ->
                currentYear = year
                currentMonth = month
                Toast.makeText(this, "选中的年月: $year 年 $month 月", Toast.LENGTH_SHORT).show()
                // 这里可以更新 UI 或保存数据，例如：updateUI(year, month)
                sendIntent(PayHistoryListIntent.Initialize(year,month))
                binding.tvDate.text = "${year}年${month}月"
            }
            dialog.show(supportFragmentManager, "YearMonthPicker")
        }
    }

    override fun render(state: PayHistoryState) {
        super.render(state)
        Log.e("YXTEST"," render payHistoryList size ${state.payHistoryList.size}")
        adapter.submitList(state.payHistoryList)
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

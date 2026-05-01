package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.PayHistoryListIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.PayHistoryState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PayHistoryListViewModel(private val context: Context) : MviViewModel<PayHistoryListIntent, PayHistoryState>() {
    private val _state = MutableStateFlow(PayHistoryState())
    override val state: StateFlow<PayHistoryState> = _state.asStateFlow()

    private var currentPage = 1
    private val payRepository = NetworkModule.providePayRepository(context)
    override fun processIntent(intent: PayHistoryListIntent) {
        viewModelScope.launch {
            when (intent) {
                is PayHistoryListIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = true)
                    getPayHistory(1, year = intent.year, month = intent.month)
                }
                is PayHistoryListIntent.LoadMoreData -> {
                    val nextPage = currentPage + 1
                    if (nextPage > _state.value.total_pages){
                        ToastUtils.showError(context, "已显示全部账单")
                    } else{
                        // *** 修正: 加载更多应该使用 isLoadingMore ***
                        _state.value = _state.value.copy(isLoadingMore = true)
                        getPayHistory(nextPage, year=intent.year,month=intent.month, isLoadingMore = true)
                    }
                }
            }
        }
    }

    fun getPayHistory(page: Int = 1,limit: Int = 50,year:Int?, month:Int?,  isLoadingMore: Boolean = false){
        viewModelScope.launch {
            try {
                val payHistoryResponse =
                    payRepository.getPayHistory(page,limit,year = year, month = month)
                if (page ==1 ){
                    _state.value = _state.value.copy(
                        isLoading = false,
                        payHistoryList = payHistoryResponse.bills ,
                        isLoadingMore = false,
                        hasMoreData = payHistoryResponse.pagination.page < payHistoryResponse.pagination.totalPages,
                        total_pages = payHistoryResponse.pagination.totalPages
                    )
                } else{
                    val currentItems = _state.value.payHistoryList.toMutableList()
                    currentItems.addAll(payHistoryResponse.bills)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        payHistoryList = currentItems,
                        isLoadingMore = false,
                        hasMoreData = payHistoryResponse.pagination.page < payHistoryResponse.pagination.totalPages,
                        total_pages = payHistoryResponse.pagination.totalPages
                    )
                }
                currentPage = payHistoryResponse.pagination.page
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}

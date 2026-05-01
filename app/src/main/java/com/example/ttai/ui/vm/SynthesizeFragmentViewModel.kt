package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.SynthesizeFragmentIntent
import com.example.ttai.state.SynthesizeFragmentState
import com.example.ttai.network.ApiService
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * 综合
 * */
class SynthesizeFragmentViewModel(
    private val apiService: ApiService,
    private val context: Context
) : MviViewModel<SynthesizeFragmentIntent, SynthesizeFragmentState>() {
    private val characterRepository = CharacterRepository(apiService, context)
    private val _state = MutableStateFlow(SynthesizeFragmentState())
    override val state: StateFlow<SynthesizeFragmentState> = _state.asStateFlow()
    private var latestIsUnlimited: Boolean = false // 或者使用 LiveData 存储

    fun updateIsUnlimitedParam(isLimited: Boolean) {
        latestIsUnlimited = isLimited
    }

    override fun processIntent(intent: SynthesizeFragmentIntent) {
        viewModelScope.launch {
            android.util.Log.d("SynthesizeViewModel", "Processing intent: $intent")
            when (intent) {
                is SynthesizeFragmentIntent.Initialize -> {
                    android.util.Log.d("SynthesizeViewModel", "Initializing...")
                    loadComprehensiveCharacters(sortBy=intent.sortBy, sortOrder = intent.sortOrder)
                }
                is SynthesizeFragmentIntent.RefreshData -> {
                    android.util.Log.d("SynthesizeViewModel", "Refreshing data...")
                    loadComprehensiveCharacters(sortBy=intent.sortBy, sortOrder = intent.sortOrder,isRefreshing = true)
                }
                is SynthesizeFragmentIntent.LoadMoreData -> {
                    android.util.Log.d("SynthesizeViewModel", "Loading more data...")
                    loadComprehensiveCharacters(_state.value.currentPage + 1,sortBy=intent.sortBy, sortOrder = intent.sortOrder, isLoadingMore = true)
                }
            }
        }
    }
    /**
     * 加载综合角色列表数据（使用新的API）
     */
    private suspend fun loadComprehensiveCharacters(
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "",
        sortOrder: String = "",
        isRefreshing: Boolean = false,
        isLoadingMore: Boolean = false
    ) {
        try {
            // 设置相应的加载状态
            if (isRefreshing) {
                _state.value = _state.value.copy(isRefreshing = true, currentPage=page,sortBy = sortBy, error = null)
            } else if (isLoadingMore) {
                _state.value = _state.value.copy(isLoadingMore = true,  currentPage=page,sortBy = sortBy, error = null)
            } else {
                _state.value = _state.value.copy(isLoading = true, currentPage=page, sortBy = sortBy, error = null)
            }
            
            val response = characterRepository.getComprehensiveCharacters(
                page = page,
                limit = limit,
                sortBy = sortBy,
                sortOrder = sortOrder,
                is_unlimited = latestIsUnlimited
            )
            android.util.Log.d("SynthesizeViewModel", " YXTEST ${response.page}  ${response.totalPages}")
            if (page == 1) {
                // 第一页，直接替换数据
                _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    items = response.characters,
                    currentPage = response.page,
                    hasMoreData = response.page < response.totalPages,
                    error = null
                )
            } else {
                // 加载更多，追加数据
                val currentItems = _state.value.items?.toMutableList()
                currentItems?.addAll(response.characters)
                _state.value = _state.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    items = currentItems,
                    currentPage = response.page,
                    hasMoreData = response.page < response.totalPages,
                    error = null
                )
            }
        } catch (e: Exception) {
            // 其他异常
            ToastUtils.showError(context, "加载综合角色列表失败")
            _state.value = _state.value.copy(
                isLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                error = " "
            )
        }
    }
} 
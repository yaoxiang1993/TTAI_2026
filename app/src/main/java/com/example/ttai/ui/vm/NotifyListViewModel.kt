package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.FollowersListIntent
import com.example.ttai.intent.FollowingListIntent
import com.example.ttai.intent.NotifyListIntent
import com.example.ttai.intent.SynthesizeFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.exception.ApiException
import com.example.ttai.state.FollowersListState
import com.example.ttai.state.NotifyListState
import com.example.ttai.utils.DebugUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotifyListViewModel(private val context: Context) : MviViewModel<NotifyListIntent, NotifyListState>() {
    private val _state = MutableStateFlow(NotifyListState())
    override val state: StateFlow<NotifyListState> = _state.asStateFlow()
    private val notifyRepository = NetworkModule.provideNotifyRepository(context)
    override fun processIntent(intent: NotifyListIntent) {
        viewModelScope.launch {
            when (intent) {
                is NotifyListIntent.Initialize -> {
                    init(isRefreshing = true)
                }
                is NotifyListIntent.RefreshData -> {
                    init(isRefreshing = true)
                }
                is SynthesizeFragmentIntent.LoadMoreData -> {
                    init(_state.value.currentPage + 1, isLoadingMore = true)
                }

                else -> {}
            }
        }
    }
    suspend fun init(
        page: Int = 1,
        isRefreshing: Boolean = false,
        isLoadingMore: Boolean = false
    ) {
        try {
            val notificationsResponse = notifyRepository.notificationsList(page,20)
            _state.value = _state.value.copy(isRefreshing = isRefreshing,isLoadingMore = isLoadingMore)
            _state.value = _state.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    list = notificationsResponse.notifications,
                    currentPage = notificationsResponse.page,
                    hasMoreData = notificationsResponse.page < notificationsResponse.totalPages,
                )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                error = "获取用户的消息列表: ${e.message}"
            )
        }
    }
}

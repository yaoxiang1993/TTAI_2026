package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.BuyExtraSlotData
import com.example.ttai.intent.MainFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.network.repository.MainRepository
import com.example.ttai.network.repository.NotifyRepository
import com.example.ttai.state.MainFragmentState
import com.example.ttai.utils.DebugUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainFragmentViewModel(private val context: Context): MviViewModel<MainFragmentIntent, MainFragmentState>() {
    private val _state = MutableStateFlow(MainFragmentState())
    override val state: StateFlow<MainFragmentState> = _state.asStateFlow()
    private val mainRepository = MainRepository(context)
    private val notifyRepository = NetworkModule.provideNotifyRepository(context)
    private var characterRepository = CharacterRepository(NetworkModule.provideApiService(), context)
    override fun processIntent(intent: MainFragmentIntent) {
        viewModelScope.launch {
            when (intent) {
                is MainFragmentIntent.Initialize -> {
                    _state.value = _state.value.copy(
                        isLoading = true,
                        error = null,
                        currentPage = 1,
                        hasMoreData = true
                    )
                    // 初始化加载第一页
                    loadChatList(page = 1)
                    initNotify()
                }
                is MainFragmentIntent.LoadChatList -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    loadChatList(intent.page, intent.limit)
                }
                is MainFragmentIntent.AddAISize -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    addAISize()
                }
                is MainFragmentIntent.RefreshData -> {
                    _state.value = _state.value.copy(
                        isRefreshing = true,
                        error = null,
                        currentPage = 1,
                        hasMoreData = true
                    )
                    loadChatList(page = 1, isRefreshing = true)
                }
                is MainFragmentIntent.LoadMoreData -> {
                    val current = _state.value
                    if (!current.hasMoreData || current.isLoadingMore) return@launch
                    _state.value = current.copy(isLoadingMore = true, error = null)
                    loadChatList(page = current.currentPage + 1, isLoadingMore = true)
                }
            }
        }
    }

    /**
     * 加载角色列表
     */
    private suspend fun loadChatList(
        page: Int = 1,
        limit: Int = 20,
        isRefreshing: Boolean = false,
        isLoadingMore: Boolean = false
    ) {
        try {
            DebugUtils.logApiCall("获取角色列表", mapOf(
                "page" to page.toString(),
                "limit" to limit.toString()
            ))

            val conversationsData = mainRepository.getConversations( page, limit)
            val currentState = _state.value

            if (page == 1) {
                _state.value = currentState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    conversationsData = conversationsData,
                    currentPage = 1,
                    hasMoreData = conversationsData.conversations.isNotEmpty()
                )
            } else {
                val existing = currentState.conversationsData
                val merged = if (existing != null) {
                    val existingList = existing.conversations
                    val newList = conversationsData.conversations
                    val combined = existingList + newList
                    existing.copy(conversations = combined)
                } else conversationsData

                _state.value = currentState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    conversationsData = merged,
                    currentPage = page,
                    hasMoreData = conversationsData.conversations.isNotEmpty()
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                error = "加载角色列表失败: ${e.message}"
            )
        }
    }

    private suspend fun initNotify(){
        notifyRepository
        try {
            val unreadCountResponse = notifyRepository.notificationsUnreadCount()
            _state.value = _state.value.copy(
                isLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
                hasUnread = unreadCountResponse.hasUnread
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                isRefreshing = false,
                isLoadingMore = false,
            )
        }
    }

    /**
     * 购买额外角色槽位
     */
    private suspend fun addAISize( ) {
        try {
            DebugUtils.logApiCall("购买额外角色槽位", )
             characterRepository.buyExtraSlot()
            _state.value = _state.value.copy(
                isLoading = false,
                addAISizeSuceesss = true
            )
        }catch (e: Exception) {
            android.util.Log.e("MainFragmentViewModel", "购买额外角色槽位失败", e)
            _state.value = _state.value.copy(
                isLoading = false,
                error = "购买额外角色槽位失败: ${e.message}"
            )
        }
    }
}
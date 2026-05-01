package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.FollowersListIntent
import com.example.ttai.intent.FollowingListIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.exception.ApiException
import com.example.ttai.state.FollowingListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FollowingListViewModel(private val context: Context) : MviViewModel<FollowingListIntent, FollowingListState>() {
    private val _state = MutableStateFlow(FollowingListState())
    override val state: StateFlow<FollowingListState> = _state.asStateFlow()
    private val followerRepository = NetworkModule.provideFollowerRepository(context)
    override fun processIntent(intent: FollowingListIntent) {
        viewModelScope.launch {
            when (intent) {
                is FollowingListIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = false)
                    search("")
                }
                is FollowingListIntent.PerformSearch -> {
                    search(intent.query)
                }
                is FollowingListIntent.ClearSearch -> {
                    _state.value = _state.value.copy(
                        searchResults = emptyList(),
                        searchQuery = "",
                        isSearching = false,
                        error = null
                    )
                }
                is FollowingListIntent.Follow -> {
                    setFollowUser(true,intent.target_user_id)
                }
                is FollowingListIntent.UnFollow -> {
                    setFollowUser(false,intent.target_user_id)
                }
                is FollowingListIntent.LoadMoreData -> {
                    Log.d("SearchViewModel", "Loading more data...")
//                    loadComprehensiveCharacters(_state.value.currentPage+1)
                }
            }
        }
    }

    fun search(query:String="") {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isSearching = true,
                searchQuery = query,
                error = null
            )
            try {
                // 执行搜索
                val searchResponse = followerRepository.following(
                    keyword = query,
                    page = 1,
                    limit = 20
                )

                searchResponse.following?.let {
                    it.forEach { it -> it.isFollowing = true }
                    _state.value = _state.value.copy(
                        searchResults = it,
                        isSearching = false,
                        error = null
                    )
                }
            } catch (e: ApiException) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = e.message ?: "搜索失败"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = "网络错误: ${e.message}"
                )
            }
        }
    }
    fun setFollowUser(followState: Boolean = false, target_user_id :String? ="") {
        viewModelScope.launch {
                // 执行搜索
            try {
            if (followState){
                followerRepository.followUser(target_user_id)
            }else{
                followerRepository.unFollowUser(target_user_id)
            }
                _state.update { currentState ->
                    // 查询当前列表
                    val currentResults = currentState.searchResults

                    // 找到目标元素（通过 ID 查询；如果用索引，用 currentResults[index]）
                    val targetIndex = currentResults.indexOfFirst { it.user_id == target_user_id }
                    if (targetIndex == -1) {
                        // 如果未找到，日志或返回原状态
                        return@update currentState
                    }
                    // 创建新列表：复制所有元素，只修改目标
                    val updatedResults = currentResults.mapIndexed { index, item ->
                        if (index == targetIndex) {
                            // 假设 FollowsItem 是 data class，使用 copy() 修改
                            item.copy(isFollowing = followState)
                        } else {
                            item  // 其他元素不变
                        }
                    }
                    // 返回新状态（只更新 searchResults）
                    currentState.copy(searchResults = updatedResults)
                }

            } catch (e: ApiException) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = e.message ?: "关注状态修改失败"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = "网络错误: ${e.message}"
                )
            }
        }
    }
}

package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.ChatItem
import com.example.ttai.bean.Character
import com.example.ttai.intent.SearchIntent
import com.example.ttai.intent.SynthesizeFragmentIntent
import com.example.ttai.state.SearchState
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.exception.ApiException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(private val context: Context) : MviViewModel<SearchIntent, SearchState>() {
    private val _state = MutableStateFlow(SearchState())
    override val state: StateFlow<SearchState> = _state.asStateFlow()
    
    private val characterRepository = NetworkModule.provideCharacterRepository(context)

    override fun processIntent(intent: SearchIntent) {
        viewModelScope.launch {
            when (intent) {
                is SearchIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
                is SearchIntent.PerformSearch -> {
                    _state.value = _state.value.copy(
                        isSearching = true,
                        searchQuery = intent.query,
                        error = null
                    )
                    try {
                        // 执行搜索
                        val searchResponse = characterRepository.searchCharacters(
                            keyword = intent.query
                        )

                        _state.value = _state.value.copy(
                            searchResults = searchResponse.characters,
                            isSearching = false,
                            error = null
                        )
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
                is SearchIntent.ClearSearch -> {
                    _state.value = _state.value.copy(
                        searchResults = emptyList(),
                        searchQuery = "",
                        isSearching = false,
                        error = null
                    )
                }
                is SearchIntent.LoadMoreData -> {
                    android.util.Log.d("SearchViewModel", "Loading more data...")
//                    loadComprehensiveCharacters(_state.value.currentPage+1)
                }
            }
        }
    }
}

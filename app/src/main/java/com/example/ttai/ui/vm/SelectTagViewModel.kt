package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.SelectTagIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.SelectTagState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SelectTagViewModel(private val context: Context) : MviViewModel<SelectTagIntent, SelectTagState>() {
    private val _state = MutableStateFlow(SelectTagState())
    override val state: StateFlow<SelectTagState> = _state.asStateFlow()
    private val selectTagRepository = NetworkModule.provideSelectTagRepository(context)

    override fun processIntent(intent: SelectTagIntent) {
        viewModelScope.launch {
            when (intent) {
                is SelectTagIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = true)
                    try {
                        val personalityTagsResponse = selectTagRepository.getPersonalityTags()
                        _state.value = _state.value.copy(
                            isLoading = false,
                            tags = personalityTagsResponse.tags
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
                is SelectTagIntent.SelectTag -> {
                    val currentSelected = _state.value.selectedTags
                    val newSelected = if (currentSelected.contains(intent.tag)) {
                        currentSelected - intent.tag
                    } else {
                        currentSelected + intent.tag
                    }
                    _state.value = _state.value.copy(selectedTags = newSelected as ArrayList<String>)
                }
                is SelectTagIntent.ConfirmSelection -> {
                    if (_state.value.selectedTags.isEmpty()) {
                        _state.value = _state.value.copy(error = "请至少选择一个标签")
                    } else {
                        // 先更新偏好标签，然后跳转
                        processIntent(SelectTagIntent.UpdatePreferredTags)
                    }
                }
                is SelectTagIntent.UpdatePreferredTags -> {
                    _state.value = _state.value.copy(
                        isUpdatingPreferredTags = true,
                        preferredTagsUpdateError = null
                    )
                    try {
                        selectTagRepository.updatePreferredTags(_state.value.selectedTags.toList())
                        _state.value = _state.value.copy(
                            isUpdatingPreferredTags = false,
                            preferredTagsUpdateSuccess = true,
                            navigateToNext = true
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isUpdatingPreferredTags = false,
                            preferredTagsUpdateError = "更新偏好标签失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}
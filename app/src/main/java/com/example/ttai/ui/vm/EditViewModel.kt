package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.EditIntent
import com.example.ttai.state.EditState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditViewModel : MviViewModel<EditIntent, EditState>() {
    private val _state = MutableStateFlow(EditState())
    override val state: StateFlow<EditState> = _state.asStateFlow()

    override fun processIntent(intent: EditIntent) {
        viewModelScope.launch {
            when (intent) {
                is EditIntent.LoadData -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        kotlinx.coroutines.delay(1000)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            content = "编辑数据已加载"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "加载数据失败: ${e.message}"
                        )
                    }
                }
                is EditIntent.PerformAction -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        kotlinx.coroutines.delay(500)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            content = "已执行操作: ${intent.action}"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "操作失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}
package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.SettingIntent
import com.example.ttai.state.SettingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingViewModel : MviViewModel<SettingIntent, SettingState>() {
    private val _state = MutableStateFlow(SettingState())
    override val state: StateFlow<SettingState> = _state.asStateFlow()

    override fun processIntent(intent: SettingIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingIntent.LoadData -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        kotlinx.coroutines.delay(1000)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            content = "设置数据已加载"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "加载数据失败: ${e.message}"
                        )
                    }
                }
                is SettingIntent.PerformAction -> {
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
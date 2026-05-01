package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.EditIntent
import com.example.ttai.intent.MyChatSettingIntent
import com.example.ttai.intent.SwitchModeIntent
import com.example.ttai.state.EditState
import com.example.ttai.state.MyChatSettingState
import com.example.ttai.state.SwitchModeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SwitchModeViewModel : MviViewModel<SwitchModeIntent, SwitchModeState>() {
    private val _state = MutableStateFlow(SwitchModeState())
    override val state: StateFlow<SwitchModeState> = _state.asStateFlow()

    override fun processIntent(intent: SwitchModeIntent) {
        viewModelScope.launch {
            when (intent) {
                is SwitchModeIntent.LoadData -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        delay(1000)
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
            }
        }
    }
}
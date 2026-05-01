package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.MainIntent
import com.example.ttai.state.MainState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : MviViewModel<MainIntent, MainState>() {
    private val _state = MutableStateFlow(MainState())
    override val state: StateFlow<MainState> = _state.asStateFlow()
    override fun processIntent(intent: MainIntent) {
        viewModelScope.launch {
            when (intent) {
                is MainIntent.EnterClicked -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    // 模拟登录逻辑

                }

            }
        }
    }
}

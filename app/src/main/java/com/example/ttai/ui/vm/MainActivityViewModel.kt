package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.MainActivityIntent
import com.example.ttai.state.MainActivityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel: MviViewModel<MainActivityIntent, MainActivityState>() {
    private val _state = MutableStateFlow(MainActivityState())
    override val state: StateFlow<MainActivityState> = _state.asStateFlow()

    override fun processIntent(intent: MainActivityIntent) {
        viewModelScope.launch {
            when (intent) {
                is MainActivityIntent.SwitchTab -> {
                    _state.value = _state.value.copy(currentTabIndex = intent.index)
                }
            }
        }
    }
}
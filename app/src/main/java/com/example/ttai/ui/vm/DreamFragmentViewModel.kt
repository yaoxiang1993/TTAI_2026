package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.DreamFragmentIntent
import com.example.ttai.intent.ShopActivityIntent
import com.example.ttai.state.DreamFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DreamFragmentViewModel : MviViewModel<DreamFragmentIntent, DreamFragmentState>() {
    private val _state = MutableStateFlow(DreamFragmentState())
    override val state: StateFlow<DreamFragmentState> = _state.asStateFlow()

    override fun processIntent(intent: DreamFragmentIntent) {
        viewModelScope.launch {
            when (intent) {
                is DreamFragmentIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = false)
                }
                is DreamFragmentIntent.SwitchTab -> {
                    _state.value = _state.value.copy(currentTabIndex = intent.index)
                }
            }
        }
    }
}
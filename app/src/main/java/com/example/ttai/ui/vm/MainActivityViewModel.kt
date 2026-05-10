package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.MainActivityIntent
import com.example.ttai.network.repository.MainRepository
import com.example.ttai.state.MainActivityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(private val context: Context): MviViewModel<MainActivityIntent, MainActivityState>() {
    private val _state = MutableStateFlow(MainActivityState())
    override val state: StateFlow<MainActivityState> = _state.asStateFlow()
    private val mainRepository = MainRepository(context)

    override fun processIntent(intent: MainActivityIntent) {
        viewModelScope.launch {
            when (intent) {
                is MainActivityIntent.SwitchTab -> {
                    _state.value = _state.value.copy(currentTabIndex = intent.index)
                }
                is MainActivityIntent.versionCheck -> {
                    _state.value = _state.value.copy(appUpdateInfo = mainRepository.getVersionCheck())
                }
            }
        }
    }
}
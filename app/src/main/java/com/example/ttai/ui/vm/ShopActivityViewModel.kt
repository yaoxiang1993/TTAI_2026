package com.example.ttai.ui.vm

import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.MainActivityIntent
import com.example.ttai.intent.ShopActivityIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.ShopActivityState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 商店Activity的ViewModel
 */
class ShopActivityViewModel : MviViewModel<ShopActivityIntent, ShopActivityState>() {

    private val _state = MutableStateFlow(ShopActivityState())
    override val state: StateFlow<ShopActivityState> = _state.asStateFlow()
    
    override fun processIntent(intent: ShopActivityIntent) {
        when (intent) {
            is ShopActivityIntent.Initialize -> {

            }
            is ShopActivityIntent.SwitchTab -> {
                _state.value = _state.value.copy(currentTabIndex = intent.index)
            }
        }
    }
    

}

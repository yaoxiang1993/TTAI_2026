package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.ShopToyFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.ShopToyFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 商店Fragment的ViewModel
 */
class ShopToyFragmentViewModel(private val context: Context) : MviViewModel<ShopToyFragmentIntent, ShopToyFragmentState>() {
    
    private val shopRepository = NetworkModule.provideShopRepository(context)
    
    private val _state = MutableStateFlow(ShopToyFragmentState())
    override val state: StateFlow<ShopToyFragmentState> = _state.asStateFlow()
    
    override fun processIntent(intent: ShopToyFragmentIntent) {
        when (intent) {
            is ShopToyFragmentIntent.Initialize -> {
                getShopItems()
            }
        }
    }
    
    private fun getShopItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val shopLinkResponse = shopRepository.getShopLinks()
                _state.value = _state.value.copy(
                    jdLink = shopLinkResponse.jdLink,
                    taobaoLink = shopLinkResponse.taobaoLink,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }
} 
package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.ShopOwnedFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.ShopOwnedFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 已拥有商品Fragment的ViewModel
 */
class ShopOwnedFragmentViewModel(private val context: Context) : MviViewModel<ShopOwnedFragmentIntent, ShopOwnedFragmentState>() {
    
    private val shopRepository = NetworkModule.provideShopRepository(context)
    
    private val _state = MutableStateFlow(ShopOwnedFragmentState())
    override val state: StateFlow<ShopOwnedFragmentState> = _state.asStateFlow()
    
    override fun processIntent(intent: ShopOwnedFragmentIntent) {
        when (intent) {
            is ShopOwnedFragmentIntent.Initialize -> {
                getOwnedItems()
            }
        }
    }
    
    private fun getOwnedItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val ownedItemsResponse = shopRepository.getOwnedItems()
                _state.value = _state.value.copy(
                    ownedItems = ownedItemsResponse.items,
                    totalCount = ownedItemsResponse.totalCount,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }
}

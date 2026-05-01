package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.ShopFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.ShopFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 商店Fragment的ViewModel
 */
class ShopFragmentViewModel(private val context: Context) : MviViewModel<ShopFragmentIntent, ShopFragmentState>() {
    
    private val shopRepository = NetworkModule.provideShopRepository(context)
    
    private val _state = MutableStateFlow(ShopFragmentState())
    override val state: StateFlow<ShopFragmentState> = _state.asStateFlow()
    
    override fun processIntent(intent: ShopFragmentIntent) {
        when (intent) {
            is ShopFragmentIntent.Initialize -> {
                getShopItems()
            }
            is ShopFragmentIntent.PurchaseItem -> {
                purchaseItem(intent.itemId, intent.quantity)
            }
        }
    }
    
    private fun getShopItems() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val shopItemsResponse = shopRepository.getShopItems()
                _state.value = _state.value.copy(
                    shopItems = shopItemsResponse.items,
                    totalCount = shopItemsResponse.totalCount,
                    isLoading = false,
                    purchaseSuccess = null // 确保刷新时重置购买成功状态
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 购买商品
     */
    fun purchaseItem(itemId: String, quantity: Int = 1) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                shopRepository.purchaseItem(itemId, quantity)
                // 购买成功，显示成功消息
                _state.value = _state.value.copy(
                    purchaseSuccess = true,
                    isLoading = false
                )
                // 延迟刷新商品列表，避免状态冲突
                kotlinx.coroutines.delay(100)
                getShopItems()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 重置购买成功状态
     */
    fun resetPurchaseSuccess() {
        _state.value = _state.value.copy(purchaseSuccess = null)
    }
} 
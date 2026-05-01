package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.ChargeMoneyIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.ChargeMoneyRepository
import com.example.ttai.state.ChargeMoneyState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChargeMoneyViewModel(private val context: Context) : MviViewModel<ChargeMoneyIntent, ChargeMoneyState>() {
    private val _state = MutableStateFlow(ChargeMoneyState())
    override val state: StateFlow<ChargeMoneyState> = _state.asStateFlow()
    private val repository = NetworkModule.provideChargeMoneyRepository(context)

    override fun processIntent(intent: ChargeMoneyIntent) {
        when (intent) {
            is ChargeMoneyIntent.LoadRechargePackages -> loadRechargePackages()
            is ChargeMoneyIntent.Charge -> charge(intent.amount)
            is ChargeMoneyIntent.ClearPayUrl -> clearPayUrl()
        }
    }

    private fun loadRechargePackages() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingPackages = true, error = null)
            try {
                val packagesData = repository.getRechargePackages()
                _state.value = _state.value.copy(
                    isLoadingPackages = false,
                    rechargePackages = packagesData.packages,
                    promotionText = packagesData.promotionText
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingPackages = false,
                    error = e.message ?: "获取充值套餐失败"
                )
            }
        }
    }

    private fun charge(amount: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, chargeSuccess = false)
            try {
                val response = repository.rechargeCurrency(amount)
                _state.value = _state.value.copy(
                    isLoading = false, 
                    chargeSuccess = false,
                    payUrl = response.payUrl,
                    orderId = response.orderId
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "充值失败"
                )
            }
        }
    }
    
    private fun clearPayUrl() {
        _state.value = _state.value.copy(payUrl = null, orderId = null)
    }
} 
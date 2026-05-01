package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.CollectItem
import com.example.ttai.intent.CollectIntent
import com.example.ttai.intent.ControlFragmentIntent
import com.example.ttai.network.ApiService
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.CollectState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CollectViewModel(  context: Context
) : MviViewModel<CollectIntent, CollectState>() {
    private val _state = MutableStateFlow(CollectState())
    override val state: StateFlow<CollectState> = _state.asStateFlow()

    private val modelsRepository = NetworkModule.provideModesRepository(context)

    override fun processIntent(intent: CollectIntent) {
        viewModelScope.launch {
            when (intent) {
                is CollectIntent.Initialize -> {
                    getDeviceControlModes()
                }

                is CollectIntent.SetFavoriteState -> {
                    setFavoriteState(intent.collectItem)
                }
            }
        }
    }

    private suspend fun setFavoriteState(collectItem : CollectItem) {
        try {
            if(!collectItem.isFavorited){
                modelsRepository.postFavorite(collectItem.id)
            }else{
                modelsRepository.postUnfavorite(collectItem.id)
            }
            getDeviceControlModes()
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "加载模型列表失败: ${e.message}"
            )
        }
    }
    private suspend fun  getDeviceControlModes() {
        try {
            var modelsItemsResponse = modelsRepository.getDeviceControlModes()
            _state.value = _state.value.copy(
                isLoading = false,
                modes = modelsItemsResponse.modes
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "加载模型列表失败: ${e.message}"
            )
        }
    }
}
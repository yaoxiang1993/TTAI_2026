package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.CollectItem
import com.example.ttai.intent.CollectIntent
import com.example.ttai.intent.RequestModelIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.RequestModelState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Arrays

class RequestModelViewModel(  context: Context
) : MviViewModel<RequestModelIntent, RequestModelState>() {
    private val _state = MutableStateFlow(RequestModelState())
    override val state: StateFlow<RequestModelState> = _state.asStateFlow()

    private val modelsRepository = NetworkModule.provideModesRepository(context)

    override fun processIntent(intent: RequestModelIntent) {
        viewModelScope.launch {
            when (intent) {
                is RequestModelIntent.Initialize -> {
                    getDeviceControlModes()
                }

                is RequestModelIntent.SetFavoriteState -> {
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
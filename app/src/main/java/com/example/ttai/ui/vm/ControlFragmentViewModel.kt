package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.CollectItem
import com.example.ttai.bean.FavoriteResponse
import com.example.ttai.intent.ControlFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.ControlFragmentState
import com.example.ttai.ui.fragment.ControlFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ControlFragmentViewModel(
    private val context: Context
) : MviViewModel<ControlFragmentIntent, ControlFragmentState>() {
    private val _state = MutableStateFlow(ControlFragmentState())
    override val state: StateFlow<ControlFragmentState> = _state.asStateFlow()

    private val modelsRepository = NetworkModule.provideModesRepository(context)

    override fun processIntent(intent: ControlFragmentIntent) {
        viewModelScope.launch {
            when (intent) {
                is ControlFragmentIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = false)
                    getDeviceControlModes()

                }
                is ControlFragmentIntent.AddDevice -> {
                    // 跳转逻辑在 ControlFragment 处理，ViewModel 只更新状态
                    _state.value = _state.value.copy(isLoading = false, error = null)
                }
                is ControlFragmentIntent.SetFavoriteState -> {
                    setFavoriteState(intent.collectItem)
                }
                is ControlFragmentIntent.UpdateVibrationIntensity -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        vibrationIntensity = intent.intensity
                    )
                }
            }
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
}
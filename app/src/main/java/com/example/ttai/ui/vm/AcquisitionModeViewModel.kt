package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.AcquisitionModeIntent
import com.example.ttai.state.AcquisitionModeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 采集模式界面的 ViewModel，处理 Intent 并更新状态
 */
class AcquisitionModeViewModel : MviViewModel<AcquisitionModeIntent, AcquisitionModeState>() {
    private val _state = MutableStateFlow(AcquisitionModeState())
    override val state: StateFlow<AcquisitionModeState> = _state.asStateFlow()

    override fun processIntent(intent: AcquisitionModeIntent) {
        viewModelScope.launch {
            when (intent) {
                is AcquisitionModeIntent.LoadData -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    // 模拟数据加载
                    try {
                        kotlinx.coroutines.delay(1000) // 模拟网络延迟
                        _state.value = _state.value.copy(
                            isLoading = false,
                            content = "采集模式数据已加载"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "加载数据失败: ${e.message}"
                        )
                    }
                }
                is AcquisitionModeIntent.PerformAction -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    // 模拟执行操作
                    try {
                        kotlinx.coroutines.delay(500)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            content = "已执行操作: ${intent.action}"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "操作失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}
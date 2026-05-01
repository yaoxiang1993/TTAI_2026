package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.CollectItem
import com.example.ttai.intent.ChatModelListIntent
import com.example.ttai.intent.CollectIntent
import com.example.ttai.intent.RequestModelIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.UserRepository
import com.example.ttai.state.ChatModelListState
import com.example.ttai.state.RequestModelState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Arrays

class ChatModelListViewModel(context: Context
) : MviViewModel<ChatModelListIntent, ChatModelListState>() {
    private val _state = MutableStateFlow(ChatModelListState())
    override val state: StateFlow<ChatModelListState> = _state.asStateFlow()

    private val modelsRepository = NetworkModule.provideModesRepository(context)

    private val userRepository = NetworkModule.provideUserRepository( context)

    override fun processIntent(intent: ChatModelListIntent) {
        viewModelScope.launch {
            when (intent) {
                is ChatModelListIntent.Initialize -> {
                    try {
                       var response = modelsRepository.getChatModels()
                        val updatedPriceList = response.models.map { item ->
                            if (item.id == response.current_user_model || (response.current_user_model?.isEmpty() == true && item.id == response.default_model)) {
                                // 使用 copy() 方法创建一个新的 PriceItem 实例，只修改 isDefaultModel
                                // 注意：如果 isDefaultModel 是 var，你可以直接 item.isDefaultModel = true; return item
                                // 但使用 copy() 更符合不可变数据流的最佳实践。
                                item.copy(isDefaultModel = true)
                            } else {
                                // 其他项保持不变
                                item
                            }
                        }
                        _state.value = _state.value.copy(
                            isLoading = false,
                            modes = updatedPriceList
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "获取聊天模型: ${e.message}"
                        )
                    }
                }

                is ChatModelListIntent.UpdateSelectedModel -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        // 调用更新AI模型偏好API
                        userRepository.updateAiModelPreference(intent.model.id)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            selectedModelSuccess = true,
                          )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "x选择模型失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}
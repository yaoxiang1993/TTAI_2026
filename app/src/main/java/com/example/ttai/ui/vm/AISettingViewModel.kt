package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.ChatModelItem
import com.example.ttai.intent.AISettingIntent
import com.example.ttai.state.AISettingState
import com.example.ttai.network.ApiService
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.network.repository.PermanentMemoryRepository
import com.example.ttai.network.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AISettingViewModel(
    private val apiService: ApiService,
    private val context: Context
) : MviViewModel<AISettingIntent, AISettingState>() {
    private val _state = MutableStateFlow(AISettingState())
    override val state: StateFlow<AISettingState> = _state.asStateFlow()

    private val userRepository = UserRepository(apiService, context)
    private val permanentMemoryRepository = PermanentMemoryRepository(apiService, context)
    private val chatRepository = ChatRepository(context)
    var characterId :String  = ""
    override fun processIntent(intent: AISettingIntent) {
        viewModelScope.launch {
            when (intent) {
                is AISettingIntent.LoadData -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        kotlinx.coroutines.delay(1000)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            content = "AI 设置数据已加载"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "加载数据失败: ${e.message}"
                        )
                    }
                }
                is AISettingIntent.LoadUserProfile -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    val response = userRepository.getUserProfile()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isUserMember = false,
                        profileData = response.profile
                    )
                }
                is AISettingIntent.LoadAiSettings -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        val response = apiService.getChatModels()
                        if (response.code == 200 && response.data != null) {
                            var current_user_model :String = response.data.current_user_model?:""
                            var models : List<ChatModelItem> = response.data.models

                            val currentModelItem: ChatModelItem? = models.find { item ->
                                item.id == current_user_model
                            }

                            _state.value = _state.value.copy(
                                isLoading = false,
                                currentModelItem = currentModelItem
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "获取AI设置失败: ${response.message}"
                            )
                        }
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "获取AI设置失败: ${e.message}"
                        )
                    }
                }
                is AISettingIntent.UpdateSelectedModel -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        // 调用更新AI模型偏好API
                        val response = userRepository.updateAiModelPreference(intent.model.id)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            selectedModelId = response.preferredAiModel,
                            userBalance = response.userBalance,
                            content = "AI模型偏好更新成功！当前余额：仙玉 ${response.userBalance.fairyJade}，仙贝 ${response.userBalance.fairyShells}"
                        )

                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "更新AI模型偏好失败: ${e.message}"
                        )
                    }
                }
                is AISettingIntent.PerformAction -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
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
                } is AISettingIntent.ClearChatHistory -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        // 调用清除聊天历史API
                        chatRepository.clearCharacterChat(characterId)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            content = "聊天历史清除成功!",
                            clearChatSuccess = true
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "清除聊天历史失败: ${e.message}"
                        )
                    }
                }
                is AISettingIntent.TogglePermanentMemory -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        if (intent.isEnabled) {
                            // 分配永久记忆权益
                            val response =
                                permanentMemoryRepository.allocatePermanentMemory(characterId)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isPermanentMemoryEnabled = true,
                                content = "成功为角色开启永久记忆！剩余权益：${response.remainingBenefits}/${response.totalBenefits}"
                            )
                        } else {
                            // 回收永久记忆权益
                            val response =
                                permanentMemoryRepository.deallocatePermanentMemory(characterId)
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isPermanentMemoryEnabled = false,
                                content = "成功回收角色的永久记忆权益！剩余权益：${response.remainingBenefits}/${response.totalBenefits}"
                            )
                        }
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "更新永久记忆设置失败: ${e.message}"
                        )
                    }
                }
                is AISettingIntent.ToggleDoubleReply -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        // 调用双倍回复升级API
                        val response = apiService.upgradeDoubleReply()
                        if (response.code == 200 && response.data != null) {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                isDoubleReplyEnabled = true,
                                content = "双倍回复权益购买成功！您现在可以享受双倍回复功能\n剩余仙玉：${response.data.remainingFairyJade}"
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "双倍回复升级失败: ${response.message}"
                            )
                        }
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "双倍回复升级失败: ${e.message}"
                        )
                    }
                }
                is AISettingIntent.LoadPermanentMemoryStatus -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        val response =
                            permanentMemoryRepository.getPermanentMemoryStatus(characterId)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isPermanentMemoryEnabled = response.permanentMemoryEnabled,
                            content = "永久记忆状态：${if (response.permanentMemoryEnabled) "已启用" else "未启用"}，可用权益：${response.availableBenefits}/${response.totalBenefits}"
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "获取永久记忆状态失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }
}
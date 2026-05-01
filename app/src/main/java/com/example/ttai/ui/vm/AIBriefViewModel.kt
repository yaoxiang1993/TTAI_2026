package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.AddChatSlotRequest
import com.example.ttai.bean.RemoveChatSlotRequest
import com.example.ttai.bean.Conversation
import com.example.ttai.event.ChatSlotChangedEvent
import com.example.ttai.intent.AIBriefIntent
import com.example.ttai.network.ApiService
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.state.AIBriefState
import com.example.ttai.utils.DebugUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class AIBriefViewModel(
    private val apiService: ApiService,
    private val context: android.content.Context
) : MviViewModel<AIBriefIntent, AIBriefState>() {

    private val _state = MutableStateFlow(AIBriefState())
    override val state: StateFlow<AIBriefState> = _state.asStateFlow()
    val chatRepository =  ChatRepository(context)
    private var characterRepository: CharacterRepository =  CharacterRepository(apiService, context)
    var characterId :String ?=""
    var conversationId :String ? =""
    override fun processIntent(intent: AIBriefIntent) {
        when (intent) {
            is AIBriefIntent.Initialize -> {
                characterId = intent.character.id
                conversationId = intent.character.conversationId
                _state.value = _state.value.copy(
                    characterId = characterId,
                    conversationId = conversationId,
                    inChatSlots = intent.character.inChatSlots == true,
                    briefIntro = intent.character.briefIntro,
                    openingLine = intent.character.openingLine,
                    characterName = intent.character.name,
                    characterAvatar = intent.character.avatarUrl

                )
            }
            is AIBriefIntent.NavigateToChat -> {
                navigateToChat()
            }
            is AIBriefIntent.ToggleChatSlot -> {
                intent.characterId?.let { toggleChatSlot(it) }
            }
            is AIBriefIntent.ReplaceAI -> {
                handleReplaceAI(intent.conversation)
            }
            is AIBriefIntent.HideReplaceDialog -> {
                _state.value = _state.value.copy(showReplaceDialog = false)
            }
            is AIBriefIntent.ToCreatAI -> {
                _state.value = _state.value.copy(toCreatAI = true)
            }
        }
    }
    private fun navigateToChat() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(navigateToChat = true)
                DebugUtils.logInfo("AIBriefViewModel", "准备跳转到聊天页面")
            } catch (e: Exception) {
                DebugUtils.logError("AIBriefViewModel", "跳转失败", e)
                _state.value = _state.value.copy(
                    error = "跳转失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 重置导航状态
     */
    fun resetNavigationState() {
        _state.value = _state.value.copy(navigateToChat = false)
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * 切换聊天槽位状态
     */
    private fun toggleChatSlot(characterId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isAddingFriend = true, error = null)
                
                val currentState = _state.value
                if (currentState.inChatSlots) {
                    // 当前在槽位中，执行移除操作
                    DebugUtils.logInfo("AIBriefViewModel", "开始移除聊天槽位: $characterId")

                    chatRepository.removeChatSlot(characterId)
                    DebugUtils.logInfo("AIBriefViewModel", "移除聊天槽位成功: ")
                    _state.value = _state.value.copy(
                        isAddingFriend = false,
                        inChatSlots = false,
                        addFriendSuccess = true
                    )
                    // 发送EventBus事件通知MainFragment刷新
                    try {
                        EventBus.getDefault().post(
                            ChatSlotChangedEvent(
                                action = "remove",
                                success = true,
                                message = "移除聊天槽位成功"
                            )
                        )
                        DebugUtils.logInfo("AIBriefViewModel", "EventBus事件发送成功")
                    } catch (e: Exception) {
                        DebugUtils.logError("AIBriefViewModel", "EventBus发送失败: ${e.message}", e)
                    }

                } else {
                    // 当前不在槽位中，执行添加操作
                    DebugUtils.logInfo("AIBriefViewModel", "开始添加聊天槽位: $characterId")
                    
                    // 1. 先调用会话列表接口，查看会话数量
                        val conversationsResponse = chatRepository.getConversations()
                        // 2. 如果会话数量小于5个，则调用添加聊天槽位接口
                        if (conversationsResponse.availableSlots > 0) {
                            val request = AddChatSlotRequest(characterId = characterId)
                            val response = apiService.addChatSlot(request)
                            
                            if (response.code == 200) {
                                DebugUtils.logInfo("AIBriefViewModel", "添加聊天槽位成功: ${response.message}")
                                _state.value = _state.value.copy(
                                    isAddingFriend = false,
                                    inChatSlots = true,
                                    addFriendSuccess = true
                                )
                                
                                // 发送EventBus事件通知MainFragment刷新
                                try {
                                   EventBus.getDefault().post(
                                        ChatSlotChangedEvent(
                                            action = "add",
                                            success = true,
                                            message = "添加聊天槽位成功"
                                        )
                                    )
                                    DebugUtils.logInfo("AIBriefViewModel", "EventBus事件发送成功")
                                } catch (e: Exception) {
                                    DebugUtils.logError("AIBriefViewModel", "EventBus发送失败: ${e.message}", e)
                                }
                                
                                // 添加成功后，自动跳转到聊天页面
                                navigateToChat()
                            } else {
                                val errorMsg = "添加聊天槽位失败: ${response.message}"
                                DebugUtils.logError("AIBriefViewModel", errorMsg)
                                _state.value = _state.value.copy(
                                    isAddingFriend = false,
                                    error = errorMsg
                                )
                            }
                        } else {
                            // 会话数量已达上限，显示替换对话框
                            _state.value = _state.value.copy(
                                isAddingFriend = false,
                                conversations = conversationsResponse.conversations,
                                showReplaceDialog = true
                            )
                        }

                }
            } catch (e: Exception) {
                val errorMsg = "切换聊天槽位异常: ${e.message}"
                DebugUtils.logError("AIBriefViewModel", errorMsg, e)
                _state.value = _state.value.copy(
                    isAddingFriend = false,
                    error = errorMsg
                )
            }
        }
    }

    /**
     * 处理替换AI的逻辑 - 使用新的聊天槽位替换API
     */
    private fun handleReplaceAI(conversation: Conversation) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isAddingFriend = true, error = null)

                // 使用新的聊天槽位替换API
                 chatRepository.replaceChatSlot(
                    oldCharacterId = conversation.characterId,
                    newCharacterId = characterId!!
                )
                val apiResponse =   characterRepository.getCharacterDetail(characterId!!)

                // 替换成功，更新状态
                _state.value = _state.value.copy(
                    isAddingFriend = false,
                    showReplaceDialog = false,
                    characterId = apiResponse.character.id,
                    conversationId = apiResponse.character.conversationId,
                    error = null
                )
                
                // 发送EventBus事件通知MainFragment刷新
                try {
                     EventBus.getDefault().post(
                         ChatSlotChangedEvent(
                            action = "replace",
                            success = true,
                            message = "替换聊天槽位成功"
                        )
                    )
                    DebugUtils.logInfo("AIBriefViewModel", "EventBus事件发送成功")
                } catch (e: Exception) {
                    DebugUtils.logError("AIBriefViewModel", "EventBus发送失败: ${e.message}", e)
                }
                
                // 替换成功后，自动跳转到聊天页面
                navigateToChat()
                
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isAddingFriend = false,
                    error = "替换AI失败: ${e.message}"
                )
            }
        }
    }
} 
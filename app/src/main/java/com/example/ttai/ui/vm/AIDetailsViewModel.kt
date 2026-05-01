package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.intent.AIDetailsIntent
import com.example.ttai.network.ApiService
import com.example.ttai.state.AIDetailsState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.example.ttai.bean.Conversation
import com.example.ttai.event.ChatSlotChangedEvent
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.network.repository.MainRepository
import org.greenrobot.eventbus.EventBus

class AIDetailsViewModel(private val apiService: ApiService) :
    MviViewModel<AIDetailsIntent, AIDetailsState>() {
    private val _state = MutableStateFlow(AIDetailsState())
    override val state: StateFlow<AIDetailsState> = _state.asStateFlow()
    public var characterId: String = ""

    // 延迟初始化CharacterRepository，因为需要context
    private var characterRepository: CharacterRepository? = null
    private var mainRepository : MainRepository? = null
    private var chatRepository : ChatRepository? = null
    private var context: Context? = null

    /**
     * 初始化CharacterRepository
     */
    fun initRepository(context: Context) {
        this.context = context
        if (characterRepository == null) {
            characterRepository = CharacterRepository(apiService, context)
        }
        if (mainRepository == null) {
            mainRepository = MainRepository(context)
        }
        if (chatRepository == null) {
            chatRepository = ChatRepository(context)
        }

    }

    override fun processIntent(intent: AIDetailsIntent) {
        viewModelScope.launch {
            when (intent) {
                is AIDetailsIntent.LoadData -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    val apiResponse = characterRepository?.getCharacterDetail(characterId)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        character = apiResponse?.character,
                        isInChatSlot = apiResponse?.character?.inChatSlots == true
                    )
                }

                is AIDetailsIntent.ToggleChatSlot -> {
                    toggleChatSlot(intent.characterId)
                }

                is AIDetailsIntent.ReplaceAI -> {
                    handleReplaceAI(intent.conversation)
                }
                is AIDetailsIntent.HideReplaceDialog -> {
                    _state.value = _state.value.copy(showReplaceDialog = false)
                }

                is AIDetailsIntent.loadCreator -> TODO()
            }
        }
    }

    /**
     * 切换聊天槽位状态
     */
    private suspend fun toggleChatSlot(characterId: String) {
        _state.value = _state.value.copy(isTogglingSlot = true, error = null)
        try {
            // 检查repository是否已初始化
            if (characterRepository == null) {
                val errorMsg = "Repository未初始化"
                context?.let { ctx ->
                    ToastUtils.showError(ctx, errorMsg)
                }
                _state.value = _state.value.copy(
                    isTogglingSlot = false,
                    error = errorMsg
                )
                return
            }
            // 根据当前状态决定是添加还是移除
            val currentState = _state.value
            if (currentState.isInChatSlot) {
                // 当前在槽位中，执行移除操作
                characterRepository!!.removeChatSlot(characterId)
                _state.value = _state.value.copy(
                    isTogglingSlot = false,
                    isInChatSlot = false,
                    slotToggleSuccess = true
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
                    android.util.Log.d("AIDetailsViewModel", "EventBus事件发送成功")
                } catch (e: Exception) {
                    android.util.Log.e("AIDetailsViewModel", "EventBus发送失败: ${e.message}", e)
                }
            } else {
                // 当前不在槽位中，执行添加操作
                val conversationsData = mainRepository?.getConversations()
                if (conversationsData?.availableSlots!! >0){
                    characterRepository!!.addChatSlot(characterId)
                    _state.value = _state.value.copy(
                        isTogglingSlot = false,
                        isInChatSlot = true,
                        slotToggleSuccess = true
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
                        android.util.Log.d("AIDetailsViewModel", "EventBus事件发送成功")
                    } catch (e: Exception) {
                        android.util.Log.e("AIDetailsViewModel", "EventBus发送失败: ${e.message}", e)
                    }
                }else{
                    // 会话数量已达上限，显示替换对话框
                    _state.value = _state.value.copy(
                        showReplaceDialog = true,
                        conversations = conversationsData.conversations
                    )
                }
            }
        } catch (e: Exception) {
            // 其他异常
            val errorMsg = "操作失败: ${e.message}"
            context?.let { ctx ->
                ToastUtils.showError(ctx, errorMsg)
            }
            _state.value = _state.value.copy(
                isTogglingSlot = false,
                error = errorMsg
            )
        }
    }

    /**
     * 重置槽位切换成功状态
     */
    fun resetSlotToggleSuccess() {
        _state.value = _state.value.copy(slotToggleSuccess = false)
    }

    /**
     * 处理替换AI的逻辑 - 使用新的聊天槽位替换API
     */
    private suspend fun handleReplaceAI(conversation: Conversation) {
        try {
            // 使用新的聊天槽位替换API
            chatRepository?.replaceChatSlot(
                oldCharacterId = conversation.characterId,
                newCharacterId = characterId
            )
            _state.value = _state.value.copy(
                isTogglingSlot = false,
                isInChatSlot = true,
                slotToggleSuccess = true,
                showReplaceDialog = false,
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
                android.util.Log.d("FeaturedFragmentViewModel", "EventBus事件发送成功")
            } catch (e: Exception) {
                android.util.Log.e("FeaturedFragmentViewModel", "EventBus发送失败: ${e.message}", e)
            }

        } catch (e: Exception) {
            val errorMsg = "操作失败: ${e.message}"
            context?.let { ctx ->
                ToastUtils.showError(ctx, errorMsg)
            }
            _state.value = _state.value.copy(
                isTogglingSlot = false,
                error = errorMsg
            )
        }
    }
}
package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.bean.Conversation
import com.example.ttai.base.MviViewModel
import com.example.ttai.event.ChatSlotChangedEvent
import com.example.ttai.intent.FeaturedFragmentIntent
import com.example.ttai.state.FeaturedFragmentState
import com.example.ttai.network.ApiService
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.network.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class FeaturedFragmentViewModel(
    private val apiService: ApiService,
    private val context: Context
) : MviViewModel<FeaturedFragmentIntent, FeaturedFragmentState>() {
    private val characterRepository = CharacterRepository(apiService, context)
    private val chatRepository = ChatRepository(context)
    private val mainRepository = MainRepository(context)

    private val _state = MutableStateFlow(FeaturedFragmentState())
    override val state: StateFlow<FeaturedFragmentState> = _state.asStateFlow()

    private var latestIsUnlimited: Boolean = false // 或者使用 LiveData 存储

    fun updateIsUnlimitedParam(isLimited: Boolean) {
        latestIsUnlimited = isLimited
    }

    override fun processIntent(intent: FeaturedFragmentIntent) {
        viewModelScope.launch {
            when (intent) {
                is FeaturedFragmentIntent.Initialize -> {
                    loadFeaturedContent()
                }
                is FeaturedFragmentIntent.LoadNextData -> {
                    loadNextData()
                }
                is FeaturedFragmentIntent.LoadPreviousData -> {
                    loadPreviousData()
                }
                is FeaturedFragmentIntent.AddAI -> {
                    toggleChatSlot(_state.value.currentCharacterId.toString())
                }
                is FeaturedFragmentIntent.ReplaceAI -> {
                    handleReplaceAI(intent.conversation)
                }
                is FeaturedFragmentIntent.HideReplaceDialog -> {
                    _state.value = _state.value.copy(showReplaceDialog = false)
                }
                is FeaturedFragmentIntent.ToggleChatSlot -> {
                    toggleChatSlot(intent.characterId)
                }
            }
        }
    }
    
    private suspend fun loadFeaturedContent() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            // 使用新的精选角色API
            val featuredResponse = characterRepository.getFeaturedCharacters(page = 1, limit = 20,latestIsUnlimited)
            
            if (featuredResponse.characters.isNotEmpty()) {
                val firstCharacter = featuredResponse.characters[0]

                _state.value = _state.value.copy(
                    isLoading = false,
                    openingLine = firstCharacter.openingLine,
                    briefIntro = firstCharacter.briefIntro,
                    userName = firstCharacter.name,
                    currentIndex = 0,
                    totalCount = featuredResponse.characters.size,
                    currentCharacterId = firstCharacter.id,
                    featuredCharacters = featuredResponse.characters,
                    inChatSlots = firstCharacter.inChatSlots == true,
                    conversationId = firstCharacter.conversationId,
                    currentCharacter = firstCharacter,
                    isInDream = false, // 初始化时重置入梦状态
                    // 保存分页信息
                    currentPage = featuredResponse.page,
                    totalPages = featuredResponse.totalPages,
                    hasMoreData = featuredResponse.page < featuredResponse.totalPages
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = null
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "加载精选内容失败: ${e.message}"
            )
        }
    }
    
    private suspend fun loadNextData() {
        val currentState = _state.value
        val nextIndex = currentState.currentIndex + 1
        
        // 检查是否需要加载下一页数据
        val remainingData = currentState.totalCount - nextIndex
        if (remainingData <= 2 && currentState.hasMoreData) {
            // 只剩下2条或更少数据，且还有更多页，加载下一页
            loadNextPage()
        } else if (nextIndex < currentState.totalCount) {
            // 正常加载下一条数据
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // 使用已加载的精选角色数据
                if (currentState.featuredCharacters.isNotEmpty() && nextIndex < currentState.featuredCharacters.size) {
                    val nextCharacter = currentState.featuredCharacters[nextIndex]
                    _state.value = _state.value.copy(
                        isLoading = false,
                        openingLine = nextCharacter.openingLine,
                        briefIntro = nextCharacter.briefIntro,
                        userName = nextCharacter.name,
                        currentIndex = nextIndex,
                        currentCharacterId = nextCharacter.id,
                        inChatSlots = nextCharacter.inChatSlots == true,
                        conversationId = nextCharacter.conversationId,
                        currentCharacter = nextCharacter,
                        isInDream = false // 重置入梦状态，避免滑动时触发跳转
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "已经是最后一条数据"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载下一条数据失败: ${e.message}"
                )
            }
        } else {
            _state.value = _state.value.copy(error = "已经是最后一条数据")
        }
    }
    
    /**
     * 加载下一页数据
     */
    private suspend fun loadNextPage() {
        val currentState = _state.value
        val nextPage = currentState.currentPage + 1
        
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            // 请求下一页数据
            val featuredResponse = characterRepository.getFeaturedCharacters(page = nextPage, limit = 5,latestIsUnlimited)
            
            if (featuredResponse.characters.isNotEmpty()) {
                // 将新数据添加到现有列表中
                val updatedCharacters = currentState.featuredCharacters + featuredResponse.characters
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    featuredCharacters = updatedCharacters,
                    totalCount = updatedCharacters.size,
                    currentPage = featuredResponse.page,
                    totalPages = featuredResponse.totalPages,
                    hasMoreData = featuredResponse.page < featuredResponse.totalPages,
                    isInDream = false // 重置入梦状态，避免滑动时触发跳转
                )
                
                // 加载完成后，继续加载下一条数据
                loadNextData()
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    hasMoreData = false,
                    error = "没有更多数据了"
                )
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "加载下一页数据失败: ${e.message}"
            )
        }
    }
    
    private suspend fun loadPreviousData() {
        val currentState = _state.value
        if (currentState.currentIndex > 0) {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val previousIndex = currentState.currentIndex - 1
                
                // 使用已加载的精选角色数据
                if (currentState.featuredCharacters.isNotEmpty() && previousIndex >= 0) {
                    val previousCharacter = currentState.featuredCharacters[previousIndex]

                    _state.value = _state.value.copy(
                        isLoading = false,
                        openingLine = previousCharacter.openingLine,
                        briefIntro = previousCharacter.briefIntro,
                        userName = previousCharacter.name,
                        currentIndex = previousIndex,
                        currentCharacterId = previousCharacter.id,
                        inChatSlots = previousCharacter.inChatSlots==true,
                        conversationId =previousCharacter.conversationId,
                        currentCharacter = previousCharacter,
                        isInDream = false // 重置入梦状态，避免滑动时触发跳转
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "已经是第一条数据"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载上一条数据失败: ${e.message}"
                )
            }
        } else {
            _state.value = _state.value.copy(error = "已经是第一条数据")
        }
    }

    /**
     * 切换聊天槽位状态
     */
    private suspend fun toggleChatSlot(characterId: String) {
        try {
            // 只在状态真正需要改变时才更新
            if (!_state.value.isAddingAI) {
                _state.value = _state.value.copy(isAddingAI = true, error = null)
            }
            
            val currentState = _state.value
            if (currentState.inChatSlots) {
                // 当前在槽位中，执行移除操作
                characterRepository.removeChatSlot(characterId)

                _state.value = _state.value.copy(
                    isAddingAI = false,
                    inChatSlots = false,
                    addAIButtonText = "入梦",
                    isInDream = false
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
                    android.util.Log.d("FeaturedFragmentViewModel", "EventBus事件发送成功")
                } catch (e: Exception) {
                    android.util.Log.e(
                        "FeaturedFragmentViewModel",
                        "EventBus发送失败: ${e.message}",
                        e
                    )
                }
            } else {
                // 当前不在槽位中，执行添加操作
                // 1. 先调用会话列表接口，查看会话数量
                val conversationsData = mainRepository.getConversations()
                if (conversationsData.availableSlots > 0) {
                    characterRepository.addChatSlot(characterId)
                    _state.value = _state.value.copy(
                        isAddingAI = false,
                        inChatSlots = true,
                        addAIButtonText = "取消入梦",
                        isInDream = true
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
                        android.util.Log.d("FeaturedFragmentViewModel", "EventBus事件发送成功")
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "FeaturedFragmentViewModel",
                            "EventBus发送失败: ${e.message}",
                            e
                        )
                    }
                } else {
                    // 会话数量已达上限，显示替换对话框
                    _state.value = _state.value.copy(
                        isAddingAI = false,
                        conversations = conversationsData.conversations,
                        showReplaceDialog = true
                    )
                }
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isAddingAI = false,
                error = "切换聊天槽位异常: ${e.message}"
            )
        }
    }
    
    /**
     * 处理替换AI的逻辑 - 使用新的聊天槽位替换API
     */
    private suspend fun handleReplaceAI(conversation: Conversation) {
        try {
            _state.value = _state.value.copy(isAddingAI = true, error = null)
            
            val currentCharacterId = _state.value.currentCharacterId
            if (currentCharacterId?.isEmpty() == true) {
                _state.value = _state.value.copy(
                    isAddingAI = false,
                    error = "无法获取当前角色ID"
                )
                return
            }
            
            // 使用新的聊天槽位替换API
            chatRepository.replaceChatSlot(
                oldCharacterId = conversation.characterId,
                newCharacterId = currentCharacterId!!
            )
            
            // 替换成功，更新状态
            _state.value = _state.value.copy(
                isAddingAI = false,
                addAIButtonText = "已入梦",
                currentCharacterId = currentCharacterId,
                conversationId = conversation.id,
                isInDream = true,
                showReplaceDialog = false,
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
                android.util.Log.d("FeaturedFragmentViewModel", "EventBus事件发送成功")
            } catch (e: Exception) {
                android.util.Log.e("FeaturedFragmentViewModel", "EventBus发送失败: ${e.message}", e)
            }
            
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isAddingAI = false,
                error = "替换AI失败: ${e.message}"
            )
        }
    }
} 
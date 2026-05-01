package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.MyBluetoothManager
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.Character
import com.example.ttai.bean.Message
import com.example.ttai.bean.SimpleMessage
import com.example.ttai.intent.ChatIntent
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.state.ChatState
import com.example.ttai.utils.JsonUtils
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class ChatViewModel(
    private val apiService: ApiService,
    private val context: Context
) : MviViewModel<ChatIntent, ChatState>() {
    private val chatRepository = ChatRepository(context)
    private var characterRepository: CharacterRepository = CharacterRepository(apiService,context)
    val reloadMessage = Message(
        id = "",
        content = "",
        sender = "reload_message",
        isReloadMessage = true,
        timestamp = 0
    )
    private val _state = MutableStateFlow(ChatState())
    override val state: StateFlow<ChatState> = _state.asStateFlow()
    var characterId :String?= ""
    var conversationId :String  = ""
    var character : Character? = null
    
    // 添加消息计数器，确保时间戳唯一性
    private var messageCounter = 0L
    private var currentPage = 1
    private val pageSize = 50
    private var totalMessages = 50
    
    /**
     * 生成唯一的时间戳，避免快速发送消息时的冲突
     */
    private fun generateUniqueTimestamp(): Long {
        return System.currentTimeMillis() * 1000 + (++messageCounter % 1000)
    }

    override fun processIntent(intent: ChatIntent) {
        viewModelScope.launch {
            when (intent) {
                is ChatIntent.LoadMessages -> {
                    currentPage = 1
                    loadMessages(intent.conversationId, page = currentPage)
                }
                is ChatIntent.SendMessage -> {
                    sendMessage(intent.content)
                }
                is ChatIntent.ClearMessage -> {
                    characterId?.let { clearMessages(it) }
                }
                is ChatIntent.StartTyping -> {
                    _state.value = _state.value.copy(isTyping = true)
                }
                is ChatIntent.StopTyping -> {
                    _state.value = _state.value.copy(isTyping = false)
                }
                is ChatIntent.LoadCharacterDetail -> {
                    loadCharacterDetail(intent.characterId)
                }
                is ChatIntent.LoadDefaultCharacterMessages -> {
                    currentPage = 1
                    loadDefaultCharacterMessages(intent.page, intent.limit)
                }
                is ChatIntent.LoadMoreMessages -> {
                    loadMore()
                }
                is ChatIntent.RollbackMessages -> {
                    rollbackMessages(intent.message)
                }
                is ChatIntent.DeleteMessages -> {
                    deleteMessages(intent.message)
                }
                is ChatIntent.UpdateMessage -> {
                    updateMessage(intent.content,intent.messageId)
                }

                is ChatIntent.RegenerateMessage -> {
                    regenerateMessage()
                }
            }
        }
    }
    
    /**
     * 加载消息列表
     */
    private suspend fun loadMessages(conversationId :String, page: Int = 1) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            val messagesData = chatRepository.fetchMessages(conversationId, page = page, limit = pageSize)
            _state.value = _state.value.copy(
                isLoading = false,
                messages = loadMessages(messagesData.messages),
                isClearMessages = false,
                currentUser = "You",
                total_pages = messagesData.totalPages
            )
        } catch (e: Exception) {
            // 其他异常
            ToastUtils.showError(context, "加载消息失败: ${e.message}")
            _state.value = _state.value.copy(
                isLoading = false,
                isClearMessages = false,
                error = "加载消息失败: ${e.message}"
            )
        }
    }

    private suspend fun loadMore() {
        if (conversationId.isEmpty()) return
        val nextPage = currentPage + 1
        if (nextPage > _state.value.total_pages){
            ToastUtils.showError(context, "已显示全部聊天内容")
            return
        }
        try {
            val messagesData = chatRepository.fetchMessages(conversationId, page = nextPage, limit = pageSize)
            // 新页通常是更早的历史，放在已有列表前面
            val merged = messagesData.messages + _state.value.messages
            currentPage = nextPage
            _state.value = _state.value.copy(messages = loadMessages(merged),total_pages = messagesData.totalPages)
        } catch (e: Exception) {
            ToastUtils.showError(context, "加载更多失败: ${e.message}")
            _state.value = _state.value.copy(error = "加载更多失败: ${e.message}")
        }
    }

    private suspend fun rollbackMessages(message: Message?) {
        try {
            val response = chatRepository.postRollbackToMessage(message)
            if (_state.value.messages.contains(message)){
                val currentState = _state.value
                val messageIndex = currentState.messages.indexOf(message)

                if (messageIndex != -1) {
                    // 使用 deleted_count  +1(重新生成图标)
                    val newMessages = currentState.messages.subList(0, currentState.messages.size - response.deleted_count-1)
                    _state.value = currentState.copy(
                        messages = loadMessages(newMessages)
                    )
                }
            }
        } catch (e: Exception) {
            ToastUtils.showError(context, e.message?:"信息回溯失败")
            _state.value = _state.value.copy(error = null)
        }
    }
    private suspend fun deleteMessages(message: Message?) {
        try {
            val response = chatRepository.deleteMessage(message)
            if (_state.value.messages.contains(message)){
                val currentState = _state.value
                val messageIndex = currentState.messages.indexOf(message)

                if (messageIndex != -1) {
                    // 使用 deleted_count  +1(重新生成图标)
                    val newMessages = currentState.messages.filterIndexed {index, _ -> index != messageIndex }
                    _state.value = currentState.copy(
                        messages = loadMessages(newMessages)
                    )
                }
            }
        } catch (e: Exception) {
            ToastUtils.showError(context, e.message?:"信息回溯失败")
            _state.value = _state.value.copy(error = null)
        }
    }
    private suspend fun updateMessage(content: String,messageId: String?) {
        try {
            // 1. 使用 map 函数创建一个新的 messages 列表
            val updatedMessages = _state.value.messages.map { message ->
                if (message.id == messageId) {
                    // 找到匹配的元素，使用 copy() 创建一个新对象并修改 content 属性
                    message.copy(content = content)
                } else {
                    // 不匹配的元素保持不变
                    message
                }
            }
            _state.value = _state.value.copy( messages = updatedMessages )
        } catch (e: Exception) {
            ToastUtils.showError(context, e.message?:"信息回溯失败")
            _state.value = _state.value.copy(error = null)
        }
    }
    private suspend fun regenerateMessage() {
        try {
            // 假设这是你想要添加的新消息，它通常包含你更新后的 content 和 timestamp
            // 1. 获取当前列表
            _state.value = _state.value.copy(isLoading = true,loadingMessages ="重新生成中...", error = null)
            val oldMessages: List<Message> = _state.value.messages
            val targetMessage: Message? = oldMessages.findLast {
                it.sender == "character" && it.message_type == "text"
            }
            Log.e("","YXTEST targetMessage ${JsonUtils.toJson(targetMessage)} ")
            if (targetMessage != null) {
                var result = chatRepository.postRegenerateMessage(targetMessage)
                var newMessage = Message(
                    content = result.new_content,
                    sender = "character",
                    timestamp = result.timestamp,
                    _id = result.message_id,
                    id = result.message_id,
                    message_type = result.message_type?:"text"
                )
                Log.e("","YXTEST newMessage ${JsonUtils.toJson(newMessage)} ")
                val indexToRemove = oldMessages.indexOfLast { it.sender == "character"  }

                val messagesAfterRemoval: List<Message>

                if (indexToRemove != -1) {
                    // 找到了要移除的消息
                    // 1. 使用 toMutableList() 创建一个可变副本
                    val mutableList = oldMessages.toMutableList()

                    // 2. 移除找到的元素
                    mutableList.removeAt(indexToRemove)

                    // 3. 将可变列表转换回不可变列表
                    messagesAfterRemoval = mutableList.toList()
                } else {
                    // 未找到任何 sender 为 "character" 的消息，列表保持不变
                    messagesAfterRemoval = oldMessages
                }
                // 4. 将新的消息元素添加到上一步生成的列表的末尾
                // 使用 'plus' 操作符或 list + element，它会返回一个新的 List
                val finalMessages = messagesAfterRemoval + newMessage

                // --- 更新状态 ---
                // 5. 将新列表 finalMessages 赋值给你的状态
                _state.value = _state.value.copy(isLoading = false,loadingMessages ="",messages = loadMessages(finalMessages))

            } else {
                _state.value = _state.value.copy(isLoading = false,loadingMessages ="", error = null)
                // 未找到任何 sender 为 "character" 的消息，不执行替换
                // 可以添加日志或错误处理
            }

        } catch (e: Exception) {
            _state.value = _state.value.copy(isLoading = false, loadingMessages ="",error = null)
            if (e is ApiException){
                withContext(Dispatchers.Main) {
                    ToastUtils.showError(context, "${e.message}")
                }
            }else{
                ToastUtils.showError(context, "重新生成消息失败 ")
            }
        }
    }

    /**
     * 发送消息
     */
    private fun sendMessage(content: String) {
        if (content.isBlank()) {
            ToastUtils.showError(context, "消息内容不能为空")
            _state.value = _state.value.copy(error = "消息内容不能为空")
            return
        }
        if (characterId.isNullOrEmpty() ){
            ToastUtils.showError(context, "消息内容不能为空")
            _state.value = _state.value.copy(error = "消息内容不能为空")
            return
        }
        if (state.value.isTyping){
            ToastUtils.showError(context, "角色正在回复中")
            return
        }
        
        // 生成唯一的时间戳，避免快速发送时的冲突
        val userTimestamp = generateUniqueTimestamp()
        val typingTimestamp = generateUniqueTimestamp()
        Log.w("ChatActivity", "    typingTimestamp: ${typingTimestamp}")
        
        // 立即添加用户发送的消息到界面
        val tempUserMessage = Message(
            content = content,
            sender = "user",
            timestamp = userTimestamp
        )
        
        // 添加正在输入的AI消息（省略号）
        val typingMessage = Message(
            content = "...",
            sender = "character",
            timestamp = typingTimestamp,
            isTyping = true
        )
        
        val currentMessages = _state.value.messages + tempUserMessage + typingMessage
        _state.value = _state.value.copy(
            messages = loadMessages(currentMessages),
            isTyping = true
        )
        // *** 关键修改：在这里启动一个新的协程来执行网络请求 ***
        viewModelScope.launch {
            try {
            val sendResult = chatRepository.sendMessage(characterId!!, content)
                // 修改震动强度
            MyBluetoothManager.writeCharacteristicUp(sendResult.vibration_intensity,sendResult.sucking_intensity)
            // 1. 获取当前消息列表的一个可变副本
            val messagesCopy = _state.value.messages.toMutableList()
            var character_chat_array: List<SimpleMessage> = sendResult.character_chat_array
            // 2. 找到并移除 "正在输入..." 的消息 (typingMessage)
            // 我们需要知道 typingMessage 在列表中的位置，以便精确替换
            val typingMessageIndex = messagesCopy.indexOfFirst { it.id == typingMessage.id }
            // 3. 准备要插入的新消息列表
            val newMessages = character_chat_array.map { simpleMessage ->
                // 将 SimpleMessage 转换为 Message 对象
                Message(
                    id = simpleMessage.message_id, // 使用新 ID 或其他逻辑来区分
                    _id = simpleMessage.message_id, // 使用新 ID 或其他逻辑来区分
                    content = simpleMessage.content,
                    sender = simpleMessage.sender,
                    timestamp = simpleMessage.timestamp.toLongOrNull(),
                    message_type = simpleMessage.message_type,
                    isTyping = false
                )
            }
            // 4. 根据找到的位置进行替换和插入操作
            if (typingMessageIndex != -1) {
                // 找到了正在输入的消息，准备替换
                // a. 移除旧的 typingMessage
                messagesCopy.removeAt(typingMessageIndex)
                // b. 在原来 typingMessage 的位置，或其后，插入新的消息
                // 如果是单个元素，直接替换：
                if (newMessages.size == 1) {
                    messagesCopy.add(typingMessageIndex, newMessages.first())
                }
                // 如果是两个元素（例如，回复 + 额外的AI行动消息），按顺序插入：
                else if (newMessages.size == 2) {
                    messagesCopy.addAll(typingMessageIndex, newMessages)
                }
                // 注意：如果 newMessages.size > 2，这里也只会插入它们。
            } else {
                // 如果没找到 typingMessage（安全保护），则将新消息添加到列表末尾
                messagesCopy.addAll(newMessages)
            }
                Log.e("ChatActivity", "YXTEST lucky_reward   ${sendResult?.lucky_reward }" )
                sendResult?.lucky_reward?.let {
                    if (it.won_reward){
                        messagesCopy.add( Message(
                            sender = "lucky_reward",
                            won_reward = it.won_reward,
                            reward_amount = it.reward_amount,
                            reward_type = it.reward_type,
                        ))
                    }
                }

                // 5. 更新状态
            _state.value = _state.value.copy(
                isLoading = false,
                // 直接使用修改后的 messagesCopy 列表。
                // 如果 loadMessages 只是返回列表本身，可以简化为 messages = messagesCopy。
                messages = loadMessages(messagesCopy),
                isClearMessages = false,
                isTyping = false
            )
          } catch (e: Exception) {
            if (e is CancellationException) {
                ToastUtils.showError(context, "已退出聊天界面")
                return@launch  // 退出协程
            }

            // 发送失败时，移除正在输入的消息，使用唯一ID进行精确匹配
            val messagesWithoutTyping = _state.value.messages.filter { 
                it.id != typingMessage.id
            }
            _state.value = _state.value.copy(
                isLoading = false,
                messages = loadMessages(messagesWithoutTyping) ,
                error = "发送消息失败: ${e.message}",
                isTyping = false
            )
            ToastUtils.showError(context, "消息发送超时")
          }
        } // viewModelScope.launch 结束
    }

    /**
     * 清除消息
     */
    private suspend fun clearMessages(characterId :String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
             chatRepository.clearCharacterChat(characterId)
            // 检查响应状态码
            _state.value = _state.value.copy(
                isLoading = false,
                messages = emptyList(),
                isClearMessages = true
            )
        } catch (e: Exception) {
            // 其他异常
            ToastUtils.showError(context, "清除历史失败: ${e.message}")
            _state.value = _state.value.copy(
                isLoading = false,
                error = "清除历史失败: ${e.message}"
            )
        }
    }

    /**
     * 加载角色详情
     * @param characterId 角色ID
     */
    private suspend fun loadCharacterDetail(characterId: String) {
        if (characterId.isEmpty()) {
            ToastUtils.showError(context, "角色ID不能为空")
            _state.value = _state.value.copy(error = "角色ID不能为空")
            return
        }
        _state.value = _state.value.copy(isLoading = true, error = null)
        try {
            // 调用API获取角色详情
            val characterDetail = characterRepository.getCharacterDetail(characterId)
            // 检查响应状态码
            character = characterDetail.character
            _state.value = _state.value.copy(
                isLoading = false,
                character = character
            )
        } catch (e: Exception) {
            // 其他异常
            ToastUtils.showError(context, "加载角色详情失败: ${e.message}")
            _state.value = _state.value.copy(
                isLoading = false,
                error = "加载角色详情失败: ${e.message}"
            )
        }
    }

    /**
     * 加载默认角色聊天历史
     * @param page 页码
     * @param limit 每页限制
     */
    private suspend fun loadDefaultCharacterMessages(page: Int = 1, limit: Int = 50) {
        _state.value = _state.value.copy(isLoadingDefaultChat = true, error = null)
        try {
            val defaultCharacterChatData = chatRepository.getDefaultCharacterMessages(page, limit)
            var defaultCharacter = defaultCharacterChatData.character
            conversationId = defaultCharacterChatData.conversationId
            characterId = defaultCharacter.id
            character = defaultCharacter
            _state.value = _state.value.copy(
                isLoadingDefaultChat = false,
                messages = loadMessages(defaultCharacterChatData.messages) ,
                isClearMessages = false,
                character = defaultCharacter
            )
        }  catch (e: Exception) {
            // 其他异常
            ToastUtils.showError(context, "加载默认角色聊天历史失败: ${e.message}")
            _state.value = _state.value.copy(
                isLoadingDefaultChat = false,
                error = "加载默认角色聊天历史失败: ${e.message}"
            )
        }
    }
    fun loadMessages(apiMessages: List<Message>) : List<Message> {
        // 1. 调用 attachPlaceholder 确保占位符在末尾
        return attachPlaceholder(apiMessages)
    }
    private fun attachPlaceholder(baseMessages: List<Message>): List<Message> {
        // 1. 先检查 baseMessages 中是否已经有这个占位符，如果有，先移除它（防止重复）
        val cleanList = baseMessages.filter { !it.isReloadMessage }
        // 2. 将占位符添加到清理后的列表末尾
        return cleanList + reloadMessage
    }
}
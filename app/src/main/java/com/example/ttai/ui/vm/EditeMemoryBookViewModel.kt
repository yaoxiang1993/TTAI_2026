package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.MemoryBookResponse
import com.example.ttai.bean.UpdateProfileRequest
import com.example.ttai.intent.EditeBriefIntent
import com.example.ttai.intent.EditeMemoryBookIntent
import com.example.ttai.intent.EditeNameIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.AuthRepository
import com.example.ttai.network.repository.UserRepository
import com.example.ttai.state.EditeBriefState
import com.example.ttai.state.EditeMemoryBookState
import com.example.ttai.state.EditeNameState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditeMemoryBookViewModel(private val context: Context) : MviViewModel<EditeMemoryBookIntent, EditeMemoryBookState>() {
    
    private val userRepository: UserRepository = NetworkModule.provideUserRepository(context)
    
    private val _state = MutableStateFlow(EditeMemoryBookState())
    override val state: StateFlow<EditeMemoryBookState> = _state.asStateFlow()

    override fun processIntent(intent: EditeMemoryBookIntent) {
        when (intent) {
            is EditeMemoryBookIntent.LoadData -> {
                loadCurrentName(intent.character_id)
            }
            
            is EditeMemoryBookIntent.UpdateMemoryBook -> {
                updateName(intent.content)
            }
            
            is EditeMemoryBookIntent.SaveMemoryBook -> {
                saveName()
            }
            
            is EditeMemoryBookIntent.Back -> {
                // 返回逻辑在Activity中处理
            }
        }
    }
    
    private fun loadCurrentName(character_id :String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                var memoryBookResponse : MemoryBookResponse = userRepository.getMemoryBook(character_id)
                val nameToLoad = memoryBookResponse.memory_book.content
                _state.update { 
                    it.copy(
                        isLoading = false,
                        content = nameToLoad,
                        character_id = character_id,
                        contentLength = nameToLoad.length,
                        isContentValid = nameToLoad.isNotBlank()
                    )
                }
            } catch (e: Exception) {
                ToastUtils.showError(context, "加载数据失败: ${e.message}")
                _state.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "加载数据失败"
                    )
                }
            }
        }
    }
    
    private fun updateName(content: String) {
        val contentLength = content.length
        val isBriefValid = content.isNotBlank() && contentLength <= 3000
        
        _state.update { 
            it.copy(
                content = content,
                contentLength = contentLength,
                isContentValid = isBriefValid
            )
        }
    }
    
    private fun saveName() {
        viewModelScope.launch {
            val currentState = _state.value
            
            // 验证昵称
            if (currentState.content.isBlank()) {
                ToastUtils.showError(context, "请输入记忆")
                _state.update { 
                    it.copy(errorMessage = "请输入记忆")
                }
                return@launch
            }
            
            if (currentState.content.length > 3000) {
                ToastUtils.showError(context, "记忆不能超过3000个字符")
                _state.update { 
                    it.copy(errorMessage = "记忆不能超过3000个字符")
                }
                return@launch
            }
            
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            
            try {
                // 调用API更新用户资料
                userRepository.postMemoryBook(currentState.character_id,currentState.content)

                _state.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                // 其他异常
                ToastUtils.showError(context, "保存失败: ${e.message}")
                _state.update { 
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "保存失败"
                    )
                }
            }
        }
    }
} 
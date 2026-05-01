package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.Message
import com.example.ttai.bean.UpdateMessageResponse
import com.example.ttai.bean.UpdateProfileRequest
import com.example.ttai.intent.EditeMessageIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.AuthRepository
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.state.EditeMessageState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditeMessageViewModel(private val context: Context) : MviViewModel<EditeMessageIntent, EditeMessageState>() {

    val chatRepository =  ChatRepository(context)
    
    private val _state = MutableStateFlow(EditeMessageState())
    override val state: StateFlow<EditeMessageState> = _state.asStateFlow()

    // 当前用户名，从Intent中获取
    private var currentUserName: String? = ""
    
    /**
     * 设置当前用户名
     */
    fun setCurrentUserName(userName: String?) {
        currentUserName = userName
        // 立即更新状态
        updateName(userName)
    }
    
    override fun processIntent(intent: EditeMessageIntent) {
        when (intent) {
            is EditeMessageIntent.LoadData -> {
                loadCurrentName()
            }
            
            is EditeMessageIntent.UpdateName -> {
                updateName(intent.name)
            }
            
            is EditeMessageIntent.SaveName -> {
                saveName(intent.message)
            }
            
            is EditeMessageIntent.Back -> {
                // 返回逻辑在Activity中处理
            }
        }
    }
    
    private fun loadCurrentName() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        currentName = currentUserName
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
    
    private fun updateName(name: String?) {
        _state.update { 
            it.copy(
                currentName = name
            )
        }
    }
    
    private fun saveName( message : Message?) {
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                // 更新消息
                chatRepository.UpdateMessage(message)
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
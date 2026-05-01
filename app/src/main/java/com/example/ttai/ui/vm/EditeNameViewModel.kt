package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.UpdateProfileRequest
import com.example.ttai.intent.EditeNameIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.AuthRepository
import com.example.ttai.state.EditeNameState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditeNameViewModel(private val context: Context) : MviViewModel<EditeNameIntent, EditeNameState>() {
    
    private val authRepository: AuthRepository = NetworkModule.provideAuthRepository(context)
    
    private val _state = MutableStateFlow(EditeNameState())
    override val state: StateFlow<EditeNameState> = _state.asStateFlow()
    
    // 当前用户名，从Intent中获取
    private var currentUserName: String = ""
    
    /**
     * 设置当前用户名
     */
    fun setCurrentUserName(userName: String) {
        currentUserName = userName
        // 立即更新状态
        updateName(userName)
    }
    
    override fun processIntent(intent: EditeNameIntent) {
        when (intent) {
            is EditeNameIntent.LoadData -> {
                loadCurrentName()
            }
            
            is EditeNameIntent.UpdateName -> {
                updateName(intent.name)
            }
            
            is EditeNameIntent.SaveName -> {
                saveName()
            }
            
            is EditeNameIntent.Back -> {
                // 返回逻辑在Activity中处理
            }
        }
    }
    
    private fun loadCurrentName() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // 使用传入的当前用户名
                val nameToLoad = if (currentUserName.isNotEmpty()) currentUserName else ""
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        currentName = nameToLoad,
                        nameLength = nameToLoad.length,
                        isNameValid = nameToLoad.isNotBlank()
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
    
    private fun updateName(name: String) {
        val nameLength = name.length
        val isNameValid = name.isNotBlank() && nameLength <= 11
        
        _state.update { 
            it.copy(
                currentName = name,
                nameLength = nameLength,
                isNameValid = isNameValid
            )
        }
    }
    
    private fun saveName() {
        viewModelScope.launch {
            val currentState = _state.value
            
            // 验证昵称
            if (currentState.currentName.isBlank()) {
                ToastUtils.showError(context, "请输入昵称")
                _state.update { 
                    it.copy(errorMessage = "请输入昵称")
                }
                return@launch
            }
            
            if (currentState.currentName.length > 15) {
                ToastUtils.showError(context, "昵称不能超过15个字符")
                _state.update { 
                    it.copy(errorMessage = "昵称不能超过15个字符")
                }
                return@launch
            }
            
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            
            try {
                // 构建请求参数
                val request = UpdateProfileRequest(
                    username = currentState.currentName
                )
                // 调用API更新用户资料
                authRepository.updateUserProfile(request)

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
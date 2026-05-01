package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.UpdateProfileRequest
import com.example.ttai.intent.EditeBriefIntent
import com.example.ttai.intent.EditeNameIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.AuthRepository
import com.example.ttai.state.EditeBriefState
import com.example.ttai.state.EditeNameState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditeBriefViewModel(private val context: Context) : MviViewModel<EditeBriefIntent, EditeBriefState>() {
    
    private val authRepository: AuthRepository = NetworkModule.provideAuthRepository(context)
    
    private val _state = MutableStateFlow(EditeBriefState())
    override val state: StateFlow<EditeBriefState> = _state.asStateFlow()
    
    // 当前用户名，从Intent中获取
    private var currentBrief: String = ""
    
    /**
     * 设置当前用户名
     */
    fun setCurrentBrief(brief: String) {
        currentBrief = brief
        // 立即更新状态
        updateName(brief)
    }
    
    override fun processIntent(intent: EditeBriefIntent) {
        when (intent) {
            is EditeBriefIntent.LoadData -> {
                loadCurrentName()
            }
            
            is EditeBriefIntent.UpdateName -> {
                updateName(intent.brief)
            }
            
            is EditeBriefIntent.SaveName -> {
                saveName()
            }
            
            is EditeBriefIntent.Back -> {
                // 返回逻辑在Activity中处理
            }
        }
    }
    
    private fun loadCurrentName() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // 使用传入的当前用户名
                val nameToLoad = if (currentBrief.isNotEmpty()) currentBrief else ""
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        brief = nameToLoad,
                        briefLength = nameToLoad.length,
                        isBriefValid = nameToLoad.isNotBlank()
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
    
    private fun updateName(brief: String) {
        val briefLength = brief.length
        val isBriefValid = brief.isNotBlank() && briefLength <= 60
        
        _state.update { 
            it.copy(
                brief = brief,
                briefLength = briefLength,
                isBriefValid = isBriefValid
            )
        }
    }
    
    private fun saveName() {
        viewModelScope.launch {
            val currentState = _state.value
            
            // 验证昵称
            if (currentState.brief.isBlank()) {
                ToastUtils.showError(context, "请输入简介")
                _state.update { 
                    it.copy(errorMessage = "请输入简介")
                }
                return@launch
            }
            
            if (currentState.brief.length > 60) {
                ToastUtils.showError(context, "简介不能超过60个字符")
                _state.update { 
                    it.copy(errorMessage = "简介不能超过60个字符")
                }
                return@launch
            }
            
            _state.update { it.copy(isSaving = true, errorMessage = null) }
            
            try {
                // 构建请求参数
                val request = UpdateProfileRequest(
                    bio = currentState.brief
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
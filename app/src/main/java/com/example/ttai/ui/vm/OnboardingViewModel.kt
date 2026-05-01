package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.OnboardingIntent
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import com.example.ttai.network.repository.UserRepository
import com.example.ttai.state.OnboardingState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户引导状态管理ViewModel
 */
class OnboardingViewModel(
    private val apiService: ApiService,
    private val context: android.content.Context
) : MviViewModel<OnboardingIntent, OnboardingState>() {
    
    private val _state = MutableStateFlow(OnboardingState())
    override val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    private val userRepository = UserRepository(apiService, context)
    
    override fun processIntent(intent: OnboardingIntent) {
        viewModelScope.launch {
            when (intent) {
                is OnboardingIntent.CheckOnboardingStatus -> {
                    checkOnboardingStatus()
                }
                is OnboardingIntent.SkipToMain -> {
                    skipToMain()
                }
                is OnboardingIntent.ShowTagSelection -> {
                    showTagSelection()
                }
            }
        }
    }
    
    /**
     * 检查用户引导状态
     */
    private suspend fun checkOnboardingStatus() {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val onboardingData = userRepository.checkOnboardingStatus()
            
            _state.value = _state.value.copy(
                isLoading = false,
                hasPreferredTags = onboardingData.hasPreferredTags,
                preferredTags = onboardingData.preferredTags,
                shouldShowTagSelection = onboardingData.shouldShowTagSelection,
                canSkipToMain = onboardingData.canSkipToMain,
                isOnboardingComplete = onboardingData.canSkipToMain || onboardingData.hasPreferredTags
            )
            
            android.util.Log.d("OnboardingViewModel", "引导状态检查成功: hasPreferredTags=${onboardingData.hasPreferredTags}, canSkipToMain=${onboardingData.canSkipToMain}")
            
        } catch (e: ApiException) {
            android.util.Log.e("OnboardingViewModel", "API异常: ${e.message}")
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message
            )
            ToastUtils.showError(context, e.message)
        } catch (e: Exception) {
            android.util.Log.e("OnboardingViewModel", "检查引导状态失败", e)
            _state.value = _state.value.copy(
                isLoading = false,
                error = "检查引导状态失败: ${e.message}"
            )
            ToastUtils.showError(context, "检查引导状态失败: ${e.message}")
        }
    }
    
    /**
     * 跳过引导，直接进入主页面
     */
    private fun skipToMain() {
        android.util.Log.d("OnboardingViewModel", "用户选择跳过引导")
        _state.value = _state.value.copy(
            isOnboardingComplete = true,
            canSkipToMain = true
        )
    }
    
    /**
     * 显示标签选择页面
     */
    private fun showTagSelection() {
        android.util.Log.d("OnboardingViewModel", "显示标签选择页面")
        _state.value = _state.value.copy(
            shouldShowTagSelection = true
        )
    }
}

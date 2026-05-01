package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.SplashIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.SplashState
import com.example.ttai.utils.MMKVUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel(private val context: Context)  : MviViewModel<SplashIntent, SplashState>() {
    private val _state = MutableStateFlow(SplashState())
    override val state: StateFlow<SplashState> = _state.asStateFlow()
    private val authRepository = NetworkModule.provideAuthRepository(context)
    var deviceId = ""

    override fun processIntent(intent: SplashIntent) {
        viewModelScope.launch {
            when (intent) {
                is SplashIntent.OtherLogin -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        navigateToLogin = true
                    )
                }
                is SplashIntent.Login -> {
                    try {
                        _state.value = _state.value.copy(isLoading = true)
                        // 使用一个默认手机号和设备ID进行一键登录
                        // 在实际应用中，应该从设备获取真实的手机号和设备ID
                        val phone = MMKVUtils.getString(MMKVUtils.PHONE) // 示例手机号
                        // 调用一键登录接口
                        val authData = authRepository.oneClickAuth(phone, deviceId)
                        NetworkModule.setAuthToken(authData.token)
                        val checkOnboardingStatus = authRepository.checkOnboardingStatus()
                        // 首次使用，标记为非首次使用并跳转到选择标签页面
                        MMKVUtils.putBoolean(MMKVUtils.IS_FIRST_USE, false)
                        MMKVUtils.putUserProfile(authData.user)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            navigateToSeletcTag = checkOnboardingStatus?.shouldShowTagSelection == true,
                            navigateToMain = checkOnboardingStatus?.canSkipToMain == true
                        )

                    } catch (e: Exception) {
                        // 登录失败，显示错误信息
                        _state.value = _state.value.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }
}
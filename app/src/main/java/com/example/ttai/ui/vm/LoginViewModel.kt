package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.LoginIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.exception.ApiException
import com.example.ttai.state.LoginState
import com.example.ttai.utils.MMKVUtils
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val context: Context) : MviViewModel<LoginIntent, LoginState>() {
    private val _state = MutableStateFlow(LoginState())
    override val state: StateFlow<LoginState> = _state.asStateFlow()
    
    private val authRepository = NetworkModule.provideAuthRepository(context)
    public var deviceId = ""

    override fun processIntent(intent: LoginIntent) {
        viewModelScope.launch {
            when (intent) {
                is LoginIntent.OneClickAuth -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        val data = authRepository.oneClickAuth(intent.phone, deviceId)
                        NetworkModule.setAuthToken(data.token)
                        // 保存token到MMKV
                        MMKVUtils.putString(MMKVUtils.Keys.USER_TOKEN, data.token)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            navigateToMain = true,
                            isNewUser = false // 可根据data内容调整
                        )
                    } catch (e: Exception) {
                        // 其他异常
                        ToastUtils.showError(context, "登录失败: ${e.message}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
                
                is LoginIntent.SendCode -> {
                    _state.value = _state.value.copy(
                        smsButtonEnabled = false,
                        countdownSeconds = 60
                    )
                    
                    try {
                        startCountdown()
                        authRepository.sendCode(intent.phone)
                        _state.value = _state.value.copy(
                            sendCodeSuccess = true
                        )
                    }  catch (e: Exception) {
                        // 其他异常
                        ToastUtils.showError(context, "发送验证码失败: ${e.message}")
                        _state.value = _state.value.copy(
                            error = e.message,
                            smsButtonEnabled = true,
                            countdownSeconds = 0
                        )
                    }
                }
                
                is LoginIntent.VerifyCodeAuth -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                        val data = authRepository.verifyCodeAuth(intent.phone, intent.code,intent.invitationCode, deviceId)
                        NetworkModule.setAuthToken(data.token)
                        // 保存token到MMKV
                        MMKVUtils.putString(MMKVUtils.Keys.USER_TOKEN, data.token)
                        MMKVUtils.putString(MMKVUtils.PHONE,intent.phone)
                        MMKVUtils.putUserProfile(data.user)

                        val checkOnboardingStatus = authRepository.checkOnboardingStatus()

                        _state.value = _state.value.copy(
                            isLoading = false,
                            navigateToMain = checkOnboardingStatus?.canSkipToMain == true,
                            navigateToTag = checkOnboardingStatus?.shouldShowTagSelection == true,
                            isNewUser = false // 可根据data内容调整
                        )
                    }  catch (e: Exception) {
                        // 其他异常
                        ToastUtils.showError(context, "验证码登录失败: ${e.message}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
            }
        }
    }
    
    private fun startCountdown() {
        viewModelScope.launch {
            repeat(60) {
                delay(1000)
                val currentSeconds = _state.value.countdownSeconds - 1
                _state.value = _state.value.copy(
                    countdownSeconds = currentSeconds,
                    smsButtonText = if (currentSeconds > 0) "Resend in $currentSeconds s" else "Get SMS Code",
                    smsButtonEnabled = currentSeconds <= 0
                )
            }
        }
    }
    
    /**
     * 重置发送验证码成功状态
     */
    fun resetSendCodeSuccess() {
        _state.value = _state.value.copy(sendCodeSuccess = false)
    }
}
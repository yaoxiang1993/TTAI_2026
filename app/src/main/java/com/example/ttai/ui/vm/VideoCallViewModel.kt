package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.VideoCallIntent
import com.example.ttai.network.ApiService
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.VideoCallState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoCallViewModel(
    private val apiService: ApiService,
    private val context: Context
) : MviViewModel<VideoCallIntent, VideoCallState>() {
    private val voiceConfigRepository = NetworkModule.provideVoiceConfigRepository(context)
    private val _state = MutableStateFlow(VideoCallState())
    override val state: StateFlow<VideoCallState> = _state.asStateFlow()
    var characterId :String?= ""
    var conversationId :String  = ""
    var sessionId :String  = ""
    // VideoCallViewModel.kt
    override fun processIntent(intent: VideoCallIntent) {
        viewModelScope.launch {
            when (intent) {
                is VideoCallIntent.ConnectVideo -> {
                    characterId = intent.characterId
                    _state.value = _state.value.copy(isConnecting = true,isPlaying = true,isVoice = true,isEnd = false)
                    try {
                        // 调用开始通话接口获取 IMS 参数
                        var response = voiceConfigRepository.getVoiceCallActive(characterId?:"")
// 如果 data 为 ""，response 里的 sdk 字段通常为 null
                        if (response?.sdk == null) {
                            Log.d("VideoCallViewModel", "没有活跃会话，申请新会话")
                            response = voiceConfigRepository.startVoiceCall(characterId)
                        } else {
                            Log.d("VideoCallViewModel", "检测到存活会话，直接恢复")
                        }
                        sessionId = response?.sessionId.toString()
                        _state.value = _state.value.copy(
                            isConnecting = false,
                            isPlaying = true,
                            sessionId = response?.sessionId,
                            sdkConfig = response?.sdk,
                            imsConfig = response?.ims
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(isConnecting = false, error = e.message)
                    }
                }
                is VideoCallIntent.SendUserTurn -> {
                    // 在 IMS 模式下，可以手动发送文本打断或交互
                    try {
                        // 调用开始通话接口获取 IMS 参数
                        val response = voiceConfigRepository.sendUserTurn(sessionId, intent.text)
                        _state.value =
                            _state.value.copy(isConnecting = false, isPlaying = true, isEnd = false)
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(isConnecting = false, error = e.message)
                    }
                }
                is VideoCallIntent.EndCall -> {
                    try {
                        // 调用开始通话接口获取 IMS 参数
                        val response =
                            voiceConfigRepository.endVoiceCall( sessionId) // 通知后端结束实例
                        _state.value =
                            _state.value.copy(isConnecting = false, isPlaying = false, isEnd = true)
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(isConnecting = false, error = e.message)
                    }

                }
                is VideoCallIntent.ToggleMic -> {
                    // 仅更新本地状态，Activity 会观察此状态并调用 SDK
                    _state.value = _state.value.copy(
                        isMicrophoneOpen = intent.isMute // 这里取决于你 Intent 的定义逻辑
                    )
                }
                is VideoCallIntent.changeToVoice -> {
                    _state.value = _state.value.copy(
                        isVoice = intent.isVoice
                    )
                }
                is VideoCallIntent.changePlayStatus -> {
                    _state.value = _state.value.copy(
                        playStatus = intent.playStatus
                    )
                }
                is VideoCallIntent.AIAgentReply -> {
                    val text = intent.text ?: ""
                    val isEnd = intent.isEnd
                    // 我们在 State 中维护一个专门给 TextView 显示的字段
                    // 假设这个字段叫 currentSubtitle
                    if (!isEnd) {
                        // 还没结束：在文本后面加一个“光标”符号 ▍ 模拟输入感
                        _state.value = _state.value.copy(
                            currentSubtitle = "$text"
                        )
                    }
//                    else {
//                        // 结束了：移除光标，显示最终文本
//                        _state.value = _state.value.copy(
//                            currentSubtitle = text
//                        )
//                    }
                }
            }
        }
    }

}
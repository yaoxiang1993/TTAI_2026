package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.EditIntent
import com.example.ttai.intent.MyChatSettingIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.EditState
import com.example.ttai.state.MyChatSettingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyChatSettingViewModel(private val context: Context) : MviViewModel<MyChatSettingIntent, MyChatSettingState>() {
    private val _state = MutableStateFlow(MyChatSettingState())
    override val state: StateFlow<MyChatSettingState> = _state.asStateFlow()

    private val charcterRepository = NetworkModule.provideCharacterRepository(context)


    override fun processIntent(intent: MyChatSettingIntent) {
        viewModelScope.launch {
            when (intent) {
                is MyChatSettingIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = true, error = null)
                    try {
                     var response = charcterRepository.getConversationSettings(intent.character_id)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            gender = response.gender,
                            nickname = response.nickname,
                            identity = response.identity,
                            personality_description = response.personality_description,
                            error = null)
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "加载数据失败: ${e.message}"
                        )
                    }
                }

                is MyChatSettingIntent.SaveChatSetting -> {
                    saveChatSetting(intent.character_id,intent.nickname,intent.identity,intent.gender,intent.personality_description)
                }

                is MyChatSettingIntent.SelectGender -> {
                    _state.update { it.copy(gender = intent.gender) }
                }
            }
        }
    }

    fun saveChatSetting( character_id: String?, nickname: String?, identity: String?, gender: String?, personality_description: String?){
        viewModelScope.launch {
            try {
                var response = charcterRepository.updateConversationSettings(
                    character_id,nickname,gender,identity,personality_description
                )
                _state.value = _state.value.copy(
                    saveSuccess = true,
                    error = "保存成功"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "加载数据失败: ${e.message}"
                )
            }
        }
    }
}
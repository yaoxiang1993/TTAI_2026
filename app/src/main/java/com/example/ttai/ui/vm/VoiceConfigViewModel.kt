package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.VoiceConfigData
import com.example.ttai.bean.VoiceOption
import com.example.ttai.intent.FollowersListIntent
import com.example.ttai.intent.FollowingListIntent
import com.example.ttai.intent.VoiceConfigIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.exception.ApiException
import com.example.ttai.state.FollowersListState
import com.example.ttai.state.VoiceConfigState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VoiceConfigViewModel(private val context: Context) : MviViewModel<VoiceConfigIntent, VoiceConfigState>() {
    private val _state = MutableStateFlow(VoiceConfigState())
    override val state: StateFlow<VoiceConfigState> = _state.asStateFlow()
    var characterId :String =""
    var selectVoiceOption :VoiceOption? = null
    private val voiceConfigRepository = NetworkModule.provideVoiceConfigRepository(context)
    override fun processIntent(intent: VoiceConfigIntent) {
        viewModelScope.launch {
            when (intent) {
                is VoiceConfigIntent.Initialize -> {
                    characterId = intent.character_id?:""
                    fetchVoiceConfigs(characterId)
                }
                is VoiceConfigIntent.SelectItem -> {
                    updateSelection(intent.selectedCode) // 处理点击切换逻辑
                }
                is VoiceConfigIntent.SaveVoiceConfig -> {
                    saveVoiceConfig(characterId,selectVoiceOption)
                }
            }
        }
    }
    private suspend fun fetchVoiceConfigs(characterId: String) {
        viewModelScope.launch {
            try {
                val response =  voiceConfigRepository.getVoiceConfig(characterId)
                // 1. 尝试找到后端标记为选中的项，如果没有，则默认选中第一个
                val defaultSelection = response.voiceOptions.find { it.isSelected }
                    ?: response.voiceOptions.firstOrNull()

                selectVoiceOption = defaultSelection

                // 2. 为了保证 UI 显示一致，如果是因为默认选中了第一个，需要同步更新列表里的 isSelected 状态
                val updatedOptions = response.voiceOptions.map {
                    it.copy(isSelected = it.voiceCode == defaultSelection?.voiceCode)
                }
                _state.value = _state.value.copy(
                    voiceConfigData = response.copy(voiceOptions = updatedOptions),
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }
    private fun updateSelection(selectedCode: String) {
        val currentData = _state.value.voiceConfigData ?: return

        // 2. 找到用户新选中的 item
        val newSelection = currentData.voiceOptions.find { it.voiceCode == selectedCode }

        if (newSelection != null) {
            // 更新本地变量，供 saveVoiceConfig 使用
            selectVoiceOption = newSelection

            // 更新 UI 状态，让列表刷新选中效果
            val updatedOptions = currentData.voiceOptions.map {
                it.copy(isSelected = it.voiceCode == selectedCode)
            }
            _state.update {
                it.copy(voiceConfigData = currentData.copy(voiceOptions = updatedOptions))
            }
        }
    }
    private fun saveVoiceConfig(characterId: String, voiceOption: VoiceOption?) {
        if (voiceOption == null) return // 防御性编程
        viewModelScope.launch {
            try {
                val response = voiceConfigRepository.saveVoiceConfig(
                    characterId,
                    voiceOption.voiceType,
                    voiceOption.voiceCode
                )
                _state.value = _state.value.copy(
                    voiceConfigData = response,
                    isLoading = false,
                    saveVoiceSuccess = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }
}

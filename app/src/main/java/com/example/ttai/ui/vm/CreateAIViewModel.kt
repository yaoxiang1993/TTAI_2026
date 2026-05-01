package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.CreateCharacterRequest
import com.example.ttai.intent.CreateAIIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.exception.ApiException
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.state.CreateAIState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateAIViewModel(private val context: Context) : MviViewModel<CreateAIIntent, CreateAIState>() {
    
    private val characterRepository: CharacterRepository = NetworkModule.provideCharacterRepository(context)
    private val selectTagRepository = NetworkModule.provideSelectTagRepository(context)
    
    private val _state = MutableStateFlow(CreateAIState())
    override val state: StateFlow<CreateAIState> = _state.asStateFlow()
    
    override fun processIntent(intent: CreateAIIntent) {
        when (intent) {
            is CreateAIIntent.SelectImage -> {
                // 图片选择逻辑在Activity中处理
            }
            
            is CreateAIIntent.SelectGender -> {
                _state.update { it.copy(gender = intent.gender) }
            }
            
            is CreateAIIntent.UpdateName -> {
                val name = intent.name
                val nameLength = name.length
                val isNameValid = name.isNotBlank()
                
                _state.update { 
                    it.copy(
                        name = name,
                        nameLength = nameLength,
                        isNameValid = isNameValid
                    )
                }
                updateCreateButtonState()
            }
            
            is CreateAIIntent.UpdateSetting -> {
                val setting = intent.setting
                val settingLength = setting.length
                val isSettingValid = setting.isNotBlank()
                
                _state.update { 
                    it.copy(
                        setting = setting,
                        settingLength = settingLength,
                        isSettingValid = isSettingValid
                    )
                }
                updateCreateButtonState()
            }
            
            is CreateAIIntent.UpdateIntroduce -> {
                val introduce = intent.introduce
                val introduceLength = introduce.length
                val isIntroduceValid = introduce.isNotBlank()
                
                _state.update { 
                    it.copy(
                        introduce = introduce,
                        introduceLength = introduceLength,
                        isIntroduceValid = isIntroduceValid
                    )
                }
                updateCreateButtonState()
            }
            
            is CreateAIIntent.UpdatePrologue -> {
                val prologue = intent.prologue
                val prologueLength = prologue.length
                val isPrologueValid = prologue.isNotBlank()
                
                _state.update { 
                    it.copy(
                        prologue = prologue,
                        prologueLength = prologueLength,
                        isPrologueValid = isPrologueValid
                    )
                }
                updateCreateButtonState()
            }
            
            is CreateAIIntent.SelectTag -> {
                var currentState = _state.value
                val currentTags = currentState.selectedTags.toMutableSet()
                if (currentTags.contains(intent.tag)) {
                    currentTags.remove(intent.tag)
                } else {
                    if ((currentState.selectedTags.size + currentState.customTags.size) > 4) {
                        ToastUtils.showShort(context, "标签最多选5个")
                        return
                    }
                    currentTags.add(intent.tag)
                }
                _state.update { it.copy(selectedTags = currentTags,showCustomTagDialog = false) }
            }
            
            is CreateAIIntent.ShowCustomTagDialog -> {
                _state.update { it.copy(showCustomTagDialog = intent.isShow) }
            }
            
            is CreateAIIntent.AddCustomTag -> {
                var currentState = _state.value
                if ((currentState.selectedTags.size + currentState.customTags.size) > 4) {
                    ToastUtils.showShort(context, "标签最多选5个")
                    return
                }

                val currentCustomTags = _state.value.customTags.toMutableList()
                if (currentCustomTags.size < 10 && intent.tag.isNotBlank()) {
                    currentCustomTags.add(intent.tag)
                    _state.update { 
                        it.copy(
                            customTags = currentCustomTags,
                            showCustomTagDialog = false
                        )
                    }
                }
            }
            
            is CreateAIIntent.RemoveCustomTag -> {
                val currentCustomTags = _state.value.customTags.toMutableList()
                currentCustomTags.remove(intent.tag)
                _state.update { 
                    it.copy(customTags = currentCustomTags)
                }
            }
            
            is CreateAIIntent.ClearAllTags -> {
                _state.update { 
                    it.copy(
                        selectedTags = emptySet(),
                        customTags = emptyList(),
                        showCustomTagDialog = false
                    )
                }
                ToastUtils.showShort(context, "已清除所有标签")
            }

            is CreateAIIntent.SetPublishType -> {
                _state.update {
                    it.copy(isPrivate = intent.isPrivate)
                }
            }
            is CreateAIIntent.SetUnlimitedType -> {
                _state.update {
                    it.copy(isUnlimited = intent.isUnlimited)
                }
            }
            
            is CreateAIIntent.CreateAI -> {
                createAI(intent.id)
            }
            
            is CreateAIIntent.Back -> {
                // 返回逻辑在Activity中处理
            }
            is CreateAIIntent.CharactersTags ->{
                getCharactersTags()
            }
        }
    }

    private fun getCharactersTags() {
        viewModelScope.launch {
            try {
                android.util.Log.d("CreateAIViewModel", "开始获取角色标签...")
                
                // 从API加载性格标签
                val personalityTagsResponse = selectTagRepository.getPersonalityTags()
                _state.value = _state.value.copy(
                    tags = personalityTagsResponse.tags,
                    errorMessage = null
                )
            } catch (e: Exception) {
                android.util.Log.e("CreateAIViewModel", "获取标签失败: ${e.message}", e)
                _state.update {
                    it.copy(
                        errorMessage = "获取标签失败: ${e.message}"
                    )
                }
            }
        }
    }

    private fun updateCreateButtonState() {
        val currentState = _state.value
        val isEnabled = currentState.isNameValid && 
                       currentState.isSettingValid && 
                       currentState.isIntroduceValid && 
                       currentState.isPrologueValid &&
                       !currentState.isCreating
        
        android.util.Log.d("CreateAIViewModel", "按钮状态检查: " +
            "name='${currentState.name}' isNameValid=${currentState.isNameValid}, " +
            "setting='${currentState.setting}' isSettingValid=${currentState.isSettingValid}, " +
            "introduce='${currentState.introduce}' isIntroduceValid=${currentState.isIntroduceValid}, " +
            "prologue='${currentState.prologue}' isPrologueValid=${currentState.isPrologueValid}, " +
            "isCreating=${currentState.isCreating}, " +
            "finalEnabled=$isEnabled")
        
        _state.update { it.copy(isCreateButtonEnabled = isEnabled) }
    }
    
    private fun createAI( id :String?) {
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, errorMessage = null) }
            
            try {
                val currentState = _state.value
                
                // 验证必填字段
                if (currentState.name.isBlank()) {
                    throw IllegalArgumentException("请输入智能体名称")
                }
                if (currentState.setting.isBlank()) {
                    throw IllegalArgumentException("请输入智能体设定")
                }
                if (currentState.introduce.isBlank()) {
                    throw IllegalArgumentException("请输入对外简介")
                }
                if (currentState.prologue.isBlank()) {
                    throw IllegalArgumentException("请输入开场白")
                }
                
                // 构建请求参数
                val request = CreateCharacterRequest(
                    id = id,
                    name = currentState.name,
                    description = currentState.setting, // 智能体设定作为描述
                    personalityTags = (currentState.selectedTags + currentState.customTags).toList(),
                    briefIntro = currentState.introduce, // 对外简介
                    avatarUrl = currentState.imageUri ?: "", // 头像URL
                    openingLine = currentState.prologue, // 开场白
                    gender = currentState.gender,
                    voiceType = "default", // 默认语音类型
                    isPublic = !currentState.isPrivate, // 修复逻辑：私密状态的反值才是公开状态
                    isUnlimited = currentState.isUnlimited
                )
                
                // 调用API创建角色
                android.util.Log.d("CreateAIViewModel", "开始调用API创建角色...")
                val result = characterRepository.createCharacter(request)
                android.util.Log.d("CreateAIViewModel", "API调用成功，结果: $result")
                
                _state.update { 
                    it.copy(
                        isCreating = false,
                        createSuccessAIID = id,
                        createSuccess = true
                    )
                }
                android.util.Log.d("CreateAIViewModel", "状态更新完成: createSuccess = true")
            } catch (e: Exception) {
                android.util.Log.e("CreateAIViewModel", "创建角色失败: ${e.message}", e)
                if (e is ApiException){
                    _state.update {
                        it.copy(
                            isCreating = false,
                            errorMessage = (e as ApiException).message ?: "创建失败"
                        )
                    }
                }else{
                    _state.update {
                        it.copy(
                            isCreating = false,
                            errorMessage = e.message ?: "创建失败"
                        )
                    }
                }

                android.util.Log.d("CreateAIViewModel", "错误状态更新完成: errorMessage = ${e.message}")
            }
        }
    }
    
    fun updateImageUri(uri: String?) {
        _state.update { 
            it.copy(
                imageUri = uri,
                isImageSelected = uri != null
            )
        }
    }
} 
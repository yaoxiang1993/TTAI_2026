package com.example.ttai.state

import com.example.ttai.base.MviState
import com.example.ttai.bean.PersonalityTag

data class CreateAIState(
    // 图片相关
    val imageUri: String? = null,
    val isImageSelected: Boolean = false,
    
    // 基本信息
    val name: String = "",
    val gender: String = "male",
    val nameLength: Int = 0,
    
    // 智能体设定
    val setting: String = "",
    val settingLength: Int = 0,
    val introduce: String = "",
    val introduceLength: Int = 0,
    val prologue: String = "",
    val prologueLength: Int = 0,
    
    // 标签选择
    val selectedTags: Set<String> = emptySet(),
    val customTags: List<String> = emptyList(),
    val showCustomTagDialog: Boolean = false,
    
    // 创建状态
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val errorMessage: String? = null,
    
    // 验证状态
    val isNameValid: Boolean = true,
    val isSettingValid: Boolean = true,
    val isIntroduceValid: Boolean = true,
    val isPrologueValid: Boolean = true,
    
    // 按钮状态
    val isCreateButtonEnabled: Boolean = false,
    val tags: List<PersonalityTag> = emptyList(),

    val createSuccessAIID: String? = "",
    // 发布设置
    val isPrivate: Boolean = true  ,// 默认为私密
    // 是否无限制
    val isUnlimited: Boolean = false  // 默认为无限制


) : MviState
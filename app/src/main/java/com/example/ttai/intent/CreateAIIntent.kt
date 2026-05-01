package com.example.ttai.intent

import com.example.ttai.base.MviIntent

sealed class CreateAIIntent : MviIntent {
    // 图片相关
    object SelectImage : CreateAIIntent()
    
    // 性别选择
    data class SelectGender(val gender: String) : CreateAIIntent()
    
    // 文本输入
    data class UpdateName(val name: String) : CreateAIIntent()
    data class UpdateSetting(val setting: String) : CreateAIIntent()
    data class UpdateIntroduce(val introduce: String) : CreateAIIntent()
    data class UpdatePrologue(val prologue: String) : CreateAIIntent()
    
    // 标签选择
    data class SelectTag(val tag: String) : CreateAIIntent()
    
    // 自定义标签
    data class ShowCustomTagDialog(val isShow: Boolean) : CreateAIIntent()
    data class AddCustomTag(val tag: String) : CreateAIIntent()
    data class RemoveCustomTag(val tag: String) : CreateAIIntent()
    object ClearAllTags : CreateAIIntent()

    // 发布设置
    data class SetPublishType(val isPrivate: Boolean) : CreateAIIntent()
    // 无限制
    data class SetUnlimitedType(val isUnlimited: Boolean) : CreateAIIntent()
    
    // 创建智能体
    data class CreateAI(val id: String?) : CreateAIIntent()

    // 返回
    object Back : CreateAIIntent()
    // 返回
    object CharactersTags : CreateAIIntent()


} 
package com.example.ttai.intent

import com.example.ttai.base.MviIntent
import com.example.ttai.bean.Message

sealed class EditeMessageIntent : MviIntent {
    object LoadData : EditeMessageIntent()
    data class UpdateName(val name: String) : EditeMessageIntent()
    data class SaveName(val message : Message?) : EditeMessageIntent()
    object Back : EditeMessageIntent()
} 
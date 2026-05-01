package com.example.ttai.intent

import com.example.ttai.base.MviIntent

sealed class EditeNameIntent : MviIntent {
    object LoadData : EditeNameIntent()
    data class UpdateName(val name: String) : EditeNameIntent()
    object SaveName : EditeNameIntent()
    object Back : EditeNameIntent()
} 
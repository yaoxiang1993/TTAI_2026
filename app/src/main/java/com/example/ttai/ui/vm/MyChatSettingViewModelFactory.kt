package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * MyChatSettingViewModel的工厂类
 * 用于提供Context参数给MyChatSettingViewModel
 */
class MyChatSettingViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyChatSettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyChatSettingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
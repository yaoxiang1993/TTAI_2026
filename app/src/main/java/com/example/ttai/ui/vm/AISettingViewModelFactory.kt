package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ttai.network.ApiService
import com.example.ttai.network.repository.PermanentMemoryRepository

class AISettingViewModelFactory(
    private val apiService: ApiService,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AISettingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AISettingViewModel(apiService, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
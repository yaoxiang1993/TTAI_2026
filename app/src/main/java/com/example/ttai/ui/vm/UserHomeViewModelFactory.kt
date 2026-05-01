package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ttai.network.ApiService

class UserHomeViewModelFactory(
    private val apiService: ApiService,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserHomeViewModel(apiService, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
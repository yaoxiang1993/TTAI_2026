package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ttai.network.ApiService

class OnboardingViewModelFactory(
    private val apiService: ApiService,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(apiService, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

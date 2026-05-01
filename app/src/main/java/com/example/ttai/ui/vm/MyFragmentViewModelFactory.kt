package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyFragmentViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyFragmentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
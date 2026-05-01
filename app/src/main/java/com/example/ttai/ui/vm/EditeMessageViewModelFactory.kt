package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EditeMessageViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditeMessageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditeMessageViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

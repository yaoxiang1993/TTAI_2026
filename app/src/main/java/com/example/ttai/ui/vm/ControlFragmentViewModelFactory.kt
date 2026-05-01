package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ttai.ui.fragment.ControlFragment

/**
 * ControlFragmentViewModel的工厂类
 * 用于提供Context参数给ControlFragmentViewModel
 */
class ControlFragmentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ControlFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ControlFragmentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
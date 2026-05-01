package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * MembershipViewModel的工厂类
 * 用于提供Context参数给MembershipViewModel
 */
class MembershipViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MembershipViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MembershipViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
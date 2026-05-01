package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ChargeMoneyViewModel的工厂类
 * 用于提供Context参数给ChargeMoneyViewModel
 */
class ChargeMoneyViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChargeMoneyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChargeMoneyViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

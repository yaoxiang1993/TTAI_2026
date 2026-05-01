package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ShopFragmentViewModel的工厂类
 * 用于提供Context参数给ShopFragmentViewModel
 */
class ShopFragmentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopFragmentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

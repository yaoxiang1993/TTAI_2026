package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ShopOwnedFragmentViewModel的工厂类
 * 用于提供Context参数给ShopOwnedFragmentViewModel
 */
class ShopOwnedFragmentViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopOwnedFragmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopOwnedFragmentViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

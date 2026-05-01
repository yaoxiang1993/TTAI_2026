package com.example.ttai.ui.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedDataViewModel : ViewModel() {
    // 使用 MutableLiveData 来存储 isUnlimited，默认值可以根据您的实际情况设置
    private val _isUnlimited = MutableLiveData<Boolean>()
    val isUnlimited: LiveData<Boolean> = _isUnlimited

    fun setIsUnlimited(isLimited: Boolean) {
        _isUnlimited.value = isLimited
    }
}
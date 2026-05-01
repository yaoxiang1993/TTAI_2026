package com.example.ttai.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


// MVI ViewModel基类
abstract class MviViewModel<I : MviIntent, S : MviState> : ViewModel() {
    abstract val state: StateFlow<S>
    abstract fun processIntent(intent: I)
}
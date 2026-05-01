package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.NotificationsEntity
import com.example.ttai.intent.NotifyDetailsIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.NotifyDetailsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotifyDetailsViewModel(private val context: Context) : MviViewModel<NotifyDetailsIntent, NotifyDetailsState>() {
    private val _state = MutableStateFlow(NotifyDetailsState())
    override val state: StateFlow<NotifyDetailsState> = _state.asStateFlow()
    private val notifyRepository = NetworkModule.provideNotifyRepository(context)
    override fun processIntent(intent: NotifyDetailsIntent) {
        viewModelScope.launch {
            when (intent) {
                is NotifyDetailsIntent.notificationsMarkRead -> {
                    notificationsMarkRead(intent.notifyEntity)
                }
            }
        }
    }
    suspend fun notificationsMarkRead(notifyEntity: NotificationsEntity?){
        _state.value = _state.value.copy(isLoading = false)
        try {
            val notificationsResponse = notifyRepository.notificationsMarkRead(notifyEntity?.id)
                _state.value.copy(
                    isLoading = false,
                )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
            )
        }
    }
}

package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.MyBluetoothManager
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.Character
import com.example.ttai.bean.Message
import com.example.ttai.bean.SimpleMessage
import com.example.ttai.intent.ChatIntent
import com.example.ttai.intent.UserHomeIntent
import com.example.ttai.intent.VideoCallIntent
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.state.ChatState
import com.example.ttai.state.VideoCallState
import com.example.ttai.utils.JsonUtils
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import org.json.JSONObject
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class VideoCallViewModel(
    private val apiService: ApiService,
    private val context: Context
) : MviViewModel<VideoCallIntent, VideoCallState>() {
    private val chatRepository = ChatRepository(context)
    private var characterRepository: CharacterRepository = CharacterRepository(apiService,context)
    val reloadMessage = Message(
        id = "",
        content = "",
        sender = "reload_message",
        isReloadMessage = true,
        timestamp = 0
    )
    private val _state = MutableStateFlow(VideoCallState())
    override val state: StateFlow<VideoCallState> = _state.asStateFlow()
    var characterId :String?= ""
    var conversationId :String  = ""
    var character : Character? = null

    override fun processIntent(intent: VideoCallIntent) {

        viewModelScope.launch {
            when (intent) {
                is VideoCallIntent.ConnectVideo -> {
                    _state.value = _state.value.copy(
                        isConnecting = true
                    )
                }
                else -> {}
            }
        }
    }

}
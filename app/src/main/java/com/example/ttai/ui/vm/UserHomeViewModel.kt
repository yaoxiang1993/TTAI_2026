package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.AISettingIntent
import com.example.ttai.intent.FollowersListIntent
import com.example.ttai.intent.SearchIntent
import com.example.ttai.intent.UserHomeIntent
import com.example.ttai.state.AISettingState
import com.example.ttai.network.ApiService
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.exception.ApiException
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.network.repository.PermanentMemoryRepository
import com.example.ttai.network.repository.UserRepository
import com.example.ttai.state.UserHomeState
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserHomeViewModel(
    private val apiService: ApiService,
    private val context: Context
) : MviViewModel<UserHomeIntent, UserHomeState>() {
    private val _state = MutableStateFlow(UserHomeState())
    override val state: StateFlow<UserHomeState> = _state.asStateFlow()

    private val userRepository = NetworkModule.provideUserRepository(context)
    private val characterRepository = NetworkModule.provideCharacterRepository(context)

    private val followerRepository = NetworkModule.provideFollowerRepository(context)
    var characterId :String  = ""
    override fun processIntent(intent: UserHomeIntent) {
        viewModelScope.launch {
            when (intent) {
               is UserHomeIntent.Initialize -> {
                   getUserInfo(intent.userId)
                   getCharacterList(intent.userId)
               }
                is UserHomeIntent.FollowSwitch -> {
                    setFollowUser()
                }
                is UserHomeIntent.PerformSearch -> {
                    loadCharacters(  searchQuery =  intent.query)

                }
                is UserHomeIntent.ClearSearch -> {
                    _state.value = _state.value.copy(
                        items = emptyList(),
                        searchQuery = "",
                        isSearching = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private suspend fun loadCharacters(
        searchQuery :String = "",
    ) {
        _state.value = _state.value.copy(
            isSearching = true,
            searchQuery = searchQuery,
            errorMessage = null
        )
        try {
            // 执行搜索
            val searchResponse = characterRepository.searchCharacters(
                keyword = searchQuery,
                page = 1,
                limit = 100
            )
            _state.value = _state.value.copy(
                items = searchResponse.characters,
                isSearching = false,
                errorMessage = null
            )
        } catch (e: ApiException) {
            _state.value = _state.value.copy(
                isSearching = false,
                errorMessage = e.message ?: "搜索失败"
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isSearching = false,
                errorMessage = "网络错误: ${e.message}"
            )
        }
    }
    fun getUserInfo(userId:String){
        viewModelScope.launch {
            try {
                val userProfile = userRepository.getUserPublicProfile(userId)
                _state.value = _state.value.copy(
                    profile = userProfile.profile,
                    is_follow = userProfile.profile?.is_following,
                    can_follow = userProfile.profile?.can_follow,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }
    fun getCharacterList(userId:String){
        viewModelScope.launch {
            try {
                val userProfile = characterRepository.getPublicCharacters(1,100,userId)
                _state.value = _state.value.copy(
                    items = userProfile?.characters,
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun setFollowUser( ) {
        viewModelScope.launch {
            // 执行搜索
            try {
                val followState: Boolean = _state.value.is_follow == true
                val target_user_id :String? = _state.value.profile?.id

                if (followState){
                     followerRepository.unFollowUser(target_user_id)
                }else{
                     followerRepository.followUser(target_user_id)
                }
                _state.value = _state.value.copy(
                    is_follow = !followState,
                    errorMessage = null
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(
                    errorMessage = e.message ?: "关注状态修改失败"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = "网络错误: ${e.message}"
                )
            }
        }
    }
}
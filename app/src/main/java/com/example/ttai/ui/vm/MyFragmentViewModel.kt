package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.bean.UpdateProfileRequest
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.MyFragmentIntent
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.network.repository.AuthRepository
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.MyFragmentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyFragmentViewModel(private val context: Context) : MviViewModel<MyFragmentIntent, MyFragmentState>() {
    private val _state = MutableStateFlow(MyFragmentState())
    override val state: StateFlow<MyFragmentState> = _state.asStateFlow()
    private val repository = NetworkModule.provideMyFragmentRepository(context)
    private val characterRepository: CharacterRepository = NetworkModule.provideCharacterRepository(context)
    private val authRepository: AuthRepository = NetworkModule.provideAuthRepository(context)

    override fun processIntent(intent: MyFragmentIntent) {
        viewModelScope.launch {
            when (intent) {
                is MyFragmentIntent.Initialize -> {
                    _state.value = _state.value.copy(isLoading = true)
                    initialize()
                }
                is MyFragmentIntent.UpdateUserName -> {
                    _state.value = _state.value.copy(
                        userName = intent.newName,
                        error = null
                    )
                }
                is MyFragmentIntent.UpdateAvatar -> {
                    updateAvatar(intent.avatarUrl)
                }
                is MyFragmentIntent.getCurrencyBalance -> {
                    getCurrencyBalance()
                }
                is MyFragmentIntent.LoadMyAICharacters -> {
                    loadMyAICharacters()
                }
                is MyFragmentIntent.DeleteMyAICharacter -> {
                    intent.characterId?.let { deleteMyAICharacter(it) }
                }
                is MyFragmentIntent.RefreshData -> {
                    refreshMyAICharacters()
                }
                is MyFragmentIntent.LoadMoreData -> {
                    loadMoreMyAICharacters()
                }
                is MyFragmentIntent.pushPin -> {
                    pushAICharactersPin(intent.characterId)
                }
                is MyFragmentIntent.pushUnPin -> {
                    pushAICharactersUnPin(intent.characterId)
                }
            }
        }
    }

    /**
     * 初始化信息
     */
    private suspend fun initialize() {
        android.util.Log.d("MyFragmentViewModel", "开始初始化...")
        try {
            val response = repository.initialize()
            _state.value = _state.value.copy(
                isLoading = false,
                userName = response.profile?.username,
                id = response.profile?.id,
                ivHeard = response.profile?.avatarUrl,
                brief_intro = response.profile?.bio,
                fairyJade = response.profile?.fairyJade,
                fairyShells = response.profile?.fairyShells,
                membershipInfo = response.profile?.membershipInfo,
                followersCount = response.profile?.stats?.followersCount,
                followingCount = response.profile?.stats?.followingCount,
                is_beta_user = response.profile?.is_beta_user == true,
                invitationInfo = response.profile?.invitationInfo ,
                error = null
            )
            
            android.util.Log.d("MyFragmentViewModel", "状态已更新: userName=${_state.value.userName}, id=${_state.value.id}")
        } catch (e: Exception) {
            android.util.Log.e("MyFragmentViewModel", "初始化失败: ${e.message}", e)
            _state.value = _state.value.copy(
                isLoading = false,
                error = "初始化失败"
            )
        }
    }
    /**
     * 获取钱包信息
     */
    private suspend fun getWallet() {
        try {
            val response = repository.getWallet()
            _state.value = _state.value.copy(
                fairyJade = response.wallet.fairyJade,
                fairyShells = response.wallet.fairyShells,
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "获取钱包信息失败: ${e.message}"
            )
        }
    }
    
    /**
     * 获取货币余额
     */
    private suspend fun getCurrencyBalance() {
        try {
            val response = repository.getCurrencyBalance()
            _state.value = _state.value.copy(
                fairyJade = response.fairyJade,
                fairyShells = response.fairyShells,
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "获取货币余额失败"
            )
        }
    }
    
    /**
     * 加载我的AI角色列表
     */
    private suspend fun loadMyAICharacters() {
        try {
            _state.value = _state.value.copy(isLoadingMyAI = true, error = null)
            
            val response = characterRepository.getMyCharacters(page = 1, limit = 100)
            
            android.util.Log.d("MyFragmentViewModel", "加载我的AI角色: page=${response.page}, total=${response.total}, totalPages=${response.totalPages}, characters.size=${response.characters.size}")
            
            _state.value = _state.value.copy(
                myCharacters = response.characters,
                isLoadingMyAI = false,
                currentPage = response.page,
                hasMoreData = response.page < response.totalPages, // 使用API返回的分页信息
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoadingMyAI = false,
                error = "加载我的AI角色失败"
            )
        }
    }
    
    /**
     * 刷新我的AI角色列表
     */
    private suspend fun refreshMyAICharacters() {
        try {
            _state.value = _state.value.copy(isRefreshing = true, error = null)
            
            val response = characterRepository.getMyCharacters(page = 1, limit = 20)
            
            _state.value = _state.value.copy(
                myCharacters = response.characters,
                isRefreshing = false,
                currentPage = response.page,
                hasMoreData = response.page < response.totalPages, // 使用API返回的分页信息
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isRefreshing = false,
                error = "刷新我的AI角色失败"
            )
        }
    }

    /**
     * 加载更多我的AI角色
     */
    private suspend fun loadMoreMyAICharacters() {
        try {
            val currentState = _state.value
            if (!currentState.hasMoreData || currentState.isLoadingMore) {
                return
            }

            _state.value = _state.value.copy(isLoadingMore = true, error = null)

            // 调用分页API获取下一页数据
            val nextPage = currentState.currentPage + 1
            val response = characterRepository.getMyCharacters(page = nextPage, limit = 20)

            android.util.Log.d("MyFragmentViewModel", "加载更多AI角色: page=${response.page}, total=${response.total}, totalPages=${response.totalPages}, characters.size=${response.characters.size}")
            android.util.Log.d("MyFragmentViewModel", "当前已有角色数量: ${currentState.myCharacters.size}")

            // 合并新数据，避免重复
            val existingIds = currentState.myCharacters.map { it.id }.toSet()
            val newCharacters = response.characters.filter { !existingIds.contains(it.id) }
            val updatedCharacters = currentState.myCharacters + newCharacters

            android.util.Log.d("MyFragmentViewModel", "过滤后新角色数量: ${newCharacters.size}, 更新后总数量: ${updatedCharacters.size}")

            _state.value = _state.value.copy(
                myCharacters = updatedCharacters,
                isLoadingMore = false,
                currentPage = response.page,
                hasMoreData = response.page < response.totalPages, // 使用API返回的分页信息
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoadingMore = false,
                error = "加载更多AI角色失败"
            )
        }
    }

    /**
     * 设置置顶
     */
    private suspend fun pushAICharactersPin(characterId: String?) {
        try {
            _state.value = _state.value.copy(isLoadingMore = true, error = null)
            // 调用分页API获取下一页数据
            characterRepository.pushAiPin(characterId)
            loadMyAICharacters()
            _state.value = _state.value.copy(
                isLoadingMyAI = false,
                isLoadingMore = false,
                error = "角色置顶成功"
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoadingMore = false,
                error = "角色置顶失败"
            )
        }
    } /**
     * 设置置顶
     */
    private suspend fun pushAICharactersUnPin(characterId: String?) {
        try {
            _state.value = _state.value.copy(isLoadingMore = true, error = null)
            // 调用分页API获取下一页数据
            characterRepository.pushAiUnPin(characterId )
            loadMyAICharacters()
            _state.value = _state.value.copy(
                isLoadingMyAI = false,
                isLoadingMore = false,
                error = "取消置顶成功"
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoadingMore = false,
                error = "取消置顶失败"
            )
        }
    }

    /**
     * 更新用户头像
     */
    private suspend fun updateAvatar(avatarUrl: String) {
        try {
            android.util.Log.d("MyFragmentViewModel", "开始更新头像: $avatarUrl")
            
            // 构建请求参数
            val request = UpdateProfileRequest(
                avatarUrl = avatarUrl
            )
            
            // 调用API更新用户资料
             authRepository.updateUserProfile(request)
            
            // 更新本地状态
            _state.value = _state.value.copy(
                ivHeard = avatarUrl,
                error = null
            )
            
            android.util.Log.d("MyFragmentViewModel", "头像更新成功: $avatarUrl")
        } catch (e: Exception) {
            android.util.Log.e("MyFragmentViewModel", "头像更新失败: ${e.message}", e)
            _state.value = _state.value.copy(
                error = "头像更新失败"
            )
        }
    }
    
    /**
     * 删除我的AI角色
     */
    private suspend fun deleteMyAICharacter(characterId: String) {
        try {
            _state.value = _state.value.copy(isLoadingMyAI = true, error = null)
            
            // 调用API删除AI角色
            characterRepository.deleteCharacter(characterId)
            
            // 重新加载AI角色列表
            loadMyAICharacters()
            
            _state.value = _state.value.copy(
                isLoadingMyAI = false,
                error = null
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoadingMyAI = false,
                error = "删除AI角色失败"
            )
        }
    }
}
package com.example.ttai.ui.vm

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.bean.CollectItem
import com.example.ttai.intent.BranchManagerIntent
import com.example.ttai.intent.ChatModelListIntent
import com.example.ttai.intent.CollectIntent
import com.example.ttai.intent.RequestModelIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.network.repository.ChatRepository
import com.example.ttai.network.repository.UserRepository
import com.example.ttai.state.BranchManagerState
import com.example.ttai.state.ChatModelListState
import com.example.ttai.state.RequestModelState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Arrays

class BranchManagerViewModel(context: Context
) : MviViewModel<BranchManagerIntent, BranchManagerState>() {
    private val _state = MutableStateFlow(BranchManagerState())
    override val state: StateFlow<BranchManagerState> = _state.asStateFlow()

    private val repository = ChatRepository(context)

    override fun processIntent(intent: BranchManagerIntent) {
        viewModelScope.launch {
            when (intent) {
                is BranchManagerIntent.Initialize -> {
                    try {
                       var response = repository.getBranchs(intent.character_id)
                        val updatedPriceList = response.branches
                        Log.w("YXTEST", "YXTEST  conversation_id: ${response.main_branch.conversation_id}")

                        _state.value = _state.value.copy(
                            isLoading = false,
                            conversation_id = response?.main_branch?.conversation_id,
                            modes = updatedPriceList
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "获取聊天模型: ${e.message}"
                        )
                    }
                }

                is BranchManagerIntent.UpdateSelectedModel -> {
                    _state.value = _state.value.copy(isLoading = true, error = null,initList = false)
                    try {
                        // 调用更新AI模型偏好API
                      var response=  repository.branchSwitchBranch(intent.character_id,intent.model.branch_id)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            selectedModelSuccess = true,
                           // conversation_id = intent.model.conversation_id,
                            character_id = response.character_id,
                            initList = true
                          )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "x选择模型失败: ${e.message}"
                        )
                    }
                }

                is BranchManagerIntent.CreateBranch -> {
                    _state.value = _state.value.copy(isLoading = true, error = null,initList = false)
                    try {
                        // 调用更新AI模型偏好API
                        repository.branchCreateBranch(intent.character_id,"","","")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            initList = true,
                            selectedModelSuccess = false,
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "x选择模型失败: ${e.message}"
                        )
                    }
                }
                is BranchManagerIntent.pushPin -> {
                    pushAICharactersPin(intent.branch_id)
                }
                is BranchManagerIntent.pushUnPin -> {
                    pushAICharactersUnPin(intent.branch_id)
                }
                is BranchManagerIntent.delete -> {
                    _state.value = _state.value.copy(isLoading = true, error = null,initList = false)
                    try {
                        // 调用更新AI模型偏好API
                        repository.branchDeleteBranch(intent.branch_id)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            selectedModelSuccess = false,
                            initList = true
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "删除分支失败: ${e.message}"
                        )
                    }
                }
                is BranchManagerIntent.editeName -> {
                    _state.value = _state.value.copy(isLoading = true, error = null,initList = false)
                    try {
                        // 调用更新AI模型偏好API
                        repository.branchEditeBranchName(intent.branch_id,intent.editeName)
                        _state.value = _state.value.copy(
                            isLoading = false,
                            selectedModelSuccess = false,
                            initList = true
                        )
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "删除分支失败: ${e.message}"
                        )
                    }
                }
            }
        }
    }
    /**
     * 设置置顶
     */
    private suspend fun pushAICharactersPin(branch_id: String?) {
        try {
            _state.value = _state.value.copy( error = null, initList = false)
            // 调用分页API获取下一页数据
            repository.branchPinBranch(branch_id)
            _state.value = _state.value.copy(
                error = "分支置顶成功",
                initList = true
            )

        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "分支置顶失败"
            )
        }
    } /**
     * 设置置顶
     */
    private suspend fun pushAICharactersUnPin(branch_id: String?) {
        try {
            _state.value = _state.value.copy( error = null, initList = false)
            // 调用分页API获取下一页数据
            repository.branchUnpinBranch(branch_id )
            _state.value = _state.value.copy(
                error = "取消置顶成功", initList = true
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                error = "取消置顶失败"
            )
        }
    }
}
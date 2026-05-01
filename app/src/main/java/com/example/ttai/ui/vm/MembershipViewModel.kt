package com.example.ttai.ui.vm

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.MembershipIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.MembershipState
import com.example.ttai.bean.MembershipItem
import com.example.ttai.utils.CommontUtils
import com.example.ttai.utils.MMKVUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MembershipViewModel(
    private val context: Context
) : MviViewModel<MembershipIntent, MembershipState>() {
    
    private val membershipRepository = NetworkModule.provideMembershipRepository(context)
    private val authRepository = NetworkModule.provideAuthRepository(context)
    private val userRepository = NetworkModule.provideUserRepository(context)
    private val _state = MutableStateFlow(MembershipState())
    override val state: StateFlow<MembershipState> = _state.asStateFlow()
    
    override fun processIntent(intent: MembershipIntent) {
        when (intent) {
            is MembershipIntent.Initialize -> {
                initializeMembership()
            }
            is MembershipIntent.LoadUserProfile -> {
                loadUserProfile()
            }
            is MembershipIntent.Subscribe -> {
                subscribeToMembership(intent.planType, intent.planName)
            }
            is MembershipIntent.SelectModel1Item -> {
                selectModel1Item(intent.itemId)
            }
            is MembershipIntent.SelectModel2Item -> {
                selectModel2Item(intent.itemId)
            }
            is MembershipIntent.SelectItem -> {
                selectItem(intent.itemId, intent.modelType)
            }
        }
    }
    
    /**
     * 初始化会员页面
     */
    private fun initializeMembership() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val packagesData = membershipRepository.getMembershipPackages()
                
                // 添加调试日志
                android.util.Log.d("MembershipViewModel", "API响应成功，数据: $packagesData")
                android.util.Log.d("MembershipViewModel", "套餐组数量: ${packagesData.size}")
                
                // 将API数据转换为MembershipItem格式
                val model1Items = mutableListOf<MembershipItem>()
                val model2Items = mutableListOf<MembershipItem>()
                var model1Title :String = ""
                var model2Title :String = ""

                packagesData.forEachIndexed { index, packageGroup ->
                    android.util.Log.d(
                        "MembershipViewModel",
                        "处理套餐组: ${packageGroup.packageName}"
                    )
                    if (index == 0) {
                        model1Title = packageGroup.packageName
                        val items = packageGroup.packages.map { pkg ->
                            MembershipItem(
                                id = pkg.id,
                                vipName = pkg.name,
                                value = pkg.price.toString(),
                                tip = "(${pkg.description})",
                                isSelected = false
                            )
                        }
                        model1Items.addAll(items)
                        android.util.Log.d("MembershipViewModel", "小慧套餐项目数量: ${items.size}")
                    } else {
                        model2Title = packageGroup.packageName
                        val items = packageGroup.packages.map { pkg ->
                            MembershipItem(
                                id = pkg.id,
                                vipName = pkg.name,
                                value = pkg.price.toString(),
                                tip = "(${pkg.description})",
                                isSelected = false
                            )
                        }
                        model2Items.addAll(items)
                        android.util.Log.d("MembershipViewModel", "星河套餐项目数量: ${items.size}")
                    }
                }
                
                android.util.Log.d("MembershipViewModel", "最终model1Items数量: ${model1Items.size}")
                android.util.Log.d("MembershipViewModel", "最终model2Items数量: ${model2Items.size}")
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = null,
                    model1Items = model1Items,
                    model2Items = model2Items,
                    model1Title = model1Title,
                    model2Title = model2Title,
                    selectedItemId = "", // 初始化时没有选中的项目
                    selectedModelType = "",
                    selectedModel1Item = "",
                    selectedModel2Item = ""
                )
            } catch (e: Exception) {
                android.util.Log.e("MembershipViewModel", "初始化失败", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "初始化失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 加载用户资料
     */
    private fun loadUserProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingUserProfile = true, error = null)
            try {
                val response = userRepository.getUserProfile()
                val userProfile = response.profile
                MMKVUtils.putUserProfile(userProfile)

                // 格式化会员等级显示
                val vipTag = userProfile?.membershipInfo?.name
                
                // 格式化到期时间
                val expireTime = CommontUtils.getExpiryTime(userProfile?.membershipInfo?.expiry)
                
                _state.value = _state.value.copy(
                    isLoadingUserProfile = false,
                    userProfile = userProfile,
                    membershipInfo = userProfile?.membershipInfo,
                    userName = userProfile?.username,
                    vipTag = vipTag,
                    expireTime = expireTime,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingUserProfile = false,
                    error = "获取用户资料失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 开通会员
     */
    private fun subscribeToMembership(planType: String, planName: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                error = null
            )
            try {
                // 调用升级会员API
                membershipRepository.upgradePremium(planType )
                // 升级成功，重新获取用户资料
                loadUserProfile()
                _state.value = _state.value.copy(
                    isLoading = false,
                    isSubscriptionSuccessful = true,
                    selectedPlan = planName
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "开通失败: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 选择模型1项目
     */
    private fun selectModel1Item(itemId: String) {
        selectItem(itemId, "model1")
    }
    
    /**
     * 选择模型2项目
     */
    private fun selectModel2Item(itemId: String) {
        selectItem(itemId, "model2")
    }
    
    /**
     * 全局选择项目 - 确保同一时刻只有一个item被选中
     */
    private fun selectItem(itemId: String, modelType: String) {
        val currentState = _state.value
        
        // 更新模型1的选中状态
        val updatedModel1Items = currentState.model1Items.map { item ->
            item.copy(isSelected = modelType == "model1" && item.id == itemId)
        }
        
        // 更新模型2的选中状态
        val updatedModel2Items = currentState.model2Items.map { item ->
            item.copy(isSelected = modelType == "model2" && item.id == itemId)
        }
        
        // 更新状态
        _state.value = currentState.copy(
            model1Items = updatedModel1Items,
            model2Items = updatedModel2Items,
            selectedItemId = itemId,
            selectedModelType = modelType,
            selectedModel1Item = if (modelType == "model1") itemId else "",
            selectedModel2Item = if (modelType == "model2") itemId else ""
        )
    }
} 
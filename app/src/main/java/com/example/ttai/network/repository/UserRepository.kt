package com.example.ttai.network.repository

import com.example.ttai.bean.MemoryBookRequest
import com.example.ttai.bean.MemoryBookResponse
import com.example.ttai.bean.OnboardingStatusData
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.UpdateAiModelRequest
import com.example.ttai.bean.UpdateAiModelResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException

/**
 * 用户相关API调用仓库
 */
class UserRepository(
    private val apiService: ApiService,
    private val context: android.content.Context
) {

    /**
     * 检查用户引导状态
     * @return OnboardingStatusData 用户引导状态数据
     * @throws ApiException 当API调用失败时抛出
     */
    suspend fun checkOnboardingStatus(): OnboardingStatusData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "检查用户引导状态",
            apiCall = { apiService.checkOnboardingStatus() }
        )
    }
    /**
     * 获取用户信息
     * @return ProfileData 用户信息
     * @throws ApiException 当API调用失败时抛出
     */
    suspend fun getUserProfile(): ProfileData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取用户信息",
            apiCall = {  apiService.getUserProfile()}
        )
    }
    /**
     * 获取用户信息
     * @return ProfileData 用户信息
     * @throws ApiException 当API调用失败时抛出
     */
    suspend fun getUserPublicProfile(userId : String =""): ProfileData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取用户信息",
            apiCall = {  apiService.getUserPublicProfile(userId)}
        )
    }

    /**
     * 更新AI模型偏好
     * @param preferredAiModel 偏好的AI模型名称
     * @return UpdateAiModelResponse 更新后的AI模型偏好和用户余额信息
     * @throws ApiException 当API调用失败时抛出
     */
    suspend fun updateAiModelPreference(preferredAiModel: String): UpdateAiModelResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "更新AI模型偏好",
            apiCall = {
                apiService.updateAiModelPreference(
                    UpdateAiModelRequest(preferredAiModel = preferredAiModel)
                )
            }
        )
    }
    /**
     * /api/memory-book/get/{character_id}
     */
    suspend fun getMemoryBook(character_id: String): MemoryBookResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取当前用户对指定角色的记忆簿记录",
            apiCall = {
                apiService.getMemoryBook(character_id)
            }
        )
    }
    /**
     * /api/memory-book/get/{character_id}
     */
    suspend fun postMemoryBook(character_id: String,content: String): MemoryBookResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "更新当前用户对指定角色的记忆簿记录",
            apiCall = {
                apiService.postMemoryBook(MemoryBookRequest(character_id = character_id,content = content))
            }
        )
    }
}

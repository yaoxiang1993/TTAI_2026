package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.AddChatSlotRequest
import com.example.ttai.bean.AddChatSlotResponse
import com.example.ttai.bean.BuyExtraSlotData
import com.example.ttai.bean.CharacterDetailData
import com.example.ttai.bean.CharacterListResponse
import com.example.ttai.bean.CharacterSearchRequest
import com.example.ttai.bean.CharacterSearchResponse
import com.example.ttai.bean.ComprehensiveCharactersResponse
import com.example.ttai.bean.ConversationSettingsRequest
import com.example.ttai.bean.ConversationSettingsResponse
import com.example.ttai.bean.CreateCharacterRequest
import com.example.ttai.bean.FeaturedCharactersResponse
import com.example.ttai.bean.GetCharactersRequest
import com.example.ttai.bean.MyAIListResponse
import com.example.ttai.bean.PinRequest
import com.example.ttai.bean.PinResponse
import com.example.ttai.bean.PurchaseMemoryRequest
import com.example.ttai.bean.RemoveChatSlotRequest
import com.example.ttai.bean.RemoveChatSlotResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import retrofit2.http.Query

/**
 * 角色管理Repository
 * 处理角色相关的API调用
 */
class CharacterRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    
    /**
     * 获取角色列表
     */
    suspend fun getCharacters(
        personalityTag: ArrayList<String>? = null,
        page: Int = 1,
        limit: Int = 20
    ): CharacterListResponse? {
        val request = GetCharactersRequest(
            page = page,
            limit = limit
        )
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取角色列表",
            apiCall = { apiService.getCharacters(request) }
        )
    }
    
    /**
     * 获取角色详情
     */
    suspend fun getCharacterDetail(characterId: String): CharacterDetailData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取角色详情",
            apiCall = { apiService.getCharacterDetail(characterId) }
        )
    }

    /**
     * 创建自定义角色
     */
    suspend fun createCharacter(request: CreateCharacterRequest): Any {
        val response = apiService.createCharacter(request)
        if (response.code == 200) {
            return response.data ?: "创建成功"
        } else {
            throw ApiException(response.code ,response.message)
        }
    }

    /**
     * 获取我创建的角色
     */
    suspend fun getMyCharacters(page: Int = 1, limit: Int = 100): MyAIListResponse {
        val response = apiService.getMyCharacters(page, limit)
        if (response.code == 200) {
            return response.data
                ?: throw ApiException(response.code ,response.message)
        } else {
            throw ApiException(response.code ,response.message)
        }
    }
    
    /**
     * 获取精选角色列表
     */
    suspend fun getFeaturedCharacters(
        page: Int = 1,
        limit: Int = 20,
        is_unlimited: Boolean = false
    ): FeaturedCharactersResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取精选角色列表",
            apiCall = { apiService.getFeaturedCharacters(page, limit,is_unlimited) }
        )
    }
    
    /**
     * 获取综合角色列表
     */
    suspend fun getComprehensiveCharacters(
        page: Int = 1,
        limit: Int = 20,
        sortBy: String = "",
        sortOrder: String = "",
        is_unlimited : Boolean = false
    ): ComprehensiveCharactersResponse {

        // 只构建有值的参数
        val params = mutableMapOf<String, String>()

        params["page"] = page.toString()
        params["limit"] = limit.toString()
        // 只有当 sortBy 有有效值时才加入
        sortBy.takeIf { it.isNotBlank() }?.let {
            params["sort_by"] = it
        }
        // 只有当 sortOrder 有有效值时才加入
        sortOrder.takeIf { it.isNotBlank() }?.let {
            params["sort_order"] = it
        }
        // Boolean 类型需要转成字符串，API 通常接受 "true"/"false"
        params["include_unlimited"] = is_unlimited.toString()

        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取综合角色列表",
            apiCall = { apiService.getComprehensiveCharacters(params) }
        )
    }

    /**
     * 获取角色列表
     */
    suspend fun getPublicCharacters(
        page: Int = 1,
        limit: Int = 100,
        userId: String = ""
    ): CharacterListResponse? {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取角色列表",
            apiCall = { apiService.getPublicCharacters(userId,page, limit) }
        )
    }
    
    /**
     * 购买永久记忆功能
     */
    suspend fun purchasePermanentMemory(request: PurchaseMemoryRequest): Any {
        val response = apiService.purchasePermanentMemory(request)
        if (response.code == 200) {
            return response.data ?: "购买成功"
        } else {
            throw ApiException(response.code ,response.message)
        }
    }
    
    /**
     * 购买额外角色槽位
     */
    suspend fun buyExtraSlot(): BuyExtraSlotData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "购买额外角色槽位",
            apiCall = { apiService.buyExtraSlot() }
        )
    }
    
    /**
     * 删除角色
     */
    suspend fun deleteCharacter(characterId: String): Any {
        val response = apiService.deleteCharacter(characterId)
        if (response.code == 200) {
            return response.data ?: "删除成功"
        } else {
            throw ApiException(response.code ,response.message)
        }
    }
    
    /**
     * 添加角色到聊天槽位
     */
    suspend fun addChatSlot(characterId: String): AddChatSlotResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "添加角色到聊天槽位",
            apiCall = { apiService.addChatSlot(AddChatSlotRequest(characterId)) }
        )
    }
    
    /**
     * 从聊天槽位移除角色
     */
    suspend fun removeChatSlot(characterId: String): RemoveChatSlotResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "从聊天槽位移除角色",
            apiCall = { apiService.removeChatSlot(RemoveChatSlotRequest(characterId)) }
        )
    }
    
    /**
     * 搜索角色
     */
    suspend fun searchCharacters(
        keyword: String,
        page: Int = 1,
        limit: Int = 20
    ): CharacterSearchResponse {
        val request = CharacterSearchRequest(
            keyword = keyword,
            page = page,
            limit = limit
        )
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "搜索角色",
            apiCall = { apiService.searchCharacters(request) },
            true
        )
    }


    /**
     * 获取我创建的角色
     */
    suspend fun pushAiPin(characterId: String? ): PinResponse {
        val response = apiService.pushAiPin( PinRequest(characterId))
        if (response.code == 200) {
            return response.data
                ?: throw ApiException(response.code ,response.message)
        } else {
            throw ApiException(response.code ,response.message)
        }
    }
    /**
     * 获取我创建的角色
     */
    suspend fun pushAiUnPin(characterId: String? ): PinResponse {
        val response = apiService.pushAiUnPin( PinRequest(characterId))
        if (response.code == 200) {
            return response.data
                ?: throw ApiException(response.code ,response.message)
        } else {
            throw ApiException(response.code ,response.message)
        }
    }
    /**
     * 获取对话设定
     */
    suspend fun getConversationSettings(
        character_id: String?
    ): ConversationSettingsResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "搜索角色",
            apiCall = { apiService.getConversationSettings(character_id) }
        )
    }

    /**
     * 修改对话设定
     */
    suspend fun updateConversationSettings(
        character_id: String?,
        nickname: String?,
        gender: String?,
        identity: String?,
        personality_description: String?,
    ): ConversationSettingsResponse {
        val request = ConversationSettingsRequest(
            character_id = character_id,
            nickname = nickname,
            gender = gender,
            identity = identity,
            personality_description = personality_description,
        )
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "修改对话设定",
            apiCall = { apiService.updateConversationSettings(request) }
        )
    }




} 
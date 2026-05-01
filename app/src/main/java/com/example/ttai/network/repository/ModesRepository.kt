package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.ChatModelsResponse
import com.example.ttai.bean.DeallocatePermanentMemoryRequest
import com.example.ttai.bean.FavoriteRequest
import com.example.ttai.bean.FavoriteResponse
import com.example.ttai.bean.ModelsItemsResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.ui.fragment.ControlFragment

class ModesRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    suspend fun getDeviceControlModes(page: Int = 1, limit: Int = 50): ModelsItemsResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取对话列表",
            apiCall = { apiService.getModes() }
        )
    }
    suspend fun postFavorite(modeId: String  ): FavoriteResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "添加收藏",
            apiCall = {
                val request = FavoriteRequest(modeId)
                apiService.favorite(request) }
        )
    }

    suspend fun postUnfavorite(modeId: String): FavoriteResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "添加收藏",
            apiCall = {
                val request = FavoriteRequest(modeId)
                apiService.unfavorite(request) }
        )
    }

    suspend fun getChatModels( ): ChatModelsResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取聊天模型",
            apiCall = {
                apiService.getChatModels( ) }
        )
    }

}
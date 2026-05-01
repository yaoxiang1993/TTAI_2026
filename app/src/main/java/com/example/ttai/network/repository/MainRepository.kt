package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.ConversationsData
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.NetworkModule

class MainRepository(private val context: Context) {
    private val apiService: ApiService = NetworkModule.createService()

    suspend fun getConversations(page: Int = 1, limit: Int = 20): ConversationsData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取对话列表",
            apiCall = { apiService.getConversations() }
        )
    }

}
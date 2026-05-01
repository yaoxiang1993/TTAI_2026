package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.TagsData
import com.example.ttai.bean.UpdatePreferredTagsRequest
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService

/**
 * 标签选择相关网络请求仓库
 */
class SelectTagRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * 获取性格标签列表
     * @return 性格标签响应
     */
    suspend fun getPersonalityTags(): TagsData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取性格标签",
            apiCall = { apiService.getPersonalityTags() }
        )
    }

    /**
     * 更新用户偏好标签
     * @param tags 标签列表
     */
    suspend fun updatePreferredTags(tags: List<String>) {
        val request = UpdatePreferredTagsRequest(tags = tags)
        ApiHelper.executeApiRequest(
            context = context,
            requestName = "更新偏好标签",
            apiCall = { apiService.updatePreferredTags(request) }
        )
    }
}

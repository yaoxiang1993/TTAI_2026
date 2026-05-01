package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.FollowRequest
import com.example.ttai.bean.FollowResponse
import com.example.ttai.bean.FollowsResponse
import com.example.ttai.bean.MarkReadRequest
import com.example.ttai.bean.MarkReadResponse
import com.example.ttai.bean.NotificationsResponse
import com.example.ttai.bean.OnboardingStatusData
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.UnreadCountResponse
import com.example.ttai.bean.UpdateAiModelRequest
import com.example.ttai.bean.UpdateAiModelResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import retrofit2.http.Query

/**
 * 用户相关API调用仓库
 */
class NotifyRepository(
    private val apiService: ApiService,
    private val context: Context
) {


    /**
     **接口地址**: `GET /notifications/list`

     **功能描述**: 获取用户的消息列表，支持分页和筛选

     */
    suspend fun notificationsList(page: Int? = 1,limit: Int? = 20): NotificationsResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取用户的消息列表",
            apiCall = { apiService.getNotificationsList(page,limit ) }
        )
    }
    /**
     **获取未读消息数量
     */
    suspend fun notificationsUnreadCount(): UnreadCountResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取未读消息数量",
            apiCall = { apiService.notificationsUnreadCount() }
        )
    }
    /**
     * 设置为已读
     */
    suspend fun notificationsMarkRead(notificationId:String?): MarkReadResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "设置为已读",
            apiCall = { apiService.notificationsMarkRead(MarkReadRequest(notificationId)) }
        )
    }
}

package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.FollowRequest
import com.example.ttai.bean.FollowResponse
import com.example.ttai.bean.FollowsResponse
import com.example.ttai.bean.OnboardingStatusData
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.UpdateAiModelRequest
import com.example.ttai.bean.UpdateAiModelResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import retrofit2.http.Query

/**
 * 用户相关API调用仓库
 */
class FollowerRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * 关注指定用户
     */
    suspend fun followUser(target_user_id :String?): FollowResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "关注指定用户",
            apiCall = { apiService.follow(FollowRequest(target_user_id = target_user_id)) },
            showMessage = true
        )
    }
    /**
     * 取消关注指定用户
     */
    suspend fun unFollowUser(target_user_id :String?): FollowResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "取消关注指定用户",
            apiCall = { apiService.unFollow(FollowRequest(target_user_id = target_user_id))},
            showMessage = true
        )
    }

    /**
     * 接口地址: GET /api/user/followers
     * 功能描述: 获取当前用户的粉丝列表
     *
     */
    suspend fun followers(keyword:String,page: Int = 1,limit: Int = 20): FollowsResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取当前用户的粉丝列表",
            apiCall = { apiService.getFollowers(keyword,page,limit ) }
        )
    }

    /**
     * 3.4 获取关注列表接口
     * 接口地址: GET /api/user/following
     * 功能描述: 获取当前用户的关注列表
     * 请求头:
     *
     *
     */
    suspend fun following(keyword:String,page: Int = 1,limit: Int = 20): FollowsResponse {
        val response = ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取当前用户的关注列表",
            apiCall = { apiService.getFollowing(keyword, page, limit) }
        )
        return response

    }


}

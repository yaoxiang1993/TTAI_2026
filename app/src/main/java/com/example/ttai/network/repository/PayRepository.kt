package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.FollowRequest
import com.example.ttai.bean.FollowResponse
import com.example.ttai.bean.FollowsResponse
import com.example.ttai.bean.OnboardingStatusData
import com.example.ttai.bean.PayHistoryItem
import com.example.ttai.bean.PayHistoryResponse
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.RollbackMessageResponse
import com.example.ttai.bean.UpdateAiModelRequest
import com.example.ttai.bean.UpdateAiModelResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import retrofit2.http.Query

/**
 * 用户相关API调用仓库
 */
class PayRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * 接口地址: GET /api/bills/list
    功能描述: 获取用户的账单明细列表，支持分页和筛选
     *
     *
     */
    suspend fun getPayHistory(page: Int = 1,limit: Int = 100,year:Int?,month:Int?,type:String?=""): PayHistoryResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取当前用户的关注列表",
            apiCall = { apiService.getPayHistory(page,limit,year,month,type) }
        )
    }
}

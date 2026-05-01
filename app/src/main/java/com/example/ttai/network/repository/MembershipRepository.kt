package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.MembershipPackageGroup
import com.example.ttai.bean.UpgradePremiumRequest
import com.example.ttai.bean.UpgradePremiumResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService

/**
 * 会员相关网络请求仓库
 */
class MembershipRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * 获取会员套餐列表
     * @return 会员套餐响应
     */
    suspend fun getMembershipPackages(): List<MembershipPackageGroup> {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取会员套餐列表",
            apiCall = { apiService.getMembershipPackages() }
        )
    }

    /**
     * 升级会员
     * @param planType 套餐类型
     * @return 升级响应
     */
    suspend fun upgradePremium(planType: String ): UpgradePremiumResponse {
        val request = UpgradePremiumRequest(planType)
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "升级会员",
            apiCall = { apiService.upgradePremium(request) }
        )
    }
}

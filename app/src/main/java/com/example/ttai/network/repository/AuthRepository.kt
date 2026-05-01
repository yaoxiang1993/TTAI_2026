package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.AuthData
import com.example.ttai.bean.OnboardingStatusData
import com.example.ttai.bean.OneClickAuthRequest
import com.example.ttai.bean.SendCodeData
import com.example.ttai.bean.SendCodeRequest
import com.example.ttai.bean.UpdateProfileRequest
import com.example.ttai.bean.VerifyCodeAuthRequest
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.utils.DebugUtils

class AuthRepository(
    private val apiService: ApiService,
    private val context: Context
) {
    /**
     * 一键登录注册
     */
    suspend fun oneClickAuth(phone: String, deviceId: String): AuthData {
        DebugUtils.logApiCall("一键登录注册", mapOf("phone" to phone, "deviceId" to deviceId))
        val request = OneClickAuthRequest(phone, deviceId)
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "一键登录注册",
            apiCall = { apiService.oneClickAuth(request) }
        )
    }

    /**
     * 发送验证码
     */
    suspend fun sendCode(phone: String): SendCodeData {
        DebugUtils.logApiCall("发送验证码", mapOf("phone" to phone))
        val request = SendCodeRequest(phone)
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "发送验证码",
            apiCall = { apiService.sendCode(request) }
        )
    }

    /**
     * 验证码登录注册
     */
    suspend fun verifyCodeAuth(phone: String, code: String, invitationCode: String, deviceId: String? = null): AuthData {
        DebugUtils.logApiCall("验证码登录注册", mapOf("phone" to phone, "code" to code, "deviceId" to (deviceId ?: "null")))
        val request = VerifyCodeAuthRequest(phone, code, deviceId,invitationCode)
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "验证码登录注册",
            apiCall = { apiService.verifyCodeAuth(request) }
        )
    }

    /**
     * 更新用户资料
     */
    suspend fun updateUserProfile(request: UpdateProfileRequest): Any {
        DebugUtils.logApiCall("更新用户资料", mapOf("username" to (request.username ?: "null")))
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "更新用户资料",
            apiCall = { apiService.updateUserProfile(request) }
        )
    }
    /**
     * 检查用户引导状态
     */
    suspend fun checkOnboardingStatus(): OnboardingStatusData? {
        DebugUtils.logApiCall("检查用户引导状态")
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "检查用户引导状态",
            apiCall = { apiService.checkOnboardingStatus() }
        )
    }


}
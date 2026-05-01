package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.CurrencyBalanceData
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.WalletData
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.utils.MMKVUtils

class MyFragmentRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    suspend fun initialize(): ProfileData {
        val profileData = ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取用户资料",
            apiCall = { apiService.getUserProfile()}
        )
        // 存储用户资料到MMKV
        MMKVUtils.putUserProfile(profileData.profile)
        return profileData
    }
    
    suspend fun getWallet(): WalletData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取钱包信息",
            apiCall = { apiService.getWallet() }
        )
    }
    
    suspend fun getCurrencyBalance(): CurrencyBalanceData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取货币余额",
            apiCall = { apiService.getCurrencyBalance() }
        )
    }
}
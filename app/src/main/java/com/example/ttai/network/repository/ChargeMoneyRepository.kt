package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.RechargeCurrencyRequest
import com.example.ttai.bean.RechargeCurrencyResponse
import com.example.ttai.bean.RechargePackagesData
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService

class ChargeMoneyRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    suspend fun rechargeCurrency(amount: Int): RechargeCurrencyResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "支付订单创建成功",
            apiCall = { apiService.rechargeCurrency(RechargeCurrencyRequest(amount)) }
        )
    }

    suspend fun getRechargePackages(): RechargePackagesData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取充值套餐列表",
            apiCall = { apiService.getRechargePackages() }
        )
    }

}
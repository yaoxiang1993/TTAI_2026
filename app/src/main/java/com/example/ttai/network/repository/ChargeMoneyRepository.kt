package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.BlindBoxDataResponse
import com.example.ttai.bean.BlindBoxResult
import com.example.ttai.bean.OrderData
import com.example.ttai.bean.OrderDetail
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

    suspend fun getBlindBoxInfo(): BlindBoxDataResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取盲盒数据",
            apiCall = { apiService.getBlindBoxInfo() }
        )
    }

    suspend fun rechargeBlindBox( ): OrderData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "支付盲盒订单创建成功",
            apiCall = { apiService.rechargeBlindBox() }
        )
    }
    suspend fun getBlindBoxOrderResult(order_id:String ): OrderDetail {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "支付盲盒订单创建成功",
            apiCall = { apiService.getBlindBoxOrderResult(order_id) }
        )
    }

}
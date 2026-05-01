package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.ShopItemsResponse
import com.example.ttai.bean.OwnedItemsResponse
import com.example.ttai.bean.PurchaseRequest
import com.example.ttai.bean.PurchaseResponse
import com.example.ttai.bean.ShopLinkResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException

/**
 * 商店相关网络请求仓库
 */
class ShopRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * 获取商店商品列表
     * @return 商店商品列表响应
     * @throws ApiException 当网络请求失败时抛出
     */
    suspend fun getShopItems(): ShopItemsResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取商店商品列表",
            apiCall = { apiService.getShopItems() }
        )
    }
    
    /**
     * 获取用户已拥有商品列表
     * @return 已拥有商品列表响应
     * @throws ApiException 当网络请求失败时抛出
     */
    suspend fun getOwnedItems(): OwnedItemsResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取已拥有商品列表",
            apiCall = { apiService.getOwnedItems() }
        )
    }

    /**
     * 购买商品
     * @param itemId 商品ID
     * @param quantity 购买数量，默认1
     * @return 购买响应
     * @throws ApiException 当网络请求失败时抛出
     */
    suspend fun purchaseItem(itemId: String, quantity: Int = 1): PurchaseResponse {
        val request = PurchaseRequest(itemId, quantity)
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "购买商品",
            apiCall = { apiService.purchaseItem(request) }
        )
    }
    /**
     *  获取商品链接
     */
    suspend fun getShopLinks(): ShopLinkResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "购买商品",
            apiCall = { apiService.getShopLinks() }
        )
    }



}

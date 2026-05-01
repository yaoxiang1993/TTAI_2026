package com.example.ttai.event

/**
 * 购买商品成功
 * 用于通知其他组件AI创建成功，需要刷新数据
 */
data class BuyShopEvent(
    val success: Boolean = true
)

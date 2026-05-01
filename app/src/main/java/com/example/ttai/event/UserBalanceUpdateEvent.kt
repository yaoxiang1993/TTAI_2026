package com.example.ttai.event

/**
 * 用户余额更新事件
 * 用于通知其他组件用户余额发生变化，需要刷新余额显示
 */
data class UserBalanceUpdateEvent(
    val success: Boolean = true
)

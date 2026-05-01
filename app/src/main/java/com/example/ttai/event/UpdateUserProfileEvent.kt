package com.example.ttai.event

/**
 * 开通会员成功
 * 用于通知其他组件AI创建成功，需要刷新数据
 */
data class UpdateUserProfileEvent(
    val success: Boolean = true
)

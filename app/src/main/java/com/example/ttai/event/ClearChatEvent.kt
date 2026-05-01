package com.example.ttai.event

/**
 * 清除聊天信息
 * 用于通知其他组件AI创建成功，需要刷新数据
 */
data class ClearChatEvent(
    val success: Boolean = true
)

package com.example.ttai.event

/**
 * 聊天槽位变化事件
 * 用于通知其他组件聊天槽位发生变化，需要刷新数据
 */
data class ChatSlotChangedEvent(
    val action: String, // "add" 或 "remove"
    val success: Boolean = true,
    val message: String = "聊天槽位变化"
)

package com.example.ttai.event

/**
 * AI创建成功事件
 * 用于通知其他组件AI创建成功，需要刷新数据
 */
data class AICreatedEvent(
    val success: Boolean = true,
    val createSuccessAIID: String? = "",// 更新智能体成功
    val message: String = "AI创建成功"
)

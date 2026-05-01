package com.example.ttai.event

/**
 * 开通记忆之心
 * 用于通知其他组件AI创建成功，需要刷新数据
 */
data class PermanentMemoryEvent(
    val success: Boolean = true
)

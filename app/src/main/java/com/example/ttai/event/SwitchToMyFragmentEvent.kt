package com.example.ttai.event

/**
 * 切换到MyFragment事件
 * 用于通知MainActivity切换到MyFragment（索引为3）
 */
data class SwitchToMyFragmentEvent(
    val targetTabIndex: Int = 3,
    val message: String = "切换到我的页面"
)

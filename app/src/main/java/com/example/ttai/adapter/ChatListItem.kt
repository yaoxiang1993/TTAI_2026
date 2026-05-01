package com.example.ttai.adapter

import com.example.ttai.bean.Conversation

/**
 * 聊天列表项的封装类，支持不同类型的item
 */
sealed class ChatListItem {
    /**
     * 普通的聊天对话item
     */
    data class ConversationItem(val conversation: Conversation) : ChatListItem()
    
    /**
     * 添加AI的占位item
     */
    object AddAIItem : ChatListItem()
}

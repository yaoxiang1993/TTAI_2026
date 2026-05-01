package com.example.ttai.bean

import com.google.gson.annotations.SerializedName

/**
 * 添加聊天槽位请求
 */
data class AddChatSlotRequest(
    @SerializedName("character_id") val characterId: String
)

/**
 * 移除聊天槽位请求
 */
data class RemoveChatSlotRequest(
    @SerializedName("character_id") val characterId: String
)

/**
 * 添加聊天槽位响应数据
 */
data class AddChatSlotResponse(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("character_name") val characterName: String,
    @SerializedName("chat_slots") val chatSlots: List<String>,
    @SerializedName("current_chat_slots") val currentChatSlots: Int,
    @SerializedName("max_chat_slots") val maxChatSlots: Int,
    @SerializedName("conversation_created") val conversationCreated: Boolean,
    @SerializedName("greeting_message") val greetingMessage: String? = null
)

/**
 * 移除聊天槽位响应数据
 */
data class RemoveChatSlotResponse(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("character_name") val characterName: String,
    @SerializedName("current_chat_slots") val currentChatSlots: Int,
    @SerializedName("max_chat_slots") val maxChatSlots: Int,
    @SerializedName("chat_slots") val chatSlots: List<String>
)

/**
 * 槽位已满错误响应数据
 */
data class SlotFullErrorData(
    @SerializedName("current_slots") val currentSlots: Int,
    @SerializedName("max_slots") val maxSlots: Int,
    @SerializedName("upgrade_required") val upgradeRequired: Boolean,
    @SerializedName("upgrade_cost") val upgradeCost: UpgradeCost? = null
)

/**
 * 升级费用
 */
data class UpgradeCost(
    @SerializedName("currency") val currency: String,
    @SerializedName("amount") val amount: Int
)

/**
 * 角色已在槽位中错误响应数据
 */
data class AlreadyInSlotErrorData(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("character_name") val characterName: String,
    @SerializedName("already_in_slot") val alreadyInSlot: Boolean
)

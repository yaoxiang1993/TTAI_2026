package com.example.ttai.bean

import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * 我的AI角色数据模型
 */
@kotlinx.parcelize.Parcelize
data class MyAI(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("brief_intro") val briefIntro: String? = null,
    @SerializedName("opening_line") val openingLine: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("personality_tags") val personalityTags: List<String>,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("voice_type") val voiceType: String? = null,
    @SerializedName("is_public") val isPublic: Boolean? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("popularity") val popularity: Int? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("review_status") val reviewStatus: String? = null,
    @SerializedName("review_message") val reviewMessage: String? = null,
    @SerializedName("reviewed_at") val reviewedAt: String? = null,
    // 保留原有字段以保持向后兼容
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("last_chat_time") val lastChatTime: String? = null,
    @SerializedName("last_chat_message") val lastChatMessage: String? = null,
    @SerializedName("chat_count") val chatCount: Int = 0,
    @SerializedName("creator_id") val creatorId: String? = null,
    @SerializedName("conversation_id") val conversationId: String? = null
) : Parcelable

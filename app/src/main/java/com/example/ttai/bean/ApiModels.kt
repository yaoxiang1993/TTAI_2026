package com.example.ttai.bean

import android.R
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlin.collections.ArrayList

/**
 * 所有接口统一响应结构
 */
data class ApiResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T?
)

// 认证相关模型
data class OneClickAuthRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("device_id") val deviceId: String
)

data class SendCodeRequest(
    @SerializedName("phone") val phone: String
)

data class SendCodeResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: SendCodeData
)

data class SendCodeData(
    @SerializedName("phone") val phone: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class VerifyCodeAuthRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("code") val code: String,
    @SerializedName("device_id") val deviceId: String? = null,
    @SerializedName("invitation_code") val invitationCode: String? = null
)

data class AuthResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AuthData,
    @SerializedName("is_new_user") val isNewUser: Boolean? = null
)

data class AuthData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserProfile,
    @SerializedName("is_new_user") val is_new_user: Boolean
)

// 设备验证失败响应
data class DeviceMismatchResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: DeviceMismatchData
)

data class DeviceMismatchData(
    @SerializedName("reason") val reason: String,
    @SerializedName("phone") val phone: String
)

// 用户不存在响应
data class UserNotFoundResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UserNotFoundData
)

data class UserNotFoundData(
    @SerializedName("reason") val reason: String,
    @SerializedName("phone") val phone: String
)

// 角色相关模型
@kotlinx.parcelize.Parcelize
data class Character(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("personality_tags") val personalityTags: ArrayList<String>?,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("brief_intro") val briefIntro: String? = null,
    @SerializedName("created_at") val createdAt: Long? = null,
    @SerializedName("gender") val gender: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("is_public") val isPublic: Boolean? = null,
    @SerializedName("opening_line") val openingLine: String? = null,
    @SerializedName("review_message") val reviewMessage: String? = null,
    @SerializedName("review_status") val reviewStatus: String? = null,
    @SerializedName("system_prompt") val systemPrompt: String? = null,
    @SerializedName("voice_type") val voiceType: String? = null,
    @SerializedName("background_story") val backgroundStory: String? = null,
    @SerializedName("updated_at") val updatedAt: Long? = null,
    @SerializedName("creator_id") val creatorId: String? = null,
    @SerializedName("popularity") val popularity: Int? = 0,
    @SerializedName("creator_info") val creator_info: CreatorInfo? = null,
    @SerializedName("author_name") val author_name: String? = null,
    @SerializedName("in_chat_slots") val inChatSlots: Boolean? = null,
    @SerializedName("is_my_character") val isMyCharacter: Boolean? = null,
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("reviewed_at") val reviewedAt: String? = null,
    // 保留原有字段以保持向后兼容
    @SerializedName("last_message_time") val lastMessageTime: Long? = null,
    @SerializedName("last_message") val lastMessage: String? = null,
    @SerializedName("chat_count") val chatCount: Int = 0,
    @SerializedName("permanent_memory_enabled") val permanentMemoryEnabled: Boolean? = null,
    @SerializedName("is_pinned") val isPinned : Boolean? = false, // ●is_pinned (boolean): 是否置顶 true: 已置顶  false: 未置顶
    @SerializedName("is_unlimited") val is_unlimited : Boolean? = false // ●is_unlimited  是否无限制
) : Parcelable

// 获取角色列表请求模型
data class GetCharactersRequest(
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20
)

data class UpdatePreferredTagsRequest(
    @SerializedName("tags") val tags: List<String>
)

data class MyCharactersResponse(
    @SerializedName("characters") val characters: List<Character>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class CharacterListResponse(
    @SerializedName("characters") val characters: List<Character>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class CharacterDetailResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CharacterDetailData
)

data class CharacterDetailData(
    @SerializedName("character") val character: Character
)

data class PersonalityTag(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String
)

data class TagsResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: TagsData
)

data class TagsData(
    @SerializedName("tags") val tags: List<PersonalityTag>
)

data class AddFriendRequest(
    @SerializedName("character_id") val characterId: String
)

data class AddFriendResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AddFriendData
)

data class AddFriendData(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("character_name") val characterName: String,
    @SerializedName("fairy_shells_spent") val fairyShellsSpent: Int,
    @SerializedName("remaining_fairy_shells") val remainingFairyShells: Int
)

data class RemoveFriendRequest(
    @SerializedName("character_id") val characterId: String
)

data class CreateCharacterRequest(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("personality_tags") val personalityTags: List<String>,
    @SerializedName("brief_intro") val briefIntro: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("opening_line") val openingLine: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("voice_type") val voiceType: String,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("is_unlimited") val isUnlimited: Boolean
)

data class PurchaseMemoryRequest(
    @SerializedName("character_id") val characterId: String
)

data class BuyExtraSlotResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: BuyExtraSlotData
)

data class BuyExtraSlotData(
    @SerializedName("fairy_jade_balance") val fairyJadeBalance: Int,
    @SerializedName("max_chat_slots") val maxChatSlots: Int?,
    @SerializedName("cost") val cost: Int
)

// 聊天相关模型
data class Conversation(
    @SerializedName("_id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("character_id") val characterId: String,
    @SerializedName("character") val character: Character,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("last_message_time") val lastMessageTime: Long

)

data class ConversationsResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ConversationsData
)

data class ConversationsData(
    @SerializedName("conversations") val conversations: List<Conversation>,
    @SerializedName("max_chat_slots") val maxChatSlots: Int,
    @SerializedName("available_slots") val availableSlots: Int

)
@kotlinx.parcelize.Parcelize
data class Message(
    val id: String? = java.util.UUID.randomUUID().toString(), // 添加唯一ID，避免消息重复
    @SerializedName("_id") var _id: String?="",
    @SerializedName("content") var content: String?="",
    @SerializedName("sender") var sender: String?="", //  "sender": "user|character|lucky_reward",
    @SerializedName("timestamp") var timestamp: Long ?=0,
    @SerializedName("message_type") var message_type: String?="", // text / meme 表情包  /intro 简介
    var isTyping: Boolean = false ,// 是否是正在输入状态
    var isReloadMessage: Boolean = false ,// 是重新生成消息图标

    //红包信息    "won_reward": false,
    @SerializedName("won_reward") var won_reward: Boolean?=false,
    @SerializedName("reward_amount") var reward_amount: String?="",  //      "reward_amount": 0,
    @SerializedName("reward_type") var reward_type: String?="",   //      "reward_type": "fairy_jade"

    ): Parcelable


data class MessagesData(
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

data class SendMessageRequest(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("message") val message: String,
    @SerializedName("vibration_intensity") val vibration_intensity: Int,
    @SerializedName("sucking_intensity") val sucking_intensity: Int,
    @SerializedName("use_ai_intensity") val use_ai_intensity: Boolean

)

data class SendMessageResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: SendMessageData
)

data class SendMessageData(
    @SerializedName("ai_message") val aiMessage: SimpleMessage,
    @SerializedName("character_chat_array") val character_chat_array: List<SimpleMessage>,
    @SerializedName("user_message") val userMessage: SimpleMessage,
    @SerializedName("lucky_reward") val lucky_reward: LuckRewardMessage,
    @SerializedName("vibration_intensity") val vibration_intensity: Int?=0, // (返回当前设备震动值)
    @SerializedName("sucking_intensity") val sucking_intensity: Int?=0 // (返回当前设备吮吸值)
)

data class LuckRewardMessage(
    @SerializedName("won_reward") val won_reward: Boolean = false,
    @SerializedName("reward_amount") val reward_amount: String,
    @SerializedName("reward_type") val reward_type: String // fairy_jade

)
data class SimpleMessage(
    @SerializedName("content") val content: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("message_id") val message_id: String,
    @SerializedName("message_type") val message_type: String

)

data class ClearChatResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ClearChatData
)

data class ClearChatData(
    @SerializedName("deleted_messages") val deletedMessages: Int,
    @SerializedName("deleted_conversations") val deletedConversations: Int
)

// 用户相关模型
@kotlinx.parcelize.Parcelize
data class UserProfile(
    @SerializedName("id") val id: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String?,
    @SerializedName("fairy_jade") val fairyJade: Int,
    @SerializedName("fairy_shells") val fairyShells: Int,
    @SerializedName("preferred_ai_model") val preferredAiModel: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("ai_friends") val aiFriends: List<Character>? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("membership_info") val membershipInfo: MembershipInfo?,
    @SerializedName("permanent_memory_benefits") val permanentMemoryBenefits: PermanentMemoryBenefits?,
    @SerializedName("is_following") val is_following: Boolean?=false,
    @SerializedName("can_follow") val can_follow: Boolean?=false,
    @SerializedName("is_beta_user") val is_beta_user: Boolean?=false,
    @SerializedName("stats") val stats: FollowStats?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("invitation_info") val invitationInfo: InvitationInfo?,
) : Parcelable


/**
 *
 *   "has_invitation_code": true,
 *         "invitation_code": "SC3E9A5BK7M2",
 *         "reward_amount": 100,
 *         "used_count": 5,
 *         "total_rewards": 500,
 *         "created_at": 1702468800
 *
 * */
@kotlinx.parcelize.Parcelize
data class InvitationInfo(
    @SerializedName("has_invitation_code") val hasInvitationCode:  Boolean? = null,
    @SerializedName("invitation_code") val invitationCode: String?,
    @SerializedName("reward_amount") val rewardAmount: String?,
    @SerializedName("used_count") val usedCount: String?, //
    @SerializedName("total_rewards") val totalRewards: String?, //
    @SerializedName("created_at") val createdAt: Long? = null,
    @SerializedName("max_invitations") val max_invitations: String? = null,
    @SerializedName("remaining_invitations") val remaining_invitations: String? = null
) : Parcelable

@kotlinx.parcelize.Parcelize
data class MembershipInfo(
    @SerializedName("level") val level: String?, // 会员等级: free/silver/galaxy
    @SerializedName("name") val name: String?, // "免费会员"   会员名称
    @SerializedName("since") val since: Long? = null, //  "2025-07-28T16:30:00",  成为会员时间
    @SerializedName("expiry") val expiry: Long? = null, //
    @SerializedName("is_active") val isActive: Boolean? = null, //
    @SerializedName("description") val description: String? = null //
) : Parcelable

@kotlinx.parcelize.Parcelize
data class ProfileData(
    @SerializedName("profile") val profile: UserProfile?
) : Parcelable

data class UpdateProfileRequest(
    @SerializedName("username") val username: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("preferred_ai_model") val preferredAiModel: String? = null,

    @SerializedName("bio") val bio: String? = null
)

data class Wallet(
    @SerializedName("fairy_jade") val fairyJade: Int,
    @SerializedName("fairy_shells") val fairyShells: Int,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("premium_expires_at") val premiumExpiresAt: String?
)

data class WalletResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: WalletData
)

data class WalletData(
    @SerializedName("wallet") val wallet: Wallet
)

data class UpgradePremiumRequest(
    @SerializedName("membership_type") val membershipType: String   // 会员类型: silver(银钻会员) 或 galaxy(星河会员) }
)

data class UpgradePremiumResponse(
    @SerializedName("membership_type") val membershipType: String,
    @SerializedName("membership_name") val membershipName: String,
    @SerializedName("expiry_time") val expiryTime: String,
    @SerializedName("days_purchased") val daysPurchased: Int,
    @SerializedName("fairy_jade_spent") val fairyJadeSpent: Int,
    @SerializedName("remaining_jade") val remainingJade: Int,
    @SerializedName("supported_models") val supportedModels: List<String>,
    @SerializedName("description") val description: String
)

data class RechargeCurrencyRequest(
    @SerializedName("amount") val amount: Int
)
data class RechargeCurrencyResponse(
    @SerializedName("order_id") val orderId: String,
    @SerializedName("pay_url") val payUrl: String,
    @SerializedName("fairy_jade_amount") val fairyJadeAmount: String
)
data class CurrencyBalanceResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CurrencyBalanceData
)

data class CurrencyBalanceData(
    @SerializedName("fairy_jade") val fairyJade: Int,
    @SerializedName("fairy_shells") val fairyShells: Int
)

data class AiModel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("cost_per_message") val costPerMessage: String, // 2
    @SerializedName("currency") val currency: String, //  "fairy_shells"
    @SerializedName("currency_name") val currencyName: String //  "仙贝"
)

data class AiSettingsResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: AiSettingsData
)

data class AiSettingsData(
    @SerializedName("preferred_ai_model") val preferredAiModel: String,
    @SerializedName("available_models") val availableModels: List<AiModel>
)

data class UpdateAiModelRequest(
    @SerializedName("preferred_ai_model") val preferredAiModel: String
)

data class UpdateAiModelResponse(
    @SerializedName("preferred_ai_model") val preferredAiModel: String,
    @SerializedName("user_balance") val userBalance: UserBalance
)

data class UserBalance(
    @SerializedName("fairy_jade") val fairyJade: Int,
    @SerializedName("fairy_shells") val fairyShells: Int
)

// 双倍回复升级相关模型
data class UpgradeDoubleReplyData(
    @SerializedName("expires_at") val expiresAt: String,
    @SerializedName("remaining_fairy_jade") val remainingFairyJade: Int,
    @SerializedName("cost") val cost: Int,
    @SerializedName("reply_type") val replyType: String
)

// 支付相关模型
data class CreateOrderRequest(
    @SerializedName("product_id") val productId: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("amount") val amount: Int
)

data class CreateOrderResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CreateOrderData
)

data class CreateOrderData(
    @SerializedName("order_no") val orderNo: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency_amount") val currencyAmount: Int,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_url") val paymentUrl: String,
    @SerializedName("qr_code") val qrCode: String
)

data class OrderItem(
    @SerializedName("order_no") val orderNo: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency_amount") val currencyAmount: Int,
    @SerializedName("status") val status: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("completed_at") val completedAt: String?
)

data class OrderListResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: OrderListData
)

data class OrderListData(
    @SerializedName("list") val list: List<OrderItem>,
    @SerializedName("pagination") val pagination: Pagination
)

data class Pagination(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("total_pages") val totalPages: Int
)

// 健康检查模型
data class HealthResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: HealthData
)

data class HealthData(
    @SerializedName("status") val status: String,
    @SerializedName("timestamp") val timestamp: String
)

// 错误响应模型
data class ErrorResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: ErrorData?
)

data class ErrorData(
    @SerializedName("required") val required: Int? = null,
    @SerializedName("current") val current: Int? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("model") val model: String? = null
)

// ==================== 精选角色和综合角色相关模型 ====================

/**
 * 精选角色列表响应
 */
data class FeaturedCharactersResponse(
    @SerializedName("characters") val characters: List<Character>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

/**
 * 精选角色数据模型
 */
data class FeaturedCharacter(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("personality_tags") val personalityTags: List<String>,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("opening_line") val openingLine: String,
    @SerializedName("popularity") val popularity: Int,
    @SerializedName("is_public") val isPublic: Boolean,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("creator_id") val creatorId: String
)

/**
 * 综合角色列表响应
 */
data class ComprehensiveCharactersResponse(
    @SerializedName("characters") val characters: List<Character>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("sort_by") val sortBy: String,
    @SerializedName("sort_order") val sortOrder: Int
)

// ==================== 聊天槽位替换相关模型 ====================

/**
 * 替换聊天槽位请求模型
 */
data class ReplaceChatSlotRequest(
    @SerializedName("old_character_id") val oldCharacterId: String,
    @SerializedName("new_character_id") val newCharacterId: String
)

/**
 * 替换聊天槽位响应模型
 */
data class ReplaceChatSlotResponse(
    @SerializedName("old_character") val oldCharacter: OldCharacterInfo,
    @SerializedName("new_character") val newCharacter: NewCharacterInfo,
    @SerializedName("chat_slots") val chatSlots: List<String>,
    @SerializedName("current_chat_slots") val currentChatSlots: Int,
    @SerializedName("max_chat_slots") val maxChatSlots: Int,
    @SerializedName("conversation_created") val conversationCreated: Boolean,
    @SerializedName("greeting_message") val greetingMessage: String
)

/**
 * 旧角色信息
 */
data class OldCharacterInfo(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

/**
 * 新角色信息
 */
data class NewCharacterInfo(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("personality_tags") val personalityTags: List<String>
)

/**
 * 错误响应 - 旧角色不在槽位中
 */
data class OldCharacterNotFoundError(
    @SerializedName("old_character_id") val oldCharacterId: String,
    @SerializedName("old_character_name") val oldCharacterName: String
)

/**
 * 错误响应 - 新角色已在槽位中
 */
data class NewCharacterAlreadyInSlotError(
    @SerializedName("new_character_id") val newCharacterId: String,
    @SerializedName("new_character_name") val newCharacterName: String,
    @SerializedName("already_in_slot") val alreadyInSlot: Boolean
)

/**
 * 错误响应 - 新角色需要先添加为AI好友
 */
data class NewCharacterNeedAddFriendError(
    @SerializedName("new_character_id") val newCharacterId: String,
    @SerializedName("new_character_name") val newCharacterName: String,
    @SerializedName("need_add_friend_first") val needAddFriendFirst: Boolean
)

/**
 * 默认角色聊天历史响应数据模型
 */
data class DefaultCharacterChatResponse(
    @SerializedName("messages") val messages: List<Message>,
    @SerializedName("conversation_id") val conversationId: String,
    @SerializedName("character") val character: Character,
    @SerializedName("total_messages") val totalMessages: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

/**
 * 默认角色信息
 */
@kotlinx.parcelize.Parcelize
data class DefaultCharacterInfo(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("brief_intro") val briefIntro: String
) : Parcelable

/**
 * 图片上传响应数据模型
 */
data class ImageUploadResponse(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("original_filename") val originalFilename: String,
    @SerializedName("size") val size: Long,
    @SerializedName("snowflake_id") val snowflakeId: String
)

/**
 * 删除角色响应数据模型
 */
data class DeleteCharacterData(
    @SerializedName("deleted_character_id") val deletedCharacterId: String,
    @SerializedName("character_name") val characterName: String,
    @SerializedName("deleted_at") val deletedAt: String
)

/**
 * 角色正在使用中错误响应数据模型
 */
data class CharacterInUseErrorData(
    @SerializedName("active_conversations") val activeConversations: Int
)

// ==================== 会员套餐相关模型 ====================

/**
 * 会员套餐响应数据模型
 */
data class MembershipPackagesResponse(
    @SerializedName("data") val data: List<MembershipPackageGroup>
)

/**
 * 会员套餐组数据模型
 */
data class MembershipPackageGroup(
    @SerializedName("packageName") val packageName: String,
    @SerializedName("packages") val packages: List<MembershipPackage>
)

/**
 * 会员套餐数据模型
 */
data class MembershipPackage(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("days") val days: Int,
    @SerializedName("models") val models: List<String>,
    @SerializedName("description") val description: String
)

// ==================== 用户引导状态相关模型 ====================

/**
 * 用户引导状态数据模型
 */
data class OnboardingStatusData(
    @SerializedName("has_preferred_tags") val hasPreferredTags: Boolean,
    @SerializedName("preferred_tags") val preferredTags: List<String>,
    @SerializedName("should_show_tag_selection") val shouldShowTagSelection: Boolean,
    @SerializedName("can_skip_to_main") val canSkipToMain: Boolean
)

// ==================== 搜索相关模型 ====================

/**
 * 角色搜索请求数据模型
 */
data class CharacterSearchRequest(
    @SerializedName("keyword") val keyword: String,
    @SerializedName("page") val page: Int = 1,
    @SerializedName("limit") val limit: Int = 20
)

/**
 * 角色搜索响应数据模型
 */
data class CharacterSearchResponse(
    @SerializedName("characters") val characters: List<Character>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("search_params") val searchParams: SearchParams
)

/**
 * 搜索参数数据模型
 */
data class SearchParams(
    @SerializedName("keyword") val keyword: String
)

// ==================== 商店相关模型 ====================

/**
 * 商店商品数据模型
 */
data class ShopItem(
    @SerializedName("item_id") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("price") val price: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("currency_name") val currencyName: String,
    @SerializedName("category") val category: String,
    @SerializedName("is_active") val isActive: Boolean
)

/**
 * 商店商品列表响应数据模型
 */
data class ShopItemsResponse(
    @SerializedName("items") val items: List<ShopItem>,
    @SerializedName("total_count") val totalCount: Int
)

/**
 * 已拥有商品数据模型
 */
data class OwnedItem(
    @SerializedName("item_id") val itemId: String,
    @SerializedName("item_name") val itemName: String,
    @SerializedName("description") val description: String,
    @SerializedName("unit_price") val unitPrice: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("currency_name") val currencyName: String,
    @SerializedName("category") val category: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("last_purchase_time") val lastPurchaseTime: String,
    @SerializedName("total_quantity") val totalQuantity: Int
)

/**
 * 已拥有商品列表响应数据模型
 */
data class OwnedItemsResponse(
    @SerializedName("items") val items: List<OwnedItem>,
    @SerializedName("total_count") val totalCount: Int
)

// ==================== 购买相关模型 ====================

/**
 * 购买商品请求数据模型
 */
data class PurchaseRequest(
    @SerializedName("item_id") val itemId: String,
    @SerializedName("quantity") val quantity: Int = 1
)

/**
 * 购买商品响应数据模型
 */
data class PurchaseResponse(
    @SerializedName("item") val item: PurchaseItem,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("total_price") val totalPrice: Int,
    @SerializedName("remaining_balance") val remainingBalance: Int,
    @SerializedName("purchase_record") val purchaseRecord: PurchaseRecord
)

/**
 * 购买的商品信息
 */
data class PurchaseItem(
    @SerializedName("item_id") val itemId: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("currency") val currency: String
)

/**
 * 购买记录
 */
data class PurchaseRecord(
    @SerializedName("user_id") val userId: String,
    @SerializedName("item_id") val itemId: String,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("unit_price") val unitPrice: Int,
    @SerializedName("total_price") val totalPrice: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("purchase_time") val purchaseTime: String,
    @SerializedName("status") val status: String
)

/**
 * 余额不足错误响应数据模型
 */
data class InsufficientBalanceError(
    @SerializedName("required") val required: Int,
    @SerializedName("current") val current: Int,
    @SerializedName("shortage") val shortage: Int
)

// ==================== 永久记忆权益相关模型 ====================

/**
 * 分配永久记忆权益请求数据模型
 */
data class AllocatePermanentMemoryRequest(
    @SerializedName("character_id") val characterId: String
)

/**
 * 分配永久记忆权益响应数据模型
 */
data class AllocatePermanentMemoryResponse(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("character_name") val characterName: String,
    @SerializedName("permanent_memory_enabled") val permanentMemoryEnabled: Boolean,
    @SerializedName("remaining_benefits") val remainingBenefits: Int,
    @SerializedName("total_benefits") val totalBenefits: Int
)

/**
 * 回收永久记忆权益请求数据模型
 */
data class DeallocatePermanentMemoryRequest(
    @SerializedName("character_id") val characterId: String
)

/**
 * 回收永久记忆权益响应数据模型
 */
data class DeallocatePermanentMemoryResponse(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("permanent_memory_enabled") val permanentMemoryEnabled: Boolean,
    @SerializedName("remaining_benefits") val remainingBenefits: Int,
    @SerializedName("total_benefits") val totalBenefits: Int
)

/**
 * 永久记忆状态响应数据模型
 */
data class PermanentMemoryStatusResponse(
    @SerializedName("character_id") val characterId: String,
    @SerializedName("permanent_memory_enabled") val permanentMemoryEnabled: Boolean,
    @SerializedName("total_benefits") val totalBenefits: Int,
    @SerializedName("available_benefits") val availableBenefits: Int
)

/**
 * 权益不足错误响应数据模型
 */
@kotlinx.parcelize.Parcelize
data class PermanentMemoryBenefits(
    @SerializedName("allocated_characters") val allocatedCharacters: ArrayList<String>?,
    @SerializedName("available_benefits") val availableBenefits: Int,
    @SerializedName("total_benefits") val totalBenefits: Int,
    @SerializedName("used_benefits") val usedBenefits: Int
) : Parcelable

/**
 * 我的AI角色列表响应
 */
data class MyAIListResponse(
    @SerializedName("characters") val characters: List<Character>,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int
)

// ==================== 充值套餐相关模型 ====================

/**
 * 充值套餐数据模型
 */
data class RechargePackage(
    @SerializedName("id") val id: String,
    @SerializedName("fairy_jade") val fairyJade: Int,
    @SerializedName("bonus_jade") val bonus_jade: Int,
    @SerializedName("price") val price: Double,
    @SerializedName("currency") val currency: String,
    @SerializedName("recommended") val recommended: Boolean,
    @SerializedName("description") val description: String,
    @SerializedName("total_jade") val total_jade: Int
)

/**
 * 充值套餐列表响应数据模型
 */
data class RechargePackagesResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: RechargePackagesData
)

/**
 * 充值套餐列表数据模型
 */
data class RechargePackagesData(
    @SerializedName("packages") val packages: List<RechargePackage>,
    @SerializedName("promotion_text") val promotionText: String
)

/**
 * 蓝牙设备播放模式
 */
data class ModelsItemsResponse(
    @SerializedName("modes") val modes: List<CollectItem>,
    @SerializedName("total_count") val totalCount: Int
)

/**
 * 蓝牙设备播放数据模型
 */
data class CollectItem(
    @SerializedName("bluetooth_mode_id") val bluetoothModeId: List<String>,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("is_favorited") val isFavorited: Boolean
)
/**
 *●id: 模型唯一标识符
 * ●name: 模型显示名称
 * ●description: 模型详细描述
 * ●provider: 模型供应商（doubao/google/grok/anthropic）
 * ●is_premium: 是否为付费模型
 * ●cost_per_message: 每次对话真实价格
 * ●original_price: 原价（真实价格的2倍）
 * ●currency: 货币单位（“仙玉·仙贝”）
 * ●available: 模型是否可用
 * ●providers: 按供应商分组的模型列表
 * ●total: 模型总数
 * ●default_model: 默认推荐模型
 * ●free_models: 免费模型列表
 * ●premium_models: 付费模型列表
 *
 */
data class ChatModelItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("available") val available: Boolean = false,
    @SerializedName("cost_per_message") val cost_per_message: String = "",
    @SerializedName("currency") val currency: String = "",
    @SerializedName("is_premium") val is_premium: String = "",
    @SerializedName("original_price") val original_price: String = "",
    @SerializedName("provider") val provider: String = "",
    var isDefaultModel: Boolean = false

)

/**
 * 收藏模式
 */
data class FavoriteRequest(
    @SerializedName("mode_id") val modeId: String
)
/**
 * 蓝牙设备播放模式
 */
data class FavoriteResponse(
    @SerializedName("mode_id") val mode_id: String,
    @SerializedName("favorite_count") val favoriteCount: Int
)
/**
 * ●followers_count: 粉丝数量（基于真实关注关系）
 * ●following_count: 关注数量（基于真实关注关系）
 * ●total_conversations: 总对话数
 * ●total_messages: 总消息数
 * ●membership_days_remaining: 会员剩余天数
 * ●chat_slots_used: 已使用聊天槽位数
 * ●max_chat_slots: 最大聊天槽位数
 *
 */
@kotlinx.parcelize.Parcelize
data class FollowStats(
    @SerializedName("followers_count") val followersCount: String,
    @SerializedName("following_count") val followingCount: String,
    @SerializedName("total_conversations") val total_conversations: String,
    @SerializedName("total_messages") val total_messages: String,
    @SerializedName("membership_days_remaining") val membership_days_remaining: String,
    @SerializedName("chat_slots_used") val chat_slots_used: String,
    @SerializedName("max_chat_slots") val max_chat_slots: String
) : Parcelable

/**
3.1 关注用户接口
接口地址: POST /api/user/follow
功能描述: 关注指定用户

"target_user_id": "68b479a4c51416f4807ce388",
"target_username": "用户8493",
"follow_id": "68fbb544caf57e6fff14afd9",
"followed_at": 1761326404
 *
 */
@kotlinx.parcelize.Parcelize
data class FollowResponse(
    @SerializedName("target_user_id") val target_user_id: String,
    @SerializedName("target_username") val target_username: String,
    @SerializedName("follow_id") val follow_id: String,
    @SerializedName("followed_at") val followed_at: String,
    @SerializedName("unfollowed_at") val unfollowed_at: String
) : Parcelable


data class FollowRequest(
    @SerializedName("target_user_id") val target_user_id: String?
)


/**
 * 接口地址: GET /api/user/followers
 * 功能描述: 获取当前用户的粉丝列表
 *
"user_id": "68b45726c51416f4807ce37a",
"username": "用户5228",
"avatar_url": "",
"followed_at": 1761326404
 */
data class FollowsResponse(
    @SerializedName("followers") var followers: List<FollowsItem>?,
    @SerializedName("following") var following: List<FollowsItem>?
)

/**
 * 接口地址: GET /api/user/followers
 * 功能描述: 获取当前用户的粉丝列表
 *
"user_id": "68b45726c51416f4807ce37a",
"username": "用户5228",
"avatar_url": "",
"followed_at": 1761326404
 */
data class FollowsItem(
    @SerializedName("user_id") val user_id: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("avatar_url") val avatar_url: String?,
    @SerializedName("is_mutual") val is_mutual: Boolean? = false,
    @SerializedName("followed_at") val followed_at: Long?,
    var isFollowing: Boolean = true, // 本地数据不是接口返回的
)

/**
 * 蓝牙设备播放模式
 */
data class ChatModelsResponse(
    @SerializedName("models") val models: List<ChatModelItem>,
    @SerializedName("default_model") val default_model: String,
    @SerializedName("current_user_model") val current_user_model: String?,
    @SerializedName("free_models") val free_models: List<ChatModelItem>,
    @SerializedName("premium_models") val premium_models: List<ChatModelItem>
)


/**
 *   "character_id": "6900f3ec25a256a8be9c9622",
 *     "character_name": "更新后的角色名称",
 *     "is_pinned": true,
 *     "pinned_count": 1
 *
 */
data class PinResponse(
    @SerializedName("character_id") val character_id: String,
    @SerializedName("character_name") val character_name: String,
    @SerializedName("is_pinned") val is_pinned: String,
    @SerializedName("pinned_count") val pinned_count: String
)


data class PinRequest(
    @SerializedName("character_id") val character_id: String?
)

data class MessageRequest(
    @SerializedName("message_id") val message_id: String?
)

/**
●deleted_count: 删除的消息数量
●rollback_to_message_id: 回溯到的消息ID
●rollback_timestamp: 回溯点的时间戳
●character_id: 角色ID
●character_name: 角色名称
●remaining_messages: 回溯后剩余的消息数量

 *
 */
data class RollbackMessageResponse(
    @SerializedName("deleted_count") val deleted_count: Int,
    @SerializedName("rollback_to_message_id") val rollback_to_message_id: String,
    @SerializedName("rollback_timestamp") val rollback_timestamp: String,
    @SerializedName("character_id") val character_id: String,
    @SerializedName("character_name") val character_name: String,
    @SerializedName("remaining_messages") val remaining_messages: String
)
/**
 *  账单明细
 */
data class PayHistoryResponse(
    @SerializedName("bills") val bills:List<PayHistoryItem>,
    @SerializedName("pagination") val pagination:PaginationEntity
)
/**
 *  账单明细
 *   "_id": "68fbb544caf57e6fff14afd9",
 *         "user_id": "68b45726c51416f4807ce37a",
 *         "transaction_type": "chat_consume",
 *         "currency_type": "fairy_jade",
 *         "amount": -4,
 *         "description": "消耗仙玉",
 *         "status": "success",
 *         "related_id": "message_id_123",
 *         "extra_data": {
 *           "ai_model": "xinghe",
 *           "character_name": "小慧",
 *           "message_id": "message_id_123"
 *           "order_id": "order_123456"
 *         },
 *         "created_at": 1761326404,
 *         "updated_at": 1761326404
 *       }
 *
 */
data class PayHistoryItem(
    @SerializedName("_id") val _id:String?,
    @SerializedName("user_id") val user_id:String?,
    @SerializedName("transaction_type") val transaction_type:String?,
    @SerializedName("currency_type") val currency_type:String?,
    @SerializedName("amount") val amount:String?,
    @SerializedName("description") val description:String?,
    @SerializedName("status") val status:String?,
    @SerializedName("related_id") val related_id:String?,
    @SerializedName("extra_data") val extra_data:PayHistoryExtraDataItem?,
    @SerializedName("created_at") val created_at: Long?,
    @SerializedName("updated_at") val updated_at: Long?
)
data class PayHistoryExtraDataItem(
    @SerializedName("ai_model") val ai_model:String,
    @SerializedName("character_name") val character_name:String,
    @SerializedName("message_id") val message_id:String,
    @SerializedName("order_id") val order_id:String,
)
@Parcelize
data class CreatorInfo(
    @SerializedName("creator_avatar") val creator_avatar:String,
    @SerializedName("creator_id") val creator_id:String,
    @SerializedName("creator_name") val creator_name:String
): Parcelable

/**
*    "message_id": "68b6da20d1501e89ab874182",
 *     "new_content": "*办公室里只有一盏台灯亮着，余笙正靠在办公桌边缘，修长的手指夹着一支没点燃的香烟，墨色的眸子在昏暗中显得更加深邃*\n\n\"这就是你的回应？\"\n\n*他缓缓站起身，身影在灯光下拉得很长，脚步声在安静的办公室里格外清晰*\n\n\"我不喜欢等人，更不喜欢敷衍。\"\n\n*走到你面前，居高临下地看着你，眼神带着审视*\n\n\"既然来了，就不要浪费我的时间。坐下，还是...需要我帮你做决定？\"\n\n*语调依然是那种令人无法拒绝的低沉，但字里行间透着不容置疑的强势*",
 *     "timestamp": 1761325260,
 *     "cost": 10,
 *     "currency_used": "fairy_jade"
 *
 * */
data class RegenerateMessageResponse(
    @SerializedName("message_id") var message_id: String?,
    @SerializedName("new_content") var new_content: String?,
    @SerializedName("timestamp") var timestamp: Long?,
    @SerializedName("cost") var cost: String?,
    @SerializedName("currency_used") var currency_used: String?,
    @SerializedName("message_type") var message_type: String?,
    @SerializedName("lucky_reward") var lucky_reward: LuckRewardMessage?
)
/**
 * "nickname": "姐姐",
 * "gender": "女",
 * "identity": "继姐",
 * "personality_description": "温柔体贴的继姐，会照顾弟弟",
 * "updated_at": 1762251580
 *
 * */
data class ConversationSettingsResponse(
    @SerializedName("nickname") val nickname: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("identity") val identity: String,
    @SerializedName("personality_description") val personality_description: String,
    @SerializedName("updated_at") val updated_at: Long
)


/**
 *
 * 更新对话设定
 *
{
"character_id": "68b53a6a42fb612951702f61",
"nickname": "姐姐",
"gender": "女",
"identity": "继姐",
"personality_description": "温柔体贴的继姐，会照顾弟弟"
}

 *
 * */
data class ConversationSettingsRequest(
    @SerializedName("character_id") val character_id: String?,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("identity") val identity: String?,
    @SerializedName("personality_description") val personality_description: String?
)

/**
 *    "memory_book": {
 *       "_id": "691094c8a895cbea41b1ea29",
 *       "user_id": "68b45726c51416f4807ce37a",
 *       "character_id": "68b71a54653b7a77bf312ce8",
 *       "content": "洛迦·梵卓是一个神秘的角色，他喜欢在深夜思考哲学问题。",
 *       "created_at": 1762694344,
 *       "updated_at": 1762694366
 *     },
 *     "character_name": "洛迦·梵卓"
 *
 * */
data class MemoryBookResponse(
    @SerializedName("memory_book") val memory_book: MemoryBook
)
data class MemoryBook(
    @SerializedName("_id") val _id: String,
    @SerializedName("user_id") val user_id: String,
    @SerializedName("character_id") val character_id: String,
    @SerializedName("content") val content: String,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("updated_at") val updated_at: String ,
    @SerializedName("character_name") val character_name: String
)

data class MemoryBookRequest(
    @SerializedName("character_id") val character_id: String?,
    @SerializedName("content") val content: String?
)
data class PaginationEntity(
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int=1
)

/**
 * 接口地址: GET /api/user/followers
 * 功能描述: 获取当前用户的粉丝列表
 *
"total": 15,
"page": 1,
"limit": 20,
"total_pages": 1
 */
data class NotificationsResponse(
    @SerializedName("notifications") var notifications: List<NotificationsEntity>?,
    @SerializedName("total") val total: Int,
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total_pages") val totalPages: Int=1
)

/**
 *   "id": "507f1f77bcf86cd799439011",
 *         "type": "invitation_reward",
 *         "title": "邀请奖励",
 *         "content": "您邀请的用户「张三」已成功注册，您获得了100仙玉奖励！",
 *         "is_read": false,
 *         "related_id": null,
 *         "related_type": null,
 *         "extra_data": {
 *           "invitee_name": "张三",
 *           "reward_amount": 100
 *         },
 *         "created_at": 1702555200,
 *         "read_at": null
 * */
@kotlinx.parcelize.Parcelize
data class NotificationsEntity(
    @SerializedName("id") val id: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("is_read") val is_read: Boolean?,
    @SerializedName("related_id") val related_id: String?,
    @SerializedName("related_type") val related_type: String?,
    @SerializedName("created_at") val created_at: Long?,
    @SerializedName("read_at") val read_at: Long?,
    @SerializedName("extra_data") val extra_data: ExtraData?
) : Parcelable

@kotlinx.parcelize.Parcelize
data class ExtraData(
    @SerializedName("invitee_name") val invitee_name: String?,
    @SerializedName("reward_amount") val reward_amount: String?,
) : Parcelable

data class DeleteMessageRequest(
    @SerializedName("message_id") val messageId: String?
)

/**
 *   "message_id": "消息ID",
 *   "content": "新的消息内容"
 * */
data class UpdateMessageRequest(
    @SerializedName("message_id") val messageId: String?,
    @SerializedName("content") val content: String?
)

/**
 *   "deleted_message_id": "507f1f77bcf86cd799439011",
 *     "character_id": "68b479a4c51416f4807ce388",
 *     "character_name": "梅樊",
 *     "deleted_message_type": "text",
 *     "deleted_sender": "user",
 *     "remaining_messages": 15
 */
data class DeletedMessageResponse(
    @SerializedName("deleted_message_id") var deleted_message_id: String="",
    @SerializedName("character_id") val character_id: String="",
    @SerializedName("character_name") val character_name: String="",
    @SerializedName("deleted_message_type") val deleted_message_type: String="",
    @SerializedName("deleted_sender") val deleted_sender: String="",
    @SerializedName("remaining_messages") val remaining_messages: Int=0
)
/**
"message_id": "507f1f77bcf86cd799439011",
"old_content": "原来的消息内容",
"new_content": "更新后的消息内容",
"character_id": "68b479a4c51416f4807ce388",
"character_name": "梅樊",
"message_type": "text",
"updated_at": 1761326404
 */
data class UpdateMessageResponse(
    @SerializedName("message_id") var message_id: String="",
    @SerializedName("old_content") val old_content: String="",
    @SerializedName("new_content") val new_content: String="",
    @SerializedName("character_id") val character_id: String="",
    @SerializedName("character_name") val character_name: String="",
    @SerializedName("message_type") val message_type: String="",
    @SerializedName("updated_at") val updated_at: Long,
)

/**
"unread_count": 5,
"has_unread": true
 */
data class UnreadCountResponse(
    @SerializedName("unread_count") var unreadCount: String="",
    @SerializedName("has_unread") val hasUnread: Boolean?
)

/**
"notification_id": "507f1f77bcf86cd799439011",
"unread_count": 4
 */
data class MarkReadResponse(
    @SerializedName("notification_id") var notificationId: String="",
    @SerializedName("unread_count") val unReadCount: String?=""
)
data class MarkReadRequest(
    @SerializedName("notification_id") val notificationId: String?
)
data class SwitchBranchRequest(
    @SerializedName("character_id") val character_id: String?,
    @SerializedName("branch_id") val branch_id: String?
)
data class PinBranchRequest(
    @SerializedName("branch_id") val branch_id: String?
)
data class CreateBranchRequest(
    //  "character_id": "68b53a6a42fb612951702f61",
//"name": "我的新支线",
//"mode": "reset",
//"source_branch_id": null
    @SerializedName("character_id") val character_id: String?,
    @SerializedName("mode") val mode: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("source_branch_id") val source_branch_id: String?
)
data class EditeBranchRequest(
    @SerializedName("name") val name: String?
)
data class ShopLinkResponse(
    @SerializedName("jd_link") var jdLink: String="",
    @SerializedName("taobao_link") val taobaoLink: String=""
)

/**
 *
 * "data": {
 *     "character_id": "68b53a6a42fb612951702f61",
 *     "character_name": "角色A",
 *     "main_branch": {
 *       "branch_id": null,
 *       "name": "主聊天",
 *       "is_main": true,
 *       "conversation_id": "6914b6c91e6bfd32d7a549d7",
 *       "message_count": 15,
 *       "last_message": "你好",
 *       "last_message_time": "2026-02-12T22:32:06.282+00:00",
 *       "unread_count": 0
 *     },
 *     "branches": [
 *       {
 *         "branch_id": "branch_abc123",
 *         "name": "支线1",
 *         "is_main": false,
 *         "source_branch_id": null,
 *         "message_count": 8,
 *         "last_message": "[表情]",
 *         "last_message_time": "2026-02-12T22:32:36.282+00:00",
 *         "unread_count": 1,
 *         "created_at": "2026-02-12T22:30:06.282+00:00",
 *         "updated_at": "2026-02-12T22:32:36.282+00:00"
 *       }
 *     ],
 *     "total_branches": 1
 *   }
 *
 * */
data class BranchManagerResponse(
    @SerializedName("character_id") var character_id: String="",
    @SerializedName("character_name") val character_name: String="",
    @SerializedName("current_branch_id") val current_branch_id: String="",
    @SerializedName("main_branch") var main_branch: BranchItem,
    @SerializedName("branches") val branches: List<BranchItem>
)

/**
 *"branch_id": "branch_abc123",
 *         "name": "支线1",
 *         "is_main": false,
 *         "source_branch_id": null,
 *         "message_count": 8,
 *         "last_message": "[表情]",
 *         "last_message_time": "2026-02-12T22:32:36.282+00:00",
 *         "unread_count": 1,
 *         "created_at": "2026-02-12T22:30:06.282+00:00",
 *         "updated_at": "2026-02-12T22:32:36.282+00:00"
 * */
data class BranchItem(
    @SerializedName("branch_id") var branch_id: String?,
    @SerializedName("conversation_id") var conversation_id: String?,
    @SerializedName("name") var name: String?,
    @SerializedName("is_main") var is_main: Boolean = false,
    @SerializedName("is_active") var is_active: Boolean = false,
    @SerializedName("is_pinned") var is_pinned: Boolean = false,
    @SerializedName("source_branch_id") var source_branch_id: String = "",
    @SerializedName("message_count") var message_count: Int = 0,
    @SerializedName("last_message") var last_message: String = "",
    @SerializedName("last_message_time") var last_message_time: Long ?=0L,
    @SerializedName("unread_count") var unread_count: Int = 0,
    @SerializedName("created_at") var created_at: String = "",
    @SerializedName("updated_at") var updated_at: String = "",
    var isDefaultModel: Boolean = false
)
/***
 * "character_id": "68b53a6a42fb612951702f61",
 * "branch_id": "branch_abc123",
 * "branch_name": "支线1",
 * "is_main": false,
 * "updated_at": 1760193300
 *
 */
data class BranchResponse(
    @SerializedName("character_id") var character_id: String="",
    @SerializedName("branch_id") val branch_id: String="",
    @SerializedName("branch_name") val branch_name: String="",
    @SerializedName("is_main") var is_main: Boolean=false,
    @SerializedName("updated_at") val updated_at: Long
)


/**
 *
 * **字段说明**
 *
 * | 字段名 | 类型 | 说明 |
 * | --- | --- | --- |
 * | has_update | bool | 是否存在更新 |
 * | force_update | bool | 是否强制更新 |
 * | latest_version | string/null | 最新版本号 |
 * | latest_build | int/null | 最新build号 |
 * | min_supported_build | int/null | 最低支持build，小于该值可判定为强更 |
 * | title | string | 弹窗标题 |
 * | subtitle | string | 弹窗副标题 |
 * | release_notes | array | 更新内容列表 |
 * | action_type | string | `download` / `store` / `tip` |
 * | action_url | string | 安卓下载地址或 iOS App Store 地址 |
 * | button_text | string | 主按钮文案 |
 * | cancel_text | string | 次按钮文案 |
 * | published_at | int/null | 发布时间，秒级时间戳 |
 *
 * **错误码**
 *
 * | 错误码 | 说明 |
 * | --- | --- |
 * | 22001 | platform是必填项 |
 * | 22002 | platform不合法 |
 * | 22003 | current_build不是整数 |
 * | 22004 | current_build和current_version都未提供 |
 * | 22099 | 服务器内部错误 |
 * */
data class AppUpdateInfo(
    @SerializedName("has_update")
    val hasUpdate: Boolean,

    @SerializedName("force_update")
    val forceUpdate: Boolean,

    @SerializedName("platform")
    val platform: String,

    @SerializedName("channel")
    val channel: String,

    @SerializedName("current_version")
    val currentVersion: String?, // JSON 中为 null，设为可空

    @SerializedName("current_build")
    val currentBuild: Int,

    @SerializedName("latest_version")
    val latestVersion: String,

    @SerializedName("latest_build")
    val latestBuild: Long =0,

    @SerializedName("min_supported_build")
    val minSupportedBuild: Int?,

    @SerializedName("title")
    val title: String,

    @SerializedName("subtitle")
    val subtitle: String,

    @SerializedName("release_notes")
    val releaseNotes: List<String>,

    @SerializedName("action_type")
    val actionType: String,

    @SerializedName("action_url")
    val actionUrl: String,

    @SerializedName("button_text")
    val buttonText: String,

    @SerializedName("cancel_text")
    val cancelText: String,

    @SerializedName("published_at")
    val publishedAt: Long
)

/**
 * 盲盒数据根对象
 */
data class BlindBoxDataResponse(
    @SerializedName("blind_box")
    val blindBox: BlindBoxInfo,

    @SerializedName("current_winning_status")
    val currentWinningStatus: CurrentWinningStatus
)

/**
 * 盲盒基础信息
 */
data class BlindBoxInfo(
    @SerializedName("product_id")
    val productId: String, // 盲盒商品ID

    @SerializedName("name")
    val name: String, // 盲盒名称

    @SerializedName("price")
    val price: Double, // 售价，单位元

    @SerializedName("currency")
    val currency: String, // 币种，固定 CNY

    @SerializedName("payment_methods")
    val paymentMethods: List<String>, // 当前支持的支付方式

    @SerializedName("rule_version")
    val ruleVersion: String, // 规则版本号
    @SerializedName("reward_description")
    val rewardDescription: String, // "可随机获得1000-14000仙玉"

    @SerializedName("open_immediately")
    val openImmediately: Boolean, // 是否支付后直接开启

    @SerializedName("rules")
    val rules: List<BlindBoxRule> // 奖池规则列表
)

/**
 * 奖池具体区间规则
 */
data class BlindBoxRule(
    @SerializedName("range_key")
    val rangeKey: String, // 区间唯一标识

    @SerializedName("range_label")
    val rangeLabel: String, // 区间展示文案

    @SerializedName("min_jade")
    val minJade: Int, // 最小仙玉值

    @SerializedName("max_jade")
    val maxJade: Int, // 最大仙玉值

    @SerializedName("probability")
    val probability: Double, // 概率，小数形式 (例如 0.15)

    @SerializedName("probability_text")
    val probabilityText: String, // 概率展示文案 (例如 "15%")

    @SerializedName("mean_jade")
    val meanJade: Int, // 区间均值

    @SerializedName("description")
    val description: String // 区间说明
)

/**
 * 中奖状态统计
 */
data class CurrentWinningStatus(
    @SerializedName("recent_wins")
    val recentWins: List<RecentWinRecord>, // 最近中奖播报

    @SerializedName("overall_stats")
    val overallStats: OverallStats, // 全站盲盒统计

    @SerializedName("user_summary")
    val userSummary: UserBlindBoxSummary? // 当前用户自己的摘要，未登录或无数据时为 null
)

/**
 * 单条最近中奖记录
 */
data class RecentWinRecord(
    @SerializedName("username_masked")
    val usernameMasked: String, // 脱敏后的用户名

    @SerializedName("reward_jade")
    val rewardJade: Int, // 获得的仙玉数量

    @SerializedName("range_label")
    val rangeLabel: String, // 中奖区间标签

    @SerializedName("range_probability_text")
    val rangeProbabilityText: String, // 该区间概率文案

    @SerializedName("opened_at")
    val openedAt: Long // 开启时间戳
)

/**
 * 全站统计信息
 */
data class OverallStats(
    @SerializedName("total_count")
    val totalCount: Int, // 总开启次数

    @SerializedName("total_reward_jade")
    val totalRewardJade: Long, // 总产出仙玉

    @SerializedName("max_reward_jade")
    val maxRewardJade: Int // 全站单次最高奖励
)

/**
 * 用户个人盲盒摘要
 */
data class UserBlindBoxSummary(
    @SerializedName("purchase_count")
    val purchaseCount: Int, // 购买次数

    @SerializedName("total_reward_jade")
    val totalRewardJade: Long, // 累计获得仙玉

    @SerializedName("max_reward_jade")
    val maxRewardJade: Int, // 个人单次最高奖励

    @SerializedName("last_reward_jade")
    val lastRewardJade: Int, // 最近一次获得的仙玉

    @SerializedName("last_opened_at")
    val lastOpenedAt: Long // 最近一次开启时间戳
)

/**
 * 盲盒订单详细信息
 */
data class OrderData(
    /**
     * 盲盒订单号
     */
    @SerializedName("order_id")
    val orderId: String,

    /**
     * 支付宝支付链接
     */
    @SerializedName("pay_url")
    val payUrl: String,

    /**
     * 实际支付金额 (目前固定为 19.9)
     */
    @SerializedName("pay_amount")
    val payAmount: Double,

    /**
     * 盲盒商品ID
     */
    @SerializedName("product_id")
    val productId: String,

    /**
     * 盲盒商品名称
     */
    @SerializedName("product_name")
    val productName: String,

    /**
     * 支付成功后是否直接开启
     */
    @SerializedName("open_immediately")
    val openImmediately: Boolean
)

/**
 * 订单详细信息
 */
data class OrderDetail(
    @SerializedName("_id")
    val id: String,

    @SerializedName("order_id")
    val orderId: String,

    /**
     * 订单支付状态: pending(待支付), paid(已支付), failed(支付失败)
     */
    @SerializedName("status")
    val status: String,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("pay_amount")
    val payAmount: Double,

    @SerializedName("product_type")
    val productType: String,

    @SerializedName("product_id")
    val productId: String,

    @SerializedName("product_name")
    val productName: String,

    /**
     * 盲盒开奖状态: pending(未开始), processing(处理中), completed(已完成)
     */
    @SerializedName("reward_status")
    val rewardStatus: String,

    /**
     * 开盒时间，秒级时间戳；未开奖时可能为 null
     */
    @SerializedName("opened_at")
    val openedAt: Long?,

    @SerializedName("paid_at")
    val paidAt: Long,

    @SerializedName("created_at")
    val createdAt: Long,

    @SerializedName("updated_at")
    val updatedAt: Long,

    /**
     * 开奖结果详情；未开奖时为 null
     */
    @SerializedName("blind_box_result")
    val blindBoxResult: BlindBoxResult?
)

/**
 * 具体的开奖结果信息
 */
data class BlindBoxResult(
    @SerializedName("product_id")
    val productId: String,

    @SerializedName("product_name")
    val productName: String,

    @SerializedName("rule_version")
    val ruleVersion: String,

    /**
     * 实际获得的仙玉数量
     */
    @SerializedName("reward_jade")
    val rewardJade: Int,

    /**
     * 命中的概率区间标识
     */
    @SerializedName("range_key")
    val rangeKey: String,

    /**
     * 命中的区间展示文案 (例如 "2500 ~ 3500")
     */
    @SerializedName("range_label")
    val rangeLabel: String,

    @SerializedName("range_min")
    val rangeMin: Int,

    @SerializedName("range_max")
    val rangeMax: Int,

    /**
     * 命中区间的概率 (例如 0.35)
     */
    @SerializedName("range_probability")
    val rangeProbability: Double,

    /**
     * 命中区间的概率展示文案 (例如 "35%")
     */
    @SerializedName("range_probability_text")
    val rangeProbabilityText: String,

    @SerializedName("range_mean_jade")
    val rangeMeanJade: Int,

    /**
     * 区间说明
     */
    @SerializedName("description")
    val description: String,

    /**
     * 开盒时间
     */
    @SerializedName("opened_at")
    val openedAt: Long,

    /**
     * 盲盒开奖记录ID
     */
    @SerializedName("record_id")
    val recordId: String
)
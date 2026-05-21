package com.example.ttai.state

import android.bluetooth.BluetoothDevice
import com.example.ttai.base.MviState
import com.example.ttai.bean.AiSettingsData
import com.example.ttai.bean.AppUpdateInfo
import com.example.ttai.bean.BlindBoxDataResponse
import com.example.ttai.bean.BlindBoxInfo
import com.example.ttai.bean.BlindBoxResult
import com.example.ttai.bean.BranchItem
import com.example.ttai.bean.Character
import com.example.ttai.bean.ChatModelItem
import com.example.ttai.bean.CollectItem
import com.example.ttai.bean.Conversation
import com.example.ttai.bean.ConversationsData
import com.example.ttai.bean.FollowsItem
import com.example.ttai.bean.InvitationInfo
import com.example.ttai.bean.MembershipInfo
import com.example.ttai.bean.MembershipItem
import com.example.ttai.bean.Message
import com.example.ttai.bean.NotificationsEntity
import com.example.ttai.bean.OwnedItem
import com.example.ttai.bean.PayHistoryItem
import com.example.ttai.bean.PersonalityTag
import com.example.ttai.bean.ReplaceChatSlotResponse
import com.example.ttai.bean.ShopItem
import com.example.ttai.bean.UserProfile
import com.example.ttai.bean.UserBalance
import com.example.ttai.bean.RechargePackage
import com.google.gson.annotations.SerializedName

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToMain: Boolean = false,
    val navigateToTag: Boolean = false,
    val smsButtonEnabled: Boolean = true,
    val smsButtonText: String = "Get SMS Code",
    val countdownSeconds: Int = 0,
    // 新增状态字段
    val showVerificationCode: Boolean = false,
    val isNewUser: Boolean = false,
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val sendCodeSuccess: Boolean = false
) : MviState

data class MainState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigateToNext: Boolean = false
) : MviState

data class SplashState(
    val isLoading: Boolean = false,
    val navigateToLogin: Boolean = false,
    val navigateToSeletcTag: Boolean = false,
    val navigateToMain: Boolean = false
) : MviState

data class SelectTagState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val tags: List<PersonalityTag> ?= null,
    val selectedTags: ArrayList<String> = ArrayList(),
    val navigateToNext:  Boolean = false,
    val isUpdatingPreferredTags: Boolean = false,
    val preferredTagsUpdateSuccess: Boolean = false,
    val preferredTagsUpdateError: String? = null
) : MviState

data class OnboardingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasPreferredTags: Boolean = false,
    val preferredTags: List<String> = emptyList(),
    val shouldShowTagSelection: Boolean = false,
    val canSkipToMain: Boolean = false,
    val isOnboardingComplete: Boolean = false
) : MviState

data class MainFragmentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "Main Fragment Content",
    val conversationsData: ConversationsData? = null,
    var addAISizeSuceesss: Boolean? = false,
    var hasUnread: Boolean? = false,
    // 分页与刷新
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 1
) : MviState

data class DreamFragmentState(
     val isLoading: Boolean = false,
     val error: String? = null,
     val content: String = "Dream Fragment Content",
     val currentTabIndex: Int = 0
) : MviState

data class ControlFragmentState(
     val isLoading: Boolean = false,
     val error: String? = null,
     val content: String = "Control Fragment Content",
     val deviceName: String = "XXXXXX",
     val deviceState: String = "已连接",
     val vibrationIntensity: Int = 50,
     val characterId: String ="",
     val modes: List<CollectItem> = emptyList()
) : MviState

data class MyFragmentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String? = "My Fragment Content",
    val userName: String? = "",
    val id: String? = null,
    val ivHeard: String? = null,
    val fairyJade: Int? = null,
    val fairyShells: Int? = null,
    val followersCount: String? = null,
    val followingCount: String? = null,
    val brief_intro: String? = null,
//    val brief_intro: String? = null,

    val myCharacters: List<Character> = emptyList(),
    val isLoadingMyAI: Boolean = false,
    val is_beta_user: Boolean = false,
    val membershipInfo: MembershipInfo? = null,
    val invitationInfo: InvitationInfo? = null,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMoreData: Boolean = true,
    val currentPage: Int = 1
) : MviState

data class MainActivityState(
     val isLoading: Boolean = false,
     val error: String? = null,
     val currentTabIndex: Int = 0,
     val  appUpdateInfo : AppUpdateInfo?= null,
) : MviState

data class ChatState(
    val isLoading: Boolean = false,
    val loadingMessages: String = "",
    val error: String? = null,
    val messages: List<Message> = emptyList(),
    val currentUser: String = "You",
    val isTyping: Boolean = false,
    val lastMessageId: String? = null,
    val unreadCount: Int = 0,
    val isConnected: Boolean = true,
    val character: Character? = null,
    val replaceSlotResult: ReplaceChatSlotResponse? = null,
    val isReplacingSlot: Boolean = false,
    val isLoadingDefaultChat: Boolean = false,
    val isClearMessages: Boolean = false,
    var total_pages: Int = 1,
    var fairyJade: Int = 0,
) : MviState

data class AcquisitionModeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "采集模式默认内容"
) : MviState

data class AIDetailsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "AI 详情默认内容",
    val character: Character? = null,
    val isInChatSlot: Boolean = false,
    val isTogglingSlot: Boolean = false,
    val slotToggleSuccess: Boolean = false,
    val showReplaceDialog: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
) : MviState

data class AISettingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "",
    val isPermanentMemoryEnabled: Boolean = false,
    val isDoubleReplyEnabled: Boolean = false,
    val isUserMember: Boolean = false,
    val profileData: UserProfile? = null,
    val aiSettings: AiSettingsData? = null,
    val currentModelItem: ChatModelItem? = null,
    val selectedModelId: String = "",
    val clearChatSuccess: Boolean = false,
    val userBalance: UserBalance? = null
) : MviState

data class CollectState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val modes: List<CollectItem> = emptyList(),
    val characterId: String = ""
) : MviState

data class RequestModelState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val modes: List<CollectItem> = emptyList(),
    val characterId: String = ""
) : MviState

data class ChatModelListState(
    val isLoading: Boolean = false,
    val selectedModelSuccess: Boolean = false,
    val error: String? = null,
    val modes: List<ChatModelItem> = emptyList()
) : MviState
data class BranchManagerState(
    val isLoading: Boolean = false,
    val selectedModelSuccess: Boolean = false,
    val initList: Boolean = false,
    val error: String? = null,
    val conversation_id: String? = null,
    val character_id: String? = null,
    val modes: List<BranchItem> = emptyList()
) : MviState

data class EditState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "编辑默认内容"
) : MviState

data class MyChatSettingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    var nickname: String?= "",
    val gender: String?= null,
    val genderCN: String?= null,
    val identity: String?= null,
    var personality_description: String?= "",
    val updated_at: Long?= null,
    val saveSuccess: Boolean?= false
) : MviState

data class SwitchModeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "编辑默认内容"
) : MviState

data class RemoteAssistanceState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "远程协助默认内容"
) : MviState

data class SearchBluetoothState(
      val isLoading: Boolean = false,
      val error: String? = null,
      val content: String = "",
      val searchBluetooth: Boolean = false,
      val devices: List<BluetoothDevice> = emptyList()
) : MviState

data class SettingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "设置默认内容"
) : MviState

data class SynthesizeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val modes: List<CollectItem> = emptyList(),
    val characterId: String = ""
) : MviState

data class SynthesizeFragmentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "综合内容",
    val items: List<Character>? = null,
    val currentPage: Int = 1,
    val sortBy: String = "",
    val hasMoreData: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false
) : MviState
data class ShopFragmentState(
    val isLoading: Boolean = false,
    val shopItems: List<ShopItem> = emptyList(),
    val errorMessage: String? = null,
    val purchaseSuccess: Boolean? = null,
    val totalCount: Int = 0
) : MviState

data class ShopToyFragmentState(
    val isLoading: Boolean = false,
    val jdLink: String="",
    val taobaoLink: String="",
    val errorMessage: String? = null,
    val totalCount: Int = 0
) : MviState

data class ShopBeautifyFragmentState(
    val isLoading: Boolean = false,
    val shopItems: List<ShopItem> = emptyList(),
    val errorMessage: String? = null,
    val purchaseSuccess: Boolean? = null,
    val totalCount: Int = 0
) : MviState

data class ShopWroldBookFragmentState(
    val isLoading: Boolean = false,
    val shopItems: List<ShopItem> = emptyList(),
    val errorMessage: String? = null,
    val purchaseSuccess: Boolean? = null,
    val totalCount: Int = 0
) : MviState

data class FeaturedFragmentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String ?= null,
    val openingLine: String ?= null,
    val briefIntro: String ?= null,
    val userName: String? =  "",
    val userAvatar: String? = "",
    val currentIndex: Int = 0,
    val totalCount: Int = 0,
    val addAIButtonText: String = "入梦",
    val isInDream: Boolean = false,
    val isAddingAI: Boolean = false,
    val currentCharacterId: String? = "",
    val conversations: List<Conversation> = emptyList(),
    val showReplaceDialog: Boolean = false,
    val featuredCharacters: List<Character> = emptyList(),
    val inChatSlots: Boolean = false,
    val conversationId: String ?= "",
    val currentCharacter: Character?= null,
    // 分页相关字段
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val hasMoreData: Boolean = true
) : MviState

data class MembershipState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSubscriptionSuccessful: Boolean = false,
    val selectedPlan: String = "",
    val plans: List<String> = emptyList(),
    val model1Items: List<MembershipItem> = emptyList(),
    val model2Items: List<MembershipItem> = emptyList(),
    val selectedModel1Item: String = "",
    val selectedModel2Item: String = "",
    val model1Title: String = "",
    val model2Title: String = "",
    val selectedItemId: String = "", // 全局选中的item ID
    val selectedModelType: String = "", // 选中的模型类型
    val userName: String? = "",
    val vipTag: String ?= "",
    val expireTime: String = "",
    val isLoadingUserProfile: Boolean = false,
    val userProfile:  UserProfile? = null,
    val membershipInfo: MembershipInfo? = null
) : MviState

data class ChargeMoneyState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: String = "",
    val promotionText: String = "",
    val chargeSuccess: Boolean = false,
    val rechargePackages: List<RechargePackage> = emptyList(),
    val blindBoxDataResponse:BlindBoxDataResponse? = null,
    val blindBoxResult: BlindBoxResult? = null,
    val isLoadingPackages: Boolean = false,
    val isLoadingBlindBoxInfo: Boolean = false,
    val payUrl: String? = null,
    val orderId: String? = null,
    val isBuyBlindBox: Boolean? = false,
    val openImmediately: Boolean? = false,
    val isShowBlindBox: Boolean? = false
) : MviState

data class AIBriefState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val characterId: String ?= "",
    val characterName: String? = "",
    val characterAvatar: String? = null,
    val characterDescription: String ?= "",
    val briefIntro: String ?= "",
    val openingLine: String ?= "",
    val personalityTags: List<String> = emptyList(),
    val navigateToChat: Boolean = false,
    val isAddingFriend: Boolean = false,
    val addFriendSuccess: Boolean = false,
    val conversations: List<Conversation> = emptyList(),
    val showReplaceDialog: Boolean = false,
    val inChatSlots: Boolean = false,
    val conversationId: String ?= "",
    val toCreatAI: Boolean ?= false
) : MviState

data class SearchState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<Character> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val hasMoreData: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false
) : MviState

data class FollowersListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<FollowsItem> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val hasMoreData: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false
) : MviState

data class FollowingListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<FollowsItem> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val hasMoreData: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false
) : MviState

/**
 * 商店Activity的状态
 */
data class ShopActivityState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalCount: Int = 0,
    val currentTabIndex: Int = 0
) : MviState


data class ShopOwnedFragmentState(
    val isLoading: Boolean = false,
    val ownedItems: List<OwnedItem> = emptyList(),
    val errorMessage: String? = null,
    val purchaseMessage: String? = null,
    val totalCount: Int = 0
) : MviState

data class UserHomeState(
    val isLoading: Boolean = false,
    val can_follow: Boolean? = false,
    val profile: UserProfile?=null,
    val errorMessage: String? = null,
    val purchaseMessage: String? = null,
    val is_follow: Boolean? = null,
    val items: List<Character>? = null,
    val totalCount: Int = 0,
    val currentPage: Int = 0,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val hasMoreData: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false
) : MviState


data class EditeBriefState(
    val isLoading: Boolean = false,
    val brief: String = "",
    val briefLength: Int = 0,
    val isBriefValid: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) : MviState


data class EditeMemoryBookState(
    val isLoading: Boolean = false,
    val content: String = "",
    val character_id: String = "",
    val contentLength: Int = 0,
    val isContentValid: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) : MviState


data class PayHistoryState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val payHistoryList: List<PayHistoryItem> = emptyList(),
    val hasMoreData: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    var total_pages: Int = 1,
) : MviState

data class EditeMessageState(
    val isLoading: Boolean = false,
    val currentName: String? = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) : MviState


data class NotifyListState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val list: List<NotificationsEntity>? = emptyList(),
    val hasMoreData: Boolean = true,
    val currentPage: Int = 1,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false
) : MviState


data class NotifyDetailsState(
    val isLoading: Boolean = false,
    val currentName: String? = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
) : MviState

data class VideoCallState(
    val isConnecting: Boolean = false,
    val isPlaying: Boolean = false,
    val isMicrophoneOpen: Boolean = true,
    val playStatus: String = "",

) : MviState

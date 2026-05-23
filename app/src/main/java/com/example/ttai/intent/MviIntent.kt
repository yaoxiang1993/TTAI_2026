package com.example.ttai.intent

import com.example.ttai.base.MviIntent
import com.example.ttai.bean.AiModel
import com.example.ttai.bean.BranchItem
import com.example.ttai.bean.Character
import com.example.ttai.bean.ChatModelItem
import com.example.ttai.bean.CollectItem
import com.example.ttai.bean.Conversation
import com.example.ttai.bean.Message
import com.example.ttai.bean.NotificationsEntity

sealed class LoginIntent : MviIntent {
    // 新的认证接口
    data class OneClickAuth(val phone: String) : LoginIntent()
    data class SendCode(val phone: String) : LoginIntent()
    data class VerifyCodeAuth(val phone: String, val code: String,val invitationCode:String) : LoginIntent()

}

sealed class MainIntent : MviIntent {
    object EnterClicked : MainIntent()
}

sealed class SplashIntent : MviIntent {
    object OtherLogin : SplashIntent()
    object Login : SplashIntent()
}

sealed class SelectTagIntent : MviIntent {
    object Initialize : SelectTagIntent()
    data class SelectTag(val tag: String) : SelectTagIntent()
    object ConfirmSelection : SelectTagIntent()
    object UpdatePreferredTags : SelectTagIntent()
}

sealed class OnboardingIntent : MviIntent {
    object CheckOnboardingStatus : OnboardingIntent()
    object SkipToMain : OnboardingIntent()
    object ShowTagSelection : OnboardingIntent()
}


sealed class MainFragmentIntent : MviIntent {
    object Initialize : MainFragmentIntent()
    data class LoadChatList(  val page: Int = 1, val limit: Int = 20) : MainFragmentIntent()
    object AddAISize : MainFragmentIntent()
    object RefreshData : MainFragmentIntent()
    object LoadMoreData : MainFragmentIntent()
}
sealed class MainActivityIntent : MviIntent {
    data class SwitchTab(val index: Int) : MainActivityIntent()
    object versionCheck : MainActivityIntent()
    object ClearUpdateInfo : MainActivityIntent()
}

sealed class DreamFragmentIntent : MviIntent {
    object Initialize : DreamFragmentIntent()
    data class SwitchTab(val index: Int) : DreamFragmentIntent()
}

sealed class ControlFragmentIntent : MviIntent {
    object Initialize : ControlFragmentIntent()
    object AddDevice : ControlFragmentIntent()

    data class SetFavoriteState(val collectItem: CollectItem) : ControlFragmentIntent()
    data class UpdateVibrationIntensity(val intensity: Int) : ControlFragmentIntent()
}

sealed class MyFragmentIntent : MviIntent {
    object Initialize : MyFragmentIntent()
    data class UpdateUserName(val newName: String) : MyFragmentIntent()
    data class pushPin(val characterId: String?) : MyFragmentIntent()
    data class pushUnPin(val characterId: String?) : MyFragmentIntent()
    data class UpdateAvatar(val avatarUrl: String) : MyFragmentIntent()
    object getCurrencyBalance : MyFragmentIntent()
    object LoadMyAICharacters : MyFragmentIntent()
    data class DeleteMyAICharacter(val characterId: String?) : MyFragmentIntent()
    object RefreshData : MyFragmentIntent()
    object LoadMoreData : MyFragmentIntent()
}

/**
 * 聊天界面的 MVI 意图，定义用户在 ChatActivity 中的操作
 */
sealed class ChatIntent : MviIntent {
    /**
     * 加载特定会话的消息列表
     */
    data class  LoadMessages(val conversationId: String) : ChatIntent()

    /**
     * 发送新消息
     * @param content 消息内容
     */
    data class SendMessage(val content: String) : ChatIntent()


    /**
     * 清除历史信息
     */
    object ClearMessage : ChatIntent()
    
    /**
     * 开始输入状态
     */
    object StartTyping : ChatIntent()
    
    /**
     * 停止输入状态
     */
    object StopTyping : ChatIntent()
    /**
     * 加载角色详情
     * @param characterId 角色ID
     */
    data class LoadCharacterDetail(val characterId: String) : ChatIntent()
    
    /**
     * 加载默认角色聊天历史
     * @param page 页码
     * @param limit 每页限制
     */
    data class LoadDefaultCharacterMessages(val page: Int = 1, val limit: Int = 50) : ChatIntent()

    /** 加载更多历史消息（上一页） */
    object LoadMoreMessages : ChatIntent()
    /** 重塑消息 */
    data class RollbackMessages(val message: Message?) : ChatIntent()
    /** 删除 */
    data class DeleteMessages(val message: Message?) : ChatIntent()
    /** 重新生成消息 */
    object RegenerateMessage : ChatIntent()

    data class UpdateMessage(val content: String,val messageId: String?) : ChatIntent()

    object getCurrencyBalance : ChatIntent()

}
/**
 * 采集模式界面的 MVI 意图，定义用户操作
 */
sealed class AcquisitionModeIntent : MviIntent {
    /**
     * 加载采集模式数据
     */
    object LoadData : AcquisitionModeIntent()

    /**
     * 执行采集操作
     * @param action 具体操作描述
     */
    data class PerformAction(val action: String) : AcquisitionModeIntent()
}
sealed class AIDetailsIntent : MviIntent {
    object LoadData : AIDetailsIntent()
    data class ToggleChatSlot(val characterId: String) : AIDetailsIntent()
    data class ReplaceAI(val conversation: Conversation) : AIDetailsIntent()
    object HideReplaceDialog : AIDetailsIntent()

    data class loadCreator(val characterId: String) : AIDetailsIntent()
}

sealed class AISettingIntent : MviIntent {
    object LoadData : AISettingIntent()
    object LoadUserProfile : AISettingIntent()
    object LoadAiSettings : AISettingIntent()
    data class PerformAction(val action: String) : AISettingIntent()
    object ClearChatHistory : AISettingIntent()
    data class TogglePermanentMemory(val isEnabled: Boolean) : AISettingIntent()
    data class ToggleDoubleReply(val isEnabled: Boolean) : AISettingIntent()
    data class UpdateSelectedModel(val model: AiModel) : AISettingIntent()
    object LoadPermanentMemoryStatus : AISettingIntent()
}

sealed class CollectIntent : MviIntent {
    object Initialize : CollectIntent()
    data class SetFavoriteState(val collectItem: CollectItem) : CollectIntent()
}
sealed class RequestModelIntent : MviIntent {
    object Initialize : RequestModelIntent()
    data class SetFavoriteState(val collectItem: CollectItem) : RequestModelIntent()
}
sealed class ChatModelListIntent : MviIntent {
    object Initialize : ChatModelListIntent()
    data class UpdateSelectedModel(val model: ChatModelItem) : ChatModelListIntent()
}
sealed class BranchManagerIntent : MviIntent {
    data class Initialize(val character_id: String? ) : BranchManagerIntent()
    data class CreateBranch(val character_id: String? ) : BranchManagerIntent()
    data class UpdateSelectedModel(val character_id: String? ,val model: BranchItem) : BranchManagerIntent()
    data class pushPin(val branch_id: String?) : BranchManagerIntent()
    data class pushUnPin(val branch_id: String?) : BranchManagerIntent()
    data class delete(val branch_id: String?) : BranchManagerIntent()
    data class editeName(val branch_id: String?,var editeName : String?) : BranchManagerIntent()

}

sealed class EditIntent : MviIntent {
    object LoadData : EditIntent()
    data class PerformAction(val action: String) : EditIntent()
}

sealed class MyChatSettingIntent : MviIntent {
    data class  Initialize(val character_id: String?) : MyChatSettingIntent()

    data class SelectGender(val gender: String) : MyChatSettingIntent()
    data class  SaveChatSetting(val character_id: String?,val nickname: String?,val identity: String?,val gender: String?,val personality_description: String?) : MyChatSettingIntent()
}

sealed class SwitchModeIntent : MviIntent {
    object LoadData : SwitchModeIntent()
}

sealed class RemoteAssistanceIntent : MviIntent {
    object LoadData : RemoteAssistanceIntent()
    data class PerformAction(val action: String) : RemoteAssistanceIntent()
}

sealed class SearchBluetoothIntent : MviIntent {
    object SearchBluetooth : SearchBluetoothIntent()
    object StopScan : SearchBluetoothIntent()
}

sealed class SettingIntent : MviIntent {
    object LoadData : SettingIntent()
    data class PerformAction(val action: String) : SettingIntent()
}

sealed class SynthesizeIntent : MviIntent {
    object LoadModes : SynthesizeIntent()
    data class PlayMode(val modeId: String) : SynthesizeIntent()
    data class ToggleCollect(val modeId: String) : SynthesizeIntent()
}

sealed class SynthesizeFragmentIntent : MviIntent {
    data class Initialize(val sortBy: String,val sortOrder: String) : SynthesizeFragmentIntent()
    data class RefreshData(val sortBy: String,val sortOrder: String) : SynthesizeFragmentIntent()
    data class LoadMoreData(val sortBy: String,val sortOrder: String) : SynthesizeFragmentIntent()
}

sealed class FeaturedFragmentIntent : MviIntent {
    object Initialize : FeaturedFragmentIntent()
    object LoadNextData : FeaturedFragmentIntent()
    object LoadPreviousData : FeaturedFragmentIntent()
    object AddAI : FeaturedFragmentIntent()
    data class ReplaceAI(val conversation: Conversation) : FeaturedFragmentIntent()
    object HideReplaceDialog : FeaturedFragmentIntent()
    data class ToggleChatSlot(val characterId: String) : FeaturedFragmentIntent()
}

sealed class MembershipIntent : MviIntent {
    object Initialize : MembershipIntent()
    object LoadUserProfile : MembershipIntent()
    data class Subscribe(val planType: String, val planName: String) : MembershipIntent()
    data class SelectModel1Item(val itemId: String) : MembershipIntent()
    data class SelectModel2Item(val itemId: String) : MembershipIntent()
    data class SelectItem(val itemId: String, val modelType: String) : MembershipIntent()
}

sealed class ChargeMoneyIntent : MviIntent {
    object LoadRechargePackages : ChargeMoneyIntent()
    object LoadBlindBoxInfo : ChargeMoneyIntent()
    object rechargeBlindBox : ChargeMoneyIntent()
    data class Charge(val amount: Int) : ChargeMoneyIntent()
    object ClearPayUrl : ChargeMoneyIntent()
    data class BlindBoxOrderResult (val blindBoxOrderId: String): ChargeMoneyIntent()

    object ClearShowBlindBox : ChargeMoneyIntent()
}

sealed class AIBriefIntent : MviIntent {
    data class Initialize(val character: Character) : AIBriefIntent()
    object NavigateToChat : AIBriefIntent()
    data class ReplaceAI(val conversation: Conversation) : AIBriefIntent()
    data class ToggleChatSlot(val characterId: String?) : AIBriefIntent()
    object HideReplaceDialog : AIBriefIntent()
    data class ToCreatAI(val character: Character?) : AIBriefIntent()
}

sealed class SearchIntent : MviIntent {
    object Initialize : SearchIntent()
    data class PerformSearch(val query: String) : SearchIntent()
    object ClearSearch : SearchIntent()
    object LoadMoreData : SearchIntent()
}
sealed class FollowersListIntent : MviIntent {
    object Initialize : FollowersListIntent()
    data class PerformSearch(val query: String) : FollowersListIntent()
    object ClearSearch : FollowersListIntent()
    object LoadMoreData : FollowersListIntent()
    data class Follow(val target_user_id: String?) : FollowersListIntent()
    data class UnFollow(val target_user_id: String?) : FollowersListIntent()
}
sealed class FollowingListIntent : MviIntent {
    object Initialize : FollowingListIntent()
    data class PerformSearch(val query: String) : FollowingListIntent()
    object ClearSearch : FollowingListIntent()
    object LoadMoreData : FollowingListIntent()
    data class Follow(val target_user_id: String?) : FollowingListIntent()
    data class UnFollow(val target_user_id: String?) : FollowingListIntent()
}
sealed class ShopActivityIntent : MviIntent {
    object Initialize : ShopActivityIntent()
    data class SwitchTab(val index: Int) : ShopActivityIntent()
}
sealed class ShopFragmentIntent : MviIntent {
    object Initialize : ShopFragmentIntent()
    data class PurchaseItem(val itemId: String, val quantity: Int = 1) : ShopFragmentIntent()
}
sealed class ShopToyFragmentIntent : MviIntent {
    object Initialize : ShopToyFragmentIntent()
}
sealed class ShopBeautifyFragmentIntent : MviIntent {
    object Initialize : ShopBeautifyFragmentIntent()
    data class PurchaseItem(val itemId: String, val quantity: Int = 1) : ShopBeautifyFragmentIntent()
}
sealed class ShopWroldBookFragmentIntent : MviIntent {
    object Initialize : ShopWroldBookFragmentIntent()
    data class PurchaseItem(val itemId: String, val quantity: Int = 1) : ShopWroldBookFragmentIntent()
}
sealed class ShopOwnedFragmentIntent : MviIntent {
    object Initialize : ShopOwnedFragmentIntent()
}

sealed class UserHomeIntent : MviIntent {
    data class Initialize(val userId: String) : UserHomeIntent()
    object FollowSwitch : UserHomeIntent()
    data class PerformSearch(val query: String) : UserHomeIntent()
    object ClearSearch : UserHomeIntent()
}

sealed class EditeBriefIntent : MviIntent {
    object LoadData : EditeBriefIntent()
    data class UpdateName(val brief: String) : EditeBriefIntent()
    object SaveName : EditeBriefIntent()
    object Back : EditeBriefIntent()
}

sealed class EditeMemoryBookIntent : MviIntent {
    data class LoadData(val character_id: String) : EditeMemoryBookIntent()
    data class UpdateMemoryBook(val content: String) : EditeMemoryBookIntent()

    object SaveMemoryBook : EditeMemoryBookIntent()
    object Back : EditeMemoryBookIntent()
}

sealed class PayHistoryListIntent : MviIntent {
    data class Initialize(val year:Int,val month : Int) : PayHistoryListIntent()
    data class LoadMoreData(val year:Int,val month : Int) : PayHistoryListIntent()
}
sealed class NotifyListIntent : MviIntent {
    object Initialize : NotifyListIntent()

    object RefreshData : NotifyListIntent()
    object LoadMoreData : NotifyListIntent()
}
sealed class NotifyDetailsIntent : MviIntent {
    data class notificationsMarkRead(val notifyEntity: NotificationsEntity?) : NotifyDetailsIntent()
}
sealed class VideoCallIntent : MviIntent {
    object ConnectVideo : VideoCallIntent()
}

sealed class VoiceConfigIntent : MviIntent {
    data class Initialize(val character_id: String?) : VoiceConfigIntent()
    object SaveVoiceConfig  : VoiceConfigIntent()
    data class SelectItem(val selectedCode: String) : VoiceConfigIntent()
}
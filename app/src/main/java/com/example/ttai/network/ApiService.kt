package com.example.ttai.network

import android.R
import com.example.ttai.bean.AddChatSlotRequest
import com.example.ttai.bean.AddChatSlotResponse
import com.example.ttai.bean.AiSettingsData
import com.example.ttai.bean.AllocatePermanentMemoryRequest
import com.example.ttai.bean.AllocatePermanentMemoryResponse
import com.example.ttai.bean.ApiResponse
import com.example.ttai.bean.AppUpdateInfo
import com.example.ttai.bean.AuthData
import com.example.ttai.bean.BlindBoxDataResponse
import com.example.ttai.bean.BranchManagerResponse
import com.example.ttai.bean.BranchResponse
import com.example.ttai.bean.BuyExtraSlotData
import com.example.ttai.bean.CharacterDetailData
import com.example.ttai.bean.CharacterListResponse
import com.example.ttai.bean.CharacterSearchRequest
import com.example.ttai.bean.CharacterSearchResponse
import com.example.ttai.bean.ChatModelsResponse
import com.example.ttai.bean.ClearChatData
import com.example.ttai.bean.ComprehensiveCharactersResponse
import com.example.ttai.bean.ConversationSettingsRequest
import com.example.ttai.bean.ConversationSettingsResponse
import com.example.ttai.bean.ConversationsData
import com.example.ttai.bean.CreateBranchRequest
import com.example.ttai.bean.CreateCharacterRequest
import com.example.ttai.bean.CreateOrderData
import com.example.ttai.bean.CreateOrderRequest
import com.example.ttai.bean.CurrencyBalanceData
import com.example.ttai.bean.DeallocatePermanentMemoryRequest
import com.example.ttai.bean.DeallocatePermanentMemoryResponse
import com.example.ttai.bean.DefaultCharacterChatResponse
import com.example.ttai.bean.DeleteCharacterData
import com.example.ttai.bean.DeleteMessageRequest
import com.example.ttai.bean.DeletedMessageResponse
import com.example.ttai.bean.EditeBranchRequest
import com.example.ttai.bean.FavoriteRequest
import com.example.ttai.bean.FavoriteResponse
import com.example.ttai.bean.FeaturedCharactersResponse
import com.example.ttai.bean.FollowRequest
import com.example.ttai.bean.FollowResponse
import com.example.ttai.bean.FollowsItem
import com.example.ttai.bean.FollowsResponse
import com.example.ttai.bean.GetCharactersRequest
import com.example.ttai.bean.ImageUploadResponse
import com.example.ttai.bean.MarkReadRequest
import com.example.ttai.bean.MarkReadResponse
import com.example.ttai.bean.MembershipPackageGroup
import com.example.ttai.bean.MemoryBookRequest
import com.example.ttai.bean.MemoryBookResponse
import com.example.ttai.bean.MessageRequest
import com.example.ttai.bean.MessagesData
import com.example.ttai.bean.ModelsItemsResponse
import com.example.ttai.bean.MyAIListResponse
import com.example.ttai.bean.NotificationsResponse
import com.example.ttai.bean.OnboardingStatusData
import com.example.ttai.bean.OneClickAuthRequest
import com.example.ttai.bean.OrderData
import com.example.ttai.bean.OrderDetail
import com.example.ttai.bean.OrderListData
import com.example.ttai.bean.OwnedItemsResponse
import com.example.ttai.bean.PayHistoryResponse
import com.example.ttai.bean.PermanentMemoryStatusResponse
import com.example.ttai.bean.PinBranchRequest
import com.example.ttai.bean.PinRequest
import com.example.ttai.bean.PinResponse
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.PurchaseMemoryRequest
import com.example.ttai.bean.PurchaseRequest
import com.example.ttai.bean.PurchaseResponse
import com.example.ttai.bean.RechargeCurrencyRequest
import com.example.ttai.bean.RechargeCurrencyResponse
import com.example.ttai.bean.RechargePackagesData
import com.example.ttai.bean.RegenerateMessageResponse
import com.example.ttai.bean.RemoveChatSlotRequest
import com.example.ttai.bean.RemoveChatSlotResponse
import com.example.ttai.bean.ReplaceChatSlotRequest
import com.example.ttai.bean.ReplaceChatSlotResponse
import com.example.ttai.bean.RollbackMessageResponse
import com.example.ttai.bean.SaveVoiceConfigRequest
import com.example.ttai.bean.SendCodeData
import com.example.ttai.bean.SendCodeRequest
import com.example.ttai.bean.SendMessageData
import com.example.ttai.bean.SendMessageRequest
import com.example.ttai.bean.ShopItemsResponse
import com.example.ttai.bean.ShopLinkResponse
import com.example.ttai.bean.SwitchBranchRequest
import com.example.ttai.bean.TagsData
import com.example.ttai.bean.UnreadCountResponse
import com.example.ttai.bean.UpdateAiModelRequest
import com.example.ttai.bean.UpdateAiModelResponse
import com.example.ttai.bean.UpdateMessageRequest
import com.example.ttai.bean.UpdateMessageResponse
import com.example.ttai.bean.UpdatePreferredTagsRequest
import com.example.ttai.bean.UpdateProfileRequest
import com.example.ttai.bean.UpgradeDoubleReplyData
import com.example.ttai.bean.UpgradePremiumRequest
import com.example.ttai.bean.UpgradePremiumResponse
import com.example.ttai.bean.VerifyCodeAuthRequest
import com.example.ttai.bean.VoiceConfigData
import com.example.ttai.bean.WalletData
import retrofit2.http.*

interface ApiService {
    // ==================== 认证模块 (/auth) ====================
    /** 一键登录注册 */
    @POST("api/auth/one-click-auth")
    suspend fun oneClickAuth(@Body request: OneClickAuthRequest): ApiResponse<AuthData>

    /** 发送验证码 */
    @POST("api/auth/send-code")
    suspend fun sendCode(@Body request: SendCodeRequest): ApiResponse<SendCodeData>

    /** 验证码登录注册 */
    @POST("api/auth/verify-code-auth")
    suspend fun verifyCodeAuth(@Body request: VerifyCodeAuthRequest): ApiResponse<AuthData>

    // ==================== AI角色管理模块 (/api/characters) ====================
    /** 获取角色列表 */
    @POST("api/characters")
    suspend fun getCharacters(@Body request: GetCharactersRequest): ApiResponse<CharacterListResponse>

    /** 获取单个角色详情 */
    @GET("api/characters/{character_id}")
    suspend fun getCharacterDetail(@Path("character_id") characterId: String): ApiResponse<CharacterDetailData>

    /** 获取性格标签列表 */
    @GET("api/characters/tags")
    suspend fun getPersonalityTags(): ApiResponse<TagsData>

    /** 创建自定义角色 */
    @POST("api/characters/create_character")
    suspend fun createCharacter(@Body request: CreateCharacterRequest): ApiResponse<Any>

    /** 获取精选角色列表 */
    @GET("api/characters/featured")
    suspend fun getFeaturedCharacters(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("include_unlimited") is_unlimited: Boolean = false
    ): ApiResponse<FeaturedCharactersResponse>

    /** 获取综合角色列表
     *
     * #### 2. 按热度降序排序（热门榜单）
     * ```bash
     * GET /characters/comprehensive?sort_by=popularity&sort_order=-1&page=1&limit=20
     * ```
     *
     * #### 3. 按热度升序排序（冷门角色）
     * ```bash
     * GET /characters/comprehensive?sort_by=popularity&sort_order=1&page=1&limit=20
     * ```
     *
     * #### 4. 最新创建的角色
     * ```bash
     * GET /characters/comprehensive?sort_by=created_at&sort_order=-1&page=1&limit=20
     * ```
     *
     * #### 5. 最近更新的角色
     * ```bash
     * GET /characters/comprehensive?sort_by=updated_at&sort_order=-1&page=1&limit=20
     * ```
     *
     * #### 6. 按名称字母顺序
     * ```bash
     * GET /characters/comprehensive?sort_by=name&sort_order=1&page=1&limit=20
     *
     * */
    @GET("api/characters/comprehensive")
    suspend fun getComprehensiveCharacters(
        @QueryMap params: Map<String, String>
    ): ApiResponse<ComprehensiveCharactersResponse>

    /** 购买永久记忆功能 */
    @POST("api/characters/purchase_permanent_memory")
    suspend fun purchasePermanentMemory(@Body request: PurchaseMemoryRequest): ApiResponse<Any>

    /** 购买额外角色槽位 */
    @POST("api/characters/buy-extra-slot")
    suspend fun buyExtraSlot(): ApiResponse<BuyExtraSlotData>

    // ==================== 聊天模块 (/api/chat) ====================
    /** 获取对话列表 */
    @GET("api/chat/conversations")
    suspend fun getConversations(): ApiResponse<ConversationsData>

    /** 获取对话消息 */
    @GET("api/chat/messages/{conversation_id}")
    suspend fun getMessages(
        @Path("conversation_id") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): ApiResponse<MessagesData>

    /** 发送消息 */
    @POST("api/chat/send_message")
    suspend fun sendMessage(@Body request: SendMessageRequest): ApiResponse<SendMessageData>

    /** 清空指定角色聊天记录 */
    @POST("api/chat/clear/{character_id}")
    suspend fun clearCharacterChat(@Path("character_id") characterId: String): ApiResponse<ClearChatData>

    /** 清空所有聊天记录 */
    @POST("api/chat/clear-all")
    suspend fun clearAllChats(): ApiResponse<Any>

    /** 替换聊天槽位中的角色 */
    @POST("api/chat/slots/replace")
    suspend fun replaceChatSlot(@Body request: ReplaceChatSlotRequest): ApiResponse<ReplaceChatSlotResponse>

    /** 添加角色到聊天槽位 */
    @POST("api/chat/slots/add")
    suspend fun addChatSlot(@Body request: AddChatSlotRequest): ApiResponse<AddChatSlotResponse>

    /** 从聊天槽位移除角色 */
    @POST("api/chat/slots/remove")
    suspend fun removeChatSlot(@Body request: RemoveChatSlotRequest): ApiResponse<RemoveChatSlotResponse>

    /** 获取默认角色聊天历史 */
    @GET("api/chat/default-character/messages")
    suspend fun getDefaultCharacterMessages(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): ApiResponse<DefaultCharacterChatResponse>

    // ==================== 用户管理模块 (/api/user) ====================
    /** 获取用户资料 */
    @GET("api/user/profile")
    suspend fun getUserProfile(): ApiResponse<ProfileData>

    /** 根据userid获取用户信息 */
    @GET("api/user/public_profile/{user_id}")
    suspend fun getUserPublicProfile( @Path("user_id") userId: String = ""): ApiResponse<ProfileData>

    /** 更新用户资料 */
    @PUT("api/user/profile")
    suspend fun updateUserProfile(@Body request: UpdateProfileRequest): ApiResponse<Any>

    /** 获取钱包信息 */
    @GET("api/user/wallet")
    suspend fun getWallet(): ApiResponse<WalletData>

    /** 升级会员 */
    @POST("api/user/upgrade_membership")
    suspend fun upgradePremium(@Body request: UpgradePremiumRequest): ApiResponse<UpgradePremiumResponse>

    /** 充值仙玉 */
    @POST("api/user/wallet/alipay-recharge")
    suspend fun rechargeCurrency(@Body request: RechargeCurrencyRequest): ApiResponse<RechargeCurrencyResponse>

    /** 创建盲盒订单 */
    @POST("api/user/wallet/alipay-blind-box")
    suspend fun rechargeBlindBox( ): ApiResponse<OrderData>

    /** 获取充值套餐列表 */
    @GET("api/user/recharge-packages")
    suspend fun getRechargePackages(): ApiResponse<RechargePackagesData>
    /** 获取盲盒规则、概率和当前中奖情况 */
    @GET("api/shop/blind-box/info")
    suspend fun getBlindBoxInfo(): ApiResponse<BlindBoxDataResponse>

    /** 获取货币余额 */
    @GET("api/user/currency_balance")
    suspend fun getCurrencyBalance(): ApiResponse<CurrencyBalanceData>

    /** 获取会员套餐列表 */
    @GET("api/user/membership-packages")
    suspend fun getMembershipPackages(): ApiResponse<List<MembershipPackageGroup>>

    /** 获取AI设置 */
    @GET("api/user/ai_settings")
    suspend fun getAiSettings(): ApiResponse<AiSettingsData>

    /** 更新AI模型偏好 */
    @POST("api/user/update_ai_model_preference")
    suspend fun updateAiModelPreference(@Body request: UpdateAiModelRequest): ApiResponse<UpdateAiModelResponse>

    /** 升级双倍回复权益 */
    @POST("api/user/upgrade-double-reply")
    suspend fun upgradeDoubleReply(): ApiResponse<UpgradeDoubleReplyData>

    /** 更新偏好标签 */
    @POST("api/characters/update-preferred-tags")
    suspend fun updatePreferredTags(@Body request: UpdatePreferredTagsRequest): ApiResponse<Unit>

    /** 检查用户引导状态 */
    @GET("api/user/check_onboarding_status")
    suspend fun checkOnboardingStatus(): ApiResponse<OnboardingStatusData>

    /** 获取我的角色列表 */
    @GET("api/characters/get_my_characters")
    suspend fun getMyCharacters(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<MyAIListResponse>

    /** 删除角色 */
    @DELETE("api/characters/delete_character/{character_id}")
    suspend fun deleteCharacter(@Path("character_id") characterId: String): ApiResponse<DeleteCharacterData>

    /** 搜索角色 */
    @POST("api/characters/search")
    suspend fun searchCharacters(@Body request: CharacterSearchRequest): ApiResponse<CharacterSearchResponse>

    // ==================== 支付模块 (/api/user/payment) ====================
    /** 创建充值订单 */
    @POST("api/user/payment/create-order")
    suspend fun createOrder(@Body request: CreateOrderRequest): ApiResponse<CreateOrderData>

    /** 获取充值订单列表 */
    @GET("api/user/payment/order-list")
    suspend fun getOrderList(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ApiResponse<OrderListData>

    // ==================== 文件上传模块 (/api/upload) ====================
    /** 图片上传 */
    @Multipart
    @POST("api/upload/image")
    suspend fun uploadImage(@Part file: okhttp3.MultipartBody.Part): ApiResponse<ImageUploadResponse>

    // ==================== 商店模块 (/api/shop) ====================
    /** 获取商店商品列表 */
    @GET("api/shop/items")
    suspend fun getShopItems(): ApiResponse<ShopItemsResponse>
    
    /** 获取用户已拥有商品列表 */
    @GET("api/shop/owned-items")
    suspend fun getOwnedItems(): ApiResponse<OwnedItemsResponse>
    
    /** 购买商品 */
    @POST("api/shop/purchase")
    suspend fun purchaseItem(@Body request: PurchaseRequest): ApiResponse<PurchaseResponse>
    
    // ==================== 永久记忆权益模块 (/api/permanent-memory) ====================
    
    /** 分配永久记忆权益给角色 */
    @POST("api/permanent-memory/allocate")
    suspend fun allocatePermanentMemory(@Body request: AllocatePermanentMemoryRequest): ApiResponse<AllocatePermanentMemoryResponse>
    
    /** 回收角色的永久记忆权益 */
    @POST("api/permanent-memory/deallocate")
    suspend fun deallocatePermanentMemory(@Body request: DeallocatePermanentMemoryRequest): ApiResponse<DeallocatePermanentMemoryResponse>

    /** 查询角色永久记忆状态 */
    @GET("api/permanent-memory/status/{character_id}")
    suspend fun getPermanentMemoryStatus(@Path("character_id") characterId: String): ApiResponse<PermanentMemoryStatusResponse>



    /** 获取模式类型 */
    @GET("api/device-control/modes")
    suspend fun getModes( ): ApiResponse<ModelsItemsResponse>


    /** 添加收藏 */
    @POST("api/device-control/modes/favorite")
    suspend fun favorite(@Body request: FavoriteRequest ): ApiResponse<FavoriteResponse>


    /** 取消收藏 */
    @POST("api/device-control/modes/unfavorite")
    suspend fun unfavorite(@Body request: FavoriteRequest ): ApiResponse<FavoriteResponse>



    /** 3.1 关注用户接口
    接口地址: POST /api/user/follow
    功能描述: 关注指定用户
    请求头:
     */
    @POST("api/user/follow")
    suspend fun follow(@Body request: FollowRequest): ApiResponse<FollowResponse>


    /** 3.1 关注用户接口
    接口地址: POST /api/user/follow
    功能描述: 取消关注指定用户

    请求头:
     */
    @POST("api/user/unfollow")
    suspend fun unFollow(@Body request: FollowRequest): ApiResponse<FollowResponse>


    /** 3.1 关注用户接口
    接口地址: GET /api/user/followers
    功能描述: 获取当前用户的粉丝列表
    请求头:
     */
    @GET("api/user/followers")
    suspend fun getFollowers(@Query("keyword") keyword: String ="",
                             @Query("page") page: Int = 1,
                             @Query("limit") limit: Int = 20): ApiResponse<FollowsResponse>


    /** 3.1 关注用户接口
    接口地址: GET /api/user/followers
    功能描述: 获取当前用户的粉丝列表
    请求头:
     */
    @GET("api/user/following")
    suspend fun getFollowing(
        @Query("keyword") keyword: String ="",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20): ApiResponse<FollowsResponse>

    /** 获取聊天模型 */
    @GET("api/chat/models")
    suspend fun getChatModels( ): ApiResponse<ChatModelsResponse>



    /** 置顶角色 */
    @POST("api/characters/pin")
    suspend fun pushAiPin(@Body request: PinRequest): ApiResponse<PinResponse>
    /** 取消置顶角色 */
    @POST("api/characters/unpin")
    suspend fun pushAiUnPin(@Body request: PinRequest): ApiResponse<PinResponse>

    /** 回溯聊天 */
    @POST("api/chat/rollback_to_message")
    suspend fun postRollbackToMessage(@Body request: MessageRequest): ApiResponse<RollbackMessageResponse>

    /** 重新生成聊天 */
    @POST("api/chat/regenerate_message")
    suspend fun regenerateMessage(@Body request: MessageRequest): ApiResponse<RegenerateMessageResponse>

    /** 回溯聊天 */
    @GET("api/bills/list")
    suspend fun getPayHistory(@Query("page") page: Int = 1,
                              @Query("limit") limit: Int = 20,
                              @Query("year") year: Int?,
                              @Query("month") month: Int?,
                              @Query("type") type: String?
    ): ApiResponse<PayHistoryResponse>


    /** 获取用户公开角色列表 */
    @GET("api/user/public_characters/{user_id}")
    suspend fun getPublicCharacters(
        @Path("user_id") user_id: String?,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20

    ): ApiResponse<CharacterListResponse>


    /** **功能描述**: 获取用户与指定角色的对话设定信息
     */
    @GET("api/conversation-settings/get")
    suspend fun getConversationSettings(@Query("character_id") character_id: String? = ""
    ): ApiResponse<ConversationSettingsResponse>


    /**
     *   11.2 更新对话设定
     */
    @POST("api/conversation-settings/update")
    suspend fun updateConversationSettings( @Body request: ConversationSettingsRequest
    ): ApiResponse<ConversationSettingsResponse>


    /** 获取记忆簿 */
    @GET("api/memory-book/get/{character_id}")
    suspend fun getMemoryBook(@Path("character_id") character_id: String?): ApiResponse<MemoryBookResponse>

    /** 保存/更新记忆簿记录
     */
    @POST("api/memory-book/save")
    suspend fun postMemoryBook(@Body request: MemoryBookRequest): ApiResponse<MemoryBookResponse>


    /** #### 7.1 获取消息列表

     **接口地址**: `GET /notifications/list`

     **功能描述**: 获取用户的消息列表，支持分页和筛选

     */
    @GET("api/notifications/list")
    suspend fun getNotificationsList( @Query("page") page: Int? = 1,  @Query("limit") limit: Int? = 20): ApiResponse<NotificationsResponse>


    /****接口地址**: `DELETE /api/chat/delete_message`

     **功能描述**: 根据消息ID删除单条消息
     */
    @POST("api/chat/delete_message")
    suspend fun deleteMessage(@Body deleteMessage: DeleteMessageRequest): ApiResponse<DeletedMessageResponse>

    /**
     * ***接口地址**: `PUT /api/chat/update_message`
     **功能描述**: 根据消息ID更新消息内容
     */
    @POST("api/chat/update_message")
    suspend fun updateMessage(@Body updateMessageRequest: UpdateMessageRequest): ApiResponse<UpdateMessageResponse>

    /**
     *
     * **接口地址**: `GET /notifications/unread-count`
     *
     * **功能描述**: 获取用户未读消息数量，用于显示小红点提醒
     *
     * **请求头**:
     */
    @GET("api/notifications/unread-count")
    suspend fun notificationsUnreadCount(): ApiResponse<UnreadCountResponse>
    /**
     *
     * **接口地址**: `GET /notifications/mark-read`
     * **功能描述**: 获取用户未读消息数量，用于显示小红点提醒
     * **请求头**:
     */
    @POST("api/notifications/mark-read")
    suspend fun notificationsMarkRead(@Body markReadRequest: MarkReadRequest): ApiResponse<MarkReadResponse>

    /**
     *
     * **接口地址**: `GET /notifications/mark-read`
     * **功能描述**: 获取用户未读消息数量，用于显示小红点提醒
     * **请求头**:
     */
    @GET("api/user/shop-links")
    suspend fun getShopLinks( ): ApiResponse<ShopLinkResponse>


    /**
     *## 1. 获取角色分支列表
     * - `GET /api/chat/branches/<character_id>`
     * 返回主聊天 + 全部分支。
     *
     */
    @GET("api/chat/branches/{character_id}")
    suspend fun getBranches(@Path("character_id") character_id: String? ): ApiResponse<BranchManagerResponse>

    @POST("api/chat/branches/switch")
    suspend fun branchSwitchBranch(@Body request: SwitchBranchRequest): ApiResponse<BranchResponse>

    @POST("api/chat/branches/pin")
    suspend fun branchPinBranch(@Body request: PinBranchRequest): ApiResponse<BranchResponse>

    @POST("api/chat/branches/{branch_id}/unpin")
    suspend fun branchUnpinBranch(@Path("branch_id") branch_id: String?): ApiResponse<BranchResponse>
    @POST("api/chat/branches/{branch_id}/delete")
    suspend fun branchDeleteBranch(@Path("branch_id") branch_id: String?): ApiResponse<BranchResponse>

    @POST("api/chat/branches")
    suspend fun branchCreateBranch(@Body request: CreateBranchRequest): ApiResponse<BranchResponse>
    @POST("api/chat/branches/{branch_id}")
    suspend fun branchEditeBranchName(@Path("branch_id") branch_id: String?,@Body request: EditeBranchRequest): ApiResponse<BranchResponse>



    /**
    获取版本信息
     *
     */
    @GET("api/app/version/check")
    suspend fun getVersionCheck(@Query("platform") platform: String?,@Query("current_build") currentBuild: Int?,@Query("current_version") currentVersion: String? ): ApiResponse<AppUpdateInfo>

    /**
    ## 4. 查询盲盒订单状态和开奖结果

    盲盒下单后，前端继续复用现有订单状态接口查询支付和开奖结果。

     **接口地址**

    `GET /api/user/order/status/{order_id}`
     */
    @GET("api/user/order/status/{order_id}")
    suspend fun getBlindBoxOrderResult(@Path("order_id") orderId: String?): ApiResponse<OrderDetail>

    /**
    ## 4. 获取角色音色配置

    - `GET /api/characters/<character_id>/voice-config`
     */
    @GET("api/characters/{character_id}/voice-config")
    suspend fun getVoiceConfig(@Path("character_id") characterId: String?): ApiResponse<VoiceConfigData>


    /**
    ## 4. 获取角色音色配置

    - `GET /api/characters/<character_id>/voice-config`
     */
    @POST("api/characters/{character_id}/voice-config")
    suspend fun saveVoiceConfig(@Path("character_id") characterId: String?,@Body request: SaveVoiceConfigRequest): ApiResponse<VoiceConfigData>



}
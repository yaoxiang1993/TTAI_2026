package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.MyBluetoothManager
import com.example.ttai.bean.BranchManagerResponse
import com.example.ttai.bean.BranchResponse
import com.example.ttai.bean.ClearChatData
import com.example.ttai.bean.ConversationsData
import com.example.ttai.bean.CreateBranchRequest
import com.example.ttai.bean.DefaultCharacterChatResponse
import com.example.ttai.bean.DeleteMessageRequest
import com.example.ttai.bean.DeletedMessageResponse
import com.example.ttai.bean.EditeBranchRequest
import com.example.ttai.bean.Message
import com.example.ttai.bean.MessageRequest
import com.example.ttai.bean.MessagesData
import com.example.ttai.bean.PinBranchRequest
import com.example.ttai.bean.RegenerateMessageResponse
import com.example.ttai.bean.RemoveChatSlotRequest
import com.example.ttai.bean.RemoveChatSlotResponse
import com.example.ttai.bean.SendMessageData
import com.example.ttai.bean.SendMessageRequest
import com.example.ttai.bean.ReplaceChatSlotRequest
import com.example.ttai.bean.ReplaceChatSlotResponse
import com.example.ttai.bean.RollbackMessageResponse
import com.example.ttai.bean.SwitchBranchRequest
import com.example.ttai.bean.UpdateMessageRequest
import com.example.ttai.bean.UpdateMessageResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.NetworkModule

class ChatRepository(private val context: Context) {
    private val apiService: ApiService = NetworkModule.createService()

    suspend fun fetchMessages(conversationId: String, page: Int = 1, limit: Int = 50): MessagesData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取消息列表",
            apiCall = { apiService.getMessages(conversationId, page, limit) }
        )
    }

    suspend fun sendMessage(characterId: String, content: String): SendMessageData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "发送消息",
            apiCall = { 
                val request = SendMessageRequest(characterId, content, MyBluetoothManager.getVibrationIntensity(),
                    MyBluetoothManager.getSuckingIntensity(), MyBluetoothManager.connectedDevice!=null)
                apiService.sendMessage(request)
            }
        )
    }

    /**
     * 获取默认角色聊天历史
     * @param page 页码
     * @param limit 每页限制
     * @return 默认角色聊天历史数据
     */
    suspend fun getDefaultCharacterMessages(page: Int = 1, limit: Int = 50): DefaultCharacterChatResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取默认角色聊天历史",
            apiCall = { apiService.getDefaultCharacterMessages(page, limit) }
        )
    }

    /**
     * 替换聊天槽位中的角色
     * @param oldCharacterId 要被替换的角色ID
     * @param newCharacterId 新的角色ID
     * @return 替换结果
     */
    suspend fun replaceChatSlot(oldCharacterId: String, newCharacterId: String): ReplaceChatSlotResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "替换聊天槽位角色",
            apiCall = {
                val request = ReplaceChatSlotRequest(oldCharacterId, newCharacterId)
                apiService.replaceChatSlot(request)
            }
        )
    }
    /**
     * 清除消息
     * @param oldCharacterId 要被替换的角色ID
     * @param newCharacterId 新的角色ID
     * @return 替换结果
     */
    suspend fun clearCharacterChat(characterId: String ): ClearChatData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "清除消息",
            apiCall = {
                apiService.clearCharacterChat(characterId)
            }
        )
    }

    /**
     * 从聊天槽位移除角色
     * @param characterId
     * @return 替换结果
     */
    suspend fun removeChatSlot(characterId: String ): RemoveChatSlotResponse {
        val request = RemoveChatSlotRequest(characterId = characterId)
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "从聊天槽位移除角色",
            apiCall = {
                  apiService.removeChatSlot(request)
            }
        )
    }

    /**
     * 获取对会列表
     */
    suspend fun getConversations(page: Int = 1, limit: Int = 20): ConversationsData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取对话列表",
            apiCall = { apiService.getConversations() }
        )
    }
    /**
     * 回溯聊天
     */
    suspend fun postRollbackToMessage (message: Message?): RollbackMessageResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "回溯聊天",
            apiCall = { apiService.postRollbackToMessage(MessageRequest(message?._id))},
            showMessage = true
        )
    }
    /**
     * 重新生成消息
     */
    suspend fun postRegenerateMessage (message: Message?): RegenerateMessageResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "重新生成消息",
            apiCall = { apiService.regenerateMessage(MessageRequest(message?._id)) }
        )
    }
    /**
     * 删除
     */
    suspend fun deleteMessage (message: Message?): DeletedMessageResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "删除消息",
            apiCall = { apiService.deleteMessage(DeleteMessageRequest(message?._id)) }
        )
    }

    /**
     * 消息ID更新消息内容
     */
    suspend fun UpdateMessage (message: Message?): UpdateMessageResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "更新消息",
            apiCall = { apiService.updateMessage(UpdateMessageRequest(message?._id,message?.content)) }
        )
    }
    /**
     * 获取分支信息
     */
    suspend fun getBranchs (character_id: String?): BranchManagerResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取分支信息",
            apiCall = { apiService.getBranches(character_id) }
        )
    }
    /**
     * 切换分支信息
     */
    suspend fun branchSwitchBranch (character_id: String?,branch_id:String?): BranchResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "切换分支",
            apiCall = { apiService.branchSwitchBranch(SwitchBranchRequest(character_id,branch_id)) }
        )
    }
    /**
     * 置顶分支
     */
    suspend fun branchPinBranch (branch_id:String?): BranchResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "置顶分支",
            apiCall = { apiService.branchPinBranch(PinBranchRequest(branch_id)) }
        )
    }
    /**
     * 取消置顶分支
     */
    suspend fun branchUnpinBranch (branch_id:String?): BranchResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "取消置顶分支",
            apiCall = { apiService.branchUnpinBranch(branch_id) }
        )
    }
    /**
     * 创建分支
     * "character_id": "68b53a6a42fb612951702f61",
     *   "name": "我的新支线",
     *   "mode": "reset",
     *   "source_branch_id": null
     */
    suspend fun branchCreateBranch (character_id:String?,name:String?,mode:String?,source_branch_id:String?,): BranchResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "取消置顶分支",
            apiCall = { apiService.branchCreateBranch(CreateBranchRequest(character_id,name,mode,source_branch_id)) }
        )
    }
    /**
     * 删除分支
     * "character_id": "68b53a6a42fb612951702f61",
     *   "name": "我的新支线",
     *   "mode": "reset",
     *   "source_branch_id": null
     */
    suspend fun branchDeleteBranch (branch_id:String?): BranchResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "删除分支",
            apiCall = { apiService.branchDeleteBranch(branch_id) }
        )
    } /**
     * 编辑分支名称
     */
    suspend fun branchEditeBranchName (branch_id:String?,editeName:String?): BranchResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "编辑分支备注",
            apiCall = { apiService.branchEditeBranchName(branch_id, EditeBranchRequest(editeName) ) }
        )
    }

}
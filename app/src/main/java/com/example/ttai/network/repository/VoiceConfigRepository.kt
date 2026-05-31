package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.EndVoiceRequest
import com.example.ttai.bean.SaveVoiceConfigRequest
import com.example.ttai.bean.StartVoiceCallRequest
import com.example.ttai.bean.VoiceCallData
import com.example.ttai.bean.VoiceCallStatusUpdate
import com.example.ttai.bean.VoiceConfigData
import com.example.ttai.bean.VoiceUserTurnRequest
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService

/**
 * 用户相关API调用仓库
 */
class VoiceConfigRepository(
    private val apiService: ApiService,
    private val context: Context
) {

    /**
     * 获取角色音色配置
     *
     *
     */
    suspend fun getVoiceConfig( characterId:String?=""): VoiceConfigData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "获取角色音色配置",
            apiCall = { apiService.getVoiceConfig( characterId) }
        )
    }

    /**
     *## 5. 保存角色音色配置
     *
     * - `POST /api/characters/<character_id>/voice-config`
     */
    suspend fun saveVoiceConfig(
        characterId: String? = "",
        voice_type: String? = "",
        voice_code: String? = ""
    ): VoiceConfigData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "保存角色音色配置",
            apiCall = {
                apiService.saveVoiceConfig(
                    characterId, SaveVoiceConfigRequest(voice_type, voice_code)
                )
            }
        )
    }
    /**
    ### 7.2 `POST /api/chat/voice_call/start`
    作用：
    - 发起语音通话
    - 当前真实主链路会调用 IMS `GenerateAIAgentCall`
    请求体：
    ```json
    {
    "character_id": "68b53a6a42fb612951702f61",
    "branch_id": null
    }
    ```
    说明：
    - `character_id` 必填
    - `branch_id` 可选
    - 后端会校验角色权限
    - 后端会自动创建或复用 `conversation`
    - 用户同一时间只允许一个进行中的语音会话；如果已有进行中会话，会直接返回已有会话
    - 返回里的 `required_fairy_jade` 表示当前发起通话要求的仙玉数，前端以返回值为准
    成功返回示例：
    ```json
    {
    "code": 200,
    "message": "发起语音通话成功",
    "data": {
    "session_id": "voice_1234567890abcdef12345678",
    "conversation_id": "6914b6c91e6bfd32d7a549d7",
    "character_id": "68b53a6a42fb612951702f61",
    "character_name": "化世景",
    "branch_id": null,
    "call_status": "connecting",
    "mic_muted": false,
    "can_interrupt": false,
    "can_switch_to_text": true,
    "duration_seconds": 0,
    "latest_user_text": null,
    "latest_ai_text": null,
    "current_message": null,
    "last_message_time": 1762213300,
    "created_at": 1762213300,
    "updated_at": 1762213300,
    "expires_at": 1762215100,
    "provider": "aliyun_ims",
    "ims": {
    "instance_id": "ims-instance-id",
    "region": "cn-beijing",
    "channel_id": "ims-channel-id",
    "rtc_user_id": "rtc-user-id",
    "rtc_token": "rtc-token",
    "aiagent_user_id": "aiagent-user-id",
    "avatar_user_id": "avatar-user-id",
    "artc_app_id": "95c78806-66c5-4094-b072-6af4964c2b6b",
    "ims_status": "Created",
    "call_log_url": null
    },
    "required_fairy_jade": 20
    }
    }
    ```

    前端联调重点：

    1. 调 `start`
    2. 读取 `data.ims`
    3. 用 `region`、`channel_id`、`rtc_user_id`、`rtc_token`、`artc_app_id` 初始化并入会
    4. 后续用 `active` / `status` 查询会话业务状态
     */
    suspend fun startVoiceCall(
        characterId: String? = "",
        branch_id: String? = "",
    ): VoiceCallData? {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "初始化官方 SDK 所需参数",
            apiCall = {
                apiService.startVoiceCall(StartVoiceCallRequest (characterId, branch_id)
                )
            },
            showSuccessMessage =false,
        )
    }
    suspend fun getVoiceCallActive(
        characterId: String = "",
    ): VoiceCallData? {
        ApiHelper.autoShowSuccessMessages = false
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "初始化官方 SDK 所需参数",
            apiCall = {
                apiService.getVoiceCallActive(characterId)
            },
            showSuccessMessage =false,
        )
    }

    /***
     *
     * {
     *   "input_mode": "voice",
     *   "content": "你好，很高兴认识你",
     *   "audio_url": "https://becomestar.com.cn/upload/user-voice.wav",
     *   "audio_duration_ms": 2100
     * }
     */
    suspend fun sendUserTurn(sid: String, text2: String): VoiceCallStatusUpdate {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "发送文本",
            apiCall = {
                apiService.sendUserTurn(sid,VoiceUserTurnRequest("text",text2,"",0))
            }
        )
    }
    suspend fun endVoiceCall(sid: String) : VoiceCallData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "关闭会话",
            apiCall = {
                apiService.endVoiceCall(sid, EndVoiceRequest("user_hangup" ))
            }
        )
    }
}

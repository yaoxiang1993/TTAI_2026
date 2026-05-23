package com.example.ttai.network.repository

import android.content.Context
import com.example.ttai.bean.FollowRequest
import com.example.ttai.bean.FollowResponse
import com.example.ttai.bean.FollowsResponse
import com.example.ttai.bean.OnboardingStatusData
import com.example.ttai.bean.PayHistoryItem
import com.example.ttai.bean.PayHistoryResponse
import com.example.ttai.bean.ProfileData
import com.example.ttai.bean.RollbackMessageResponse
import com.example.ttai.bean.SaveVoiceConfigRequest
import com.example.ttai.bean.UpdateAiModelRequest
import com.example.ttai.bean.UpdateAiModelResponse
import com.example.ttai.bean.VoiceConfigData
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException
import retrofit2.http.Query

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
}

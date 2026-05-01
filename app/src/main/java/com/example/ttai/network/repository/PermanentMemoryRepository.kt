package com.example.ttai.network.repository

import com.example.ttai.bean.AllocatePermanentMemoryRequest
import com.example.ttai.bean.AllocatePermanentMemoryResponse
import com.example.ttai.bean.DeallocatePermanentMemoryRequest
import com.example.ttai.bean.DeallocatePermanentMemoryResponse
import com.example.ttai.bean.PermanentMemoryStatusResponse
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.ApiService
import com.example.ttai.network.exception.ApiException

/**
 * 永久记忆权益相关网络请求仓库
 */
class PermanentMemoryRepository(private val apiService: ApiService,
                                private val context: android.content.Context) {

    /**
     * 分配永久记忆权益给角色
     * @param characterId 角色ID
     * @return 分配结果响应
     * @throws ApiException 当网络请求失败时抛出
     */
    suspend fun allocatePermanentMemory(characterId: String): AllocatePermanentMemoryResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "分配永久记忆权益给角色",
            apiCall = {
                val request = AllocatePermanentMemoryRequest(characterId)
                apiService.allocatePermanentMemory(request)
            }
        )
    }


    /**
     * 回收角色的永久记忆权益
     * @param characterId 角色ID
     * @return 回收结果响应
     * @throws ApiException 当网络请求失败时抛出
     */
    suspend fun deallocatePermanentMemory(characterId: String): DeallocatePermanentMemoryResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "回收角色的永久记忆权益",
            apiCall = {
                val request = DeallocatePermanentMemoryRequest(characterId)
                apiService.deallocatePermanentMemory(request)
            }
        )
    }

    /**
     * 查询角色永久记忆状态
     * @param characterId 角色ID
     * @return 永久记忆状态响应
     * @throws ApiException 当网络请求失败时抛出
     */
    suspend fun getPermanentMemoryStatus(characterId: String): PermanentMemoryStatusResponse {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "查询角色永久记忆状态",
            apiCall = {
                apiService.getPermanentMemoryStatus(characterId)
            }
        )
    }
}

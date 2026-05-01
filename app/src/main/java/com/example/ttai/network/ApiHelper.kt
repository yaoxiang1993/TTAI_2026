package com.example.ttai.network

import android.content.Context
import com.example.ttai.bean.ApiResponse
import com.example.ttai.network.exception.ApiException
import com.example.ttai.utils.ToastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * API请求辅助类
 * 统一处理网络请求，自动显示返回的message字段
 */
object ApiHelper {
    
    /**
     * 全局设置：是否自动显示错误提示
     * 默认为true，表示当接口返回code不是200时自动显示错误提示
     */
    var autoShowErrorMessages: Boolean = true
    
    /**
     * 全局设置：是否显示成功消息
     * 默认为true，表示当接口返回code是200时显示成功消息
     */
    var autoShowSuccessMessages: Boolean = true
    
    /**
     * 执行API请求并处理响应
     * @param context 上下文，用于显示Toast
     * @param requestName 请求名称，用于日志记录
     * @param apiCall 具体的API调用
     * @param showMessage 是否显示返回的message字段，默认为null（使用全局设置）
     * @return 返回API响应的data字段
     */
    suspend fun <T> executeApiRequest(
        context: Context,
        requestName: String,
        apiCall: suspend () -> ApiResponse<T>,
        showMessage: Boolean? = null
    ): T {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                // 记录API响应日志
                android.util.Log.d("ApiHelper", "$requestName - Code: ${response.code}, Message: ${response.message}")

                // 检查响应状态
                if (response.code == 200 && response.data != null) {
                    // 成功响应，显示成功消息（如果启用）
                    val shouldShowSuccess = showMessage ?: autoShowSuccessMessages
                    if (shouldShowSuccess && response.message.isNotBlank()) {
                        withContext(Dispatchers.Main) {
                            ToastUtils.showApiMessage(context, response.message)
                        }
                    }
                    response.data
                } else {
                    // 非200状态码，显示接口返回的错误信息（如果启用）
                    val shouldShowError = showMessage ?: autoShowErrorMessages
                    if (shouldShowError) {
                        withContext(Dispatchers.Main) {
                            ToastUtils.showError(context, response.message)
                        }
                    }
                    throw ApiException(response.code, response.message)
                }
            } catch (e: Exception) {
                if (e is ApiException){
                    withContext(Dispatchers.Main) {
                        ToastUtils.showError(context, "${e.message}")
                    }
                }else{
                    android.util.Log.e("ApiHelper", "$requestName 异常", e)
                    val shouldShowError = showMessage ?: autoShowErrorMessages
                    if (shouldShowError) {
                        withContext(Dispatchers.Main) {
                            ToastUtils.showError(context, "${e.message}")
                        }
                    }
                }
                throw e
            }
        }
    }

    /*catch (e: retrofit2.HttpException) {
        android.util.Log.e("ApiHelper", "$requestName HTTP异常: ${e.code()}", e)
        // 尝试解析错误响应
        try {
            val errorBody = e.response()?.errorBody()?.string()
            android.util.Log.e("ApiHelper", "$requestName 错误响应体: $errorBody")

            // 尝试解析错误响应体中的message字段
            val errorMessage = if (!errorBody.isNullOrBlank()) {
                try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, ApiResponse::class.java)
                    errorResponse.message.ifBlank { "网络请求失败: HTTP ${e.code()}" }
                } catch (parseError: Exception) {
                    android.util.Log.w("ApiHelper", "$requestName 解析错误响应message失败", parseError)
                    "网络请求失败: HTTP ${e.code()}"
                }
            } else {
                "网络请求失败: HTTP ${e.code()}"
            }

            // 显示解析出的错误信息（如果启用）
            val shouldShowError = showMessage ?: autoShowErrorMessages
            if (shouldShowError) {
                withContext(Dispatchers.Main) {
                    ToastUtils.showError(context, errorMessage)
                }
            }

            throw ApiException(e.code(), errorMessage)
        } catch (parseException: Exception) {
            android.util.Log.e("ApiHelper", "$requestName 解析错误响应失败", parseException)
            val shouldShowError = showMessage ?: autoShowErrorMessages
            if (shouldShowError) {
                withContext(Dispatchers.Main) {
                    ToastUtils.showError(context, "网络请求失败: HTTP ${e.code()}")
                }
            }
            throw ApiException(e.code(), "网络请求失败: HTTP ${e.code()}")
        }
*/
} 
package com.example.ttai.network

import android.content.Context
import android.util.Log
import com.example.ttai.network.repository.AuthRepository
import com.example.ttai.network.repository.CharacterRepository
import com.example.ttai.network.repository.ChargeMoneyRepository
import com.example.ttai.network.repository.FollowerRepository
import com.example.ttai.network.repository.MembershipRepository
import com.example.ttai.network.repository.ModesRepository
import com.example.ttai.network.repository.MyFragmentRepository
import com.example.ttai.network.repository.NotifyRepository
import com.example.ttai.network.repository.PayRepository
import com.example.ttai.network.repository.ShopRepository
import com.example.ttai.network.repository.SelectTagRepository
import com.example.ttai.network.repository.UserRepository
import com.example.ttai.utils.DebugUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.ResponseBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    // 环境配置
    enum class Environment {
        PRODUCTION,
        TEST
    }
    
    // 当前环境配置
    private var currentEnvironment = Environment.TEST // 默认使用测试环境
    
    // 不同环境的base URL
    private val BASE_URLS = mapOf(
        Environment.PRODUCTION to "https://becomestar.com.cn/",
        Environment.TEST to "http://34.143.193.85:5000/"
    )
    
    // Token管理
    private var authToken: String? = null
    
    /**
     * 设置当前环境
     */
    fun setEnvironment(environment: Environment) {
        currentEnvironment = environment
    }

    /**
     * 获取当前环境名称
     */
    fun getCurrentEnvironmentName(): String {
        return when (currentEnvironment) {
            Environment.PRODUCTION -> "生产环境"
            Environment.TEST -> "测试环境"
        }
    }
    
    fun setAuthToken(token: String) {
        authToken = token
    }
    
    fun clearAuthToken() {
        authToken = null
    }

    // 认证拦截器
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = if (authToken != null) {
            DebugUtils.logInfo("NetworkModule", "添加认证头: Bearer ${authToken?.take(10)}...")
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $authToken")
                .build()
        } else {
            DebugUtils.logWarning("NetworkModule", "未找到认证token，跳过认证头")
            originalRequest
        }
        chain.proceed(newRequest)
    }

    // 自定义日志拦截器
    private val customLoggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        // 打印请求信息
        DebugUtils.logNetworkRequest(
            url = request.url.toString(),
            method = request.method,
            headers = request.headers.toString(),
            body = request.body?.toString() ?: "无请求体"
        )
        
        val response = chain.proceed(request)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // 读取响应体
        val responseBody = response.body
        val responseBodyString = responseBody?.string() ?: ""
        
        // 创建新的响应体（因为响应体只能读取一次）
        val newResponseBody = ResponseBody.create(
            responseBody?.contentType(),
            responseBodyString
        )
        
        val newResponse = response.newBuilder()
            .body(newResponseBody)
            .build()
        
        // 打印响应信息
        DebugUtils.logNetworkResponse(
            url = request.url.toString(),
            method = request.method,
            code = response.code,
            message = response.message,
            duration = duration,
            headers = response.headers.toString(),
            body = responseBodyString
        )
        
        newResponse
    }
    val sseListener = object : EventSourceListener() {
        override fun onOpen(eventSource: EventSource, response: Response) {
            // 连接成功建立
            Log.d("SSE", "Connection Opened")
        }

        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            // 接收到消息
            // type 对应服务端的 event 字段，data 对应服务端 data 字段
            Log.d("SSE", "Received event: $data")
        }

        override fun onClosed(eventSource: EventSource) {
            // 连接关闭
            Log.d("SSE", "Connection Closed")
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            // 出错，通常在这里处理重连逻辑
            Log.e("SSE", "Error: ${t?.message}")
        }
    }

    // 提取公共的 OkHttpClient 配置
    private fun getBaseOkHttpClientBuilder(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(customLoggingInterceptor)
            .addInterceptor(logging)
    }

    // 提供给 Retrofit 使用的 Client (带超时控制)
    private fun provideOkHttpClient(): OkHttpClient {
        return getBaseOkHttpClientBuilder()
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    // 提供给 SSE 使用的 Client：不能复用带 customLoggingInterceptor 的 Builder
    // customLoggingInterceptor 会 responseBody.string() 把整个 SSE 流读完后才返回，导致无法边收边渲染
    private fun provideSseOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(180, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS)
            .build()
    }

    fun provideRetrofit(): Retrofit {
        val customGson: Gson = GsonBuilder()
            .registerTypeAdapterFactory(NullStringToEmptyObjectAdapterFactory())
            .create()

        return Retrofit.Builder()
            .baseUrl("https://becomestar.com.cn/") // 使用你定义的动态 URL
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(customGson))
            .build()
    }

    /**
     * 创建并开启 POST 方式的 SSE 连接
     * @param path 请求路径
     * @param jsonBody 请求体 JSON 字符串
     * @param listener 监听器
     */
    fun createPostSseConnection(path: String, jsonBody: String, listener: EventSourceListener): EventSource {
        val baseUrl = "https://becomestar.com.cn/"
        val fullUrl = if (baseUrl.endsWith("/") || path.startsWith("/")) "$baseUrl$path" else "$baseUrl/$path"

        // 构建 RequestBody
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(fullUrl)
            .post(body) // 使用 POST 请求
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        // 使用专门的 SSE Client (readTimeout 为 0)
        val factory = EventSources.createFactory(provideSseOkHttpClient())
        return factory.newEventSource(request, listener)
    }

    inline fun <reified T> createService(): T {
        return provideRetrofit().create(T::class.java)
    }
    
    fun provideApiService(): ApiService {
        return provideRetrofit().create(ApiService::class.java)
    }
    
    fun provideAuthRepository(context: Context): AuthRepository {
        return AuthRepository(provideApiService(), context)
    }
    
    fun provideCharacterRepository(context: Context): CharacterRepository {
        return CharacterRepository(provideApiService(), context)
    }

    fun provideShopRepository(context: Context): ShopRepository {
        return ShopRepository(provideApiService(), context)
    }
    
    fun provideChargeMoneyRepository(context: Context): ChargeMoneyRepository {
        return ChargeMoneyRepository(provideApiService(), context)
    }
    
    fun provideSelectTagRepository(context: Context): SelectTagRepository {
        return SelectTagRepository(provideApiService(), context)
    }
    
    fun provideMembershipRepository(context: Context): MembershipRepository {
        return MembershipRepository(provideApiService(), context)
    }
    
    fun provideMyFragmentRepository(context: Context): MyFragmentRepository {
        return MyFragmentRepository(provideApiService(), context)
    }


    fun provideUserRepository(context: Context): UserRepository {
        return UserRepository(provideApiService(), context)
    }
    fun provideModesRepository(context: Context): ModesRepository {
        return ModesRepository(provideApiService(), context)
    }

    fun provideFollowerRepository(context: Context): FollowerRepository {
        return FollowerRepository(provideApiService(), context)
    }

    fun provideNotifyRepository(context: Context): NotifyRepository {
        return NotifyRepository(provideApiService(), context)
    }

    fun providePayRepository(context: Context): PayRepository {
        return PayRepository(provideApiService(), context)
    }

}
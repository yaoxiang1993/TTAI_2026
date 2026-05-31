package com.example.ttai.ui.activity


import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.aliyun.auikits.aiagent.ARTCAICallEngine
import com.aliyun.auikits.aiagent.ARTCAICallEngine.AICallErrorCode
import com.aliyun.auikits.aiagent.ARTCAICallEngine.ARTCAICallConfig
import com.aliyun.auikits.aiagent.ARTCAICallEngine.ARTCAICallNetworkQuality
import com.aliyun.auikits.aiagent.ARTCAICallEngine.ARTCAICallRobotState
import com.aliyun.auikits.aiagent.ARTCAICallEngine.IARTCAICallEngineCallback
import com.aliyun.auikits.aiagent.ARTCAICallEngine.VoicePrintStatusCode
import com.aliyun.auikits.aiagent.ARTCAICallEngineImpl
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.R
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.Character
import com.example.ttai.bean.ImsConfig
import com.example.ttai.bean.SDKConfig
import com.example.ttai.databinding.ActivityVideoCallBinding
import com.example.ttai.intent.VideoCallIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.VideoCallState
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.ui.vm.VideoCallViewModel
import com.example.ttai.ui.vm.VideoCallViewModelFactory
import com.example.ttai.utils.Constants
import com.example.ttai.utils.ImageUtils
import com.example.ttai.utils.KeyboardManager
import com.example.ttai.utils.LayoutUtils
import com.example.ttai.utils.SystemUIUtils


class VideoCallActivity : BaseMviActivity<VideoCallIntent, VideoCallState, VideoCallViewModel, ActivityVideoCallBinding>() {


    override val binding by viewBinding { ActivityVideoCallBinding.inflate(it) }
    private val character by lazy { intent.getParcelableExtra<Character>(Constants.CHARACTER_KEY) }

    override val viewModel: VideoCallViewModel by viewModels {
        VideoCallViewModelFactory(NetworkModule.provideApiService(), this)
    }
    private var isCalling = false // 增加一个私有标志位
    private var aRTCAICallRobotState : ARTCAICallRobotState? =null

    // 成员变量定义
    private var mEngine: ARTCAICallEngineImpl? = null
    private var currentSessionId: String? = null // 用于防止重复入会
    private var state: VideoCallState? =null
    private fun startImsCall(sdkConfig: SDKConfig?,imgSDKConfig: ImsConfig?) {

        Log.d("VideoCallActivity", "当前机器人状态: $aRTCAICallRobotState")
        Log.d("VideoCallActivity", "YXTEST  startImsCall  sdkConfig ${sdkConfig}")
        // 关键判断：检查当前机器人状态
        // 如果已经在通话中或正在连接，不要重复 call
        if (aRTCAICallRobotState == ARTCAICallRobotState.Listening ||
            aRTCAICallRobotState == ARTCAICallRobotState.Speaking) {
            Log.w("VideoCallActivity", "已经在通话中，跳过重复呼叫")
            return
        }
        // 1. 创建引擎实例
        if (mEngine == null) {
            mEngine = ARTCAICallEngineImpl(this, sdkConfig?.rtcUserId)
            mEngine?.setEngineCallback(mCallEngineCallback)
        }else{
            Log.d("VideoCallActivity", "mEngine: $mEngine")
            // 如果之前发生过错误导致挂断，可以在这里先确保清空上一次的状态
            mEngine?.handup()
        }
        val artcaiCallConfig = ARTCAICallConfig()
        artcaiCallConfig.agentType = ARTCAICallEngine.ARTCAICallAgentType.VoiceAgent
        artcaiCallConfig.region = sdkConfig?.region
        artcaiCallConfig.agentUserId = sdkConfig?.agentUserId
        artcaiCallConfig.agentId = sdkConfig?.agentId


        Log.d("VideoCallActivity", "YXTEST  artcaiCallConfig ${artcaiCallConfig}")
        Log.d("VideoCallActivity", "YXTEST  rtcToken ${sdkConfig?.rtcToken}")
        // 3. 发起通话
        mEngine?.init(artcaiCallConfig)
        mEngine?.call(sdkConfig?.rtcToken, imgSDKConfig?.instanceId,
            sdkConfig?.agentUserId,
            imgSDKConfig?.channelId)
    }
    protected var mCallEngineCallback: IARTCAICallEngineCallback =
        object : IARTCAICallEngineCallback() {
            override fun onErrorOccurs(errorCode: AICallErrorCode?) {
                Log.d("VideoCallActivity", "YXTEST    onErrorOccurs $errorCode")
                // 发生了错误，结束通话
                mEngine?.handup()
            }

            override fun onCallBegin() {
                Log.d("VideoCallActivity", "YXTEST    onCallBegin")
                // 通话开始（入会）
//                sendIntent(VideoCallIntent.ToggleMic(true))
                // 确保入会后麦克风是打开的
                mEngine?.muteMicrophone(false)
                // 通知 UI 更新图标
                sendIntent(VideoCallIntent.ToggleMic(true))
            }

            override fun onCallEnd() {
                Log.d("VideoCallActivity", "YXTEST    onCallEnd")
                // 通话结束（离会）
            }

            override fun onAICallEngineRobotStateChanged(
                oldRobotState: ARTCAICallRobotState?,
                newRobotState: ARTCAICallRobotState?
            ) {
                Log.d("VideoCallActivity", "YXTEST  onAICallEngineRobotStateChanged oldRobotState：$oldRobotState newRobotState：$newRobotState")
                // 机器人状态同步
                aRTCAICallRobotState = newRobotState
                if (newRobotState== ARTCAICallRobotState.Listening){
                    sendIntent(VideoCallIntent.changePlayStatus(Constants.PLAY_LISTENING))
                }else if (newRobotState== ARTCAICallRobotState.Thinking){
                    sendIntent(VideoCallIntent.changePlayStatus(Constants.PLAY_THINKING))
                }else if (newRobotState== ARTCAICallRobotState.Speaking){
                    sendIntent(VideoCallIntent.changePlayStatus(Constants.PLAY_SPEAKING))
                }
            }

            override fun onUserSpeaking(isSpeaking: Boolean) {
//                Log.d("VideoCallActivity", "YXTEST    onUserSpeaking  isSpeaking ${isSpeaking} ")
                // 用户说话回调
                if (isSpeaking){
                    sendIntent(VideoCallIntent.changePlayStatus(Constants.PLAY_LISTENING))
                }
            }

            override fun onUserAsrSubtitleNotify(
                text: String?,
                isSentenceEnd: Boolean,
                sentenceId: Int,
                voicePrintStatusCode: VoicePrintStatusCode?
            ) {
                Log.d("VideoCallActivity", "YXTEST    onUserAsrSubtitleNotify")
            }

            override fun onAIAgentSubtitleNotify(
                text: String?,
                end: Boolean,
                userAsrSentenceId: Int
            ) {
                // 同步智能体回应的话
                Log.d("VideoCallActivity", "YXTEST    onAIAgentSubtitleNotify ${text}  end ${end}  ")
                sendIntent(VideoCallIntent.AIAgentReply(text,end))
            }

            override fun onNetworkStatusChanged(uid: String?, quality: ARTCAICallNetworkQuality?) {
                // 网络状态回调
                Log.d("VideoCallActivity", "YXTEST    onNetworkStatusChanged")
            }

            override fun onVoiceVolumeChanged(uid: String?, volume: Int) {
                // 音量变化
//                Log.d("VideoCallActivity", "YXTEST    onVoiceVolumeChanged  ${uid}")
            }

            override fun onVoiceIdChanged(voiceId: String?) {
                // 当前通话的音色发生了改变
                Log.d("VideoCallActivity", "YXTEST    onVoiceIdChanged  ${voiceId}")
            }

            override fun onVoiceInterrupted(enable: Boolean) {
                // 当前通话的语音打断设置改变
                Log.d("VideoCallActivity", "YXTEST    onVoiceInterrupted   ${enable}")
            }

            override fun onAgentVideoAvailable(available: Boolean) {
                // 智能体视频是否可用（推流）
                Log.d("VideoCallActivity", "YXTEST    onAgentVideoAvailable   available ${available}")
            }

            override fun onAgentAudioAvailable(available: Boolean) {
                // 智能体音频是否可用（推流）
                Log.d("VideoCallActivity", "YXTEST    onAgentAudioAvailable  ${available}")
            }

            override fun onAgentAvatarFirstFrameDrawn() {
                // 数字人首视频帧渲染
                Log.d("VideoCallActivity", "YXTEST    onAgentAvatarFirstFrameDrawn")
            }

            override fun onUserOnLine(uid: String?) {
                // 用户上线回调
                Log.d("VideoCallActivity", "YXTEST    onUserOnLine ${uid} ")
            }
        }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

    }

    override fun setupViews() {
        SystemUIUtils.setTransparentStatusBarVisible(this)
        SystemUIUtils.setStatusBarIconColor(this, true) // 白色图标

        // 设置点击外部区域隐藏键盘
        KeyboardManager.setupHideKeyboardOnTouchOutside(this, binding.root, binding.etMessage)

        // 设置输入框监听
        setupInputListeners()

        // 设置软键盘监听
        setupKeyboardListener()
        // 设置导航栏不遮挡 输入框
        LayoutUtils.setupInputContainerPadding(this, R.id.input_container)

        binding.tvSendMessage.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                // 1. 发送给后端记录
//                sendIntent(VideoCallIntent.SendUserTurn(text))
                // 2. 如果 SDK 支持文本插话，也可以调用 SDK
                mEngine?.sendTextToAgent(ARTCAICallEngine.ARTCAICallSendTextToAgentRequest(text))
                binding.etMessage.text.clear()
            }
        }
        binding.ivPhoneHangUp.setOnClickListener {
            if (state?.isConnecting == true){
                finish()
            }else{
                showStopChatDialog()
            }
        }
        binding.ivPhoneConnect.setOnClickListener {
            val isMute = !(state?.isMicrophoneOpen?:false)
            mEngine?.muteMicrophone(isMute) // 调用 SDK 静音
            // 发送 Intent 更新本地 State 切换图标
            sendIntent(VideoCallIntent.ToggleMic(isMute))
        }

        binding.ivMicrophone.setOnClickListener {
            sendIntent(VideoCallIntent.changeToVoice(true))
        }
        binding.ivKeyboard.setOnClickListener {
            sendIntent(VideoCallIntent.changeToVoice(false))
        }
        binding.tvClickStop.setOnClickListener {
            mEngine?.interruptSpeaking()
        }


        sendIntent(VideoCallIntent.ConnectVideo(characterId = character?.id?:""))
        character?.let {
            loadImage(it.avatarUrl)
        }
        checkRequiredPermissions{}
    }


    /**
     * 设置输入框监听
     */
    private fun setupInputListeners() {
        // 输入框回车键监听
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                return@setOnEditorActionListener true
            }
            false
        }

        // 输入框焦点监听
        binding.etMessage.setOnFocusChangeListener { _, hasFocus ->

        }
    }

    /**
     * 设置软键盘监听
     */
    private fun setupKeyboardListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime())
            val systemInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            if (imeInsets.bottom > 0) {
                // 手动调整输入框位置
                binding.inputContainer.post {
                    val params = binding.inputContainer.layoutParams as ConstraintLayout.LayoutParams
                    params.bottomMargin = imeInsets.bottom
                    binding.inputContainer.layoutParams = params
                }
            } else {
                // 软键盘隐藏时，恢复输入框位置
                binding.inputContainer.post {
                    val params = binding.inputContainer.layoutParams as ConstraintLayout.LayoutParams
                    params.bottomMargin = 0
                    binding.inputContainer.layoutParams = params
                }
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * 发送消息
     */
    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            binding.etMessage.text.clear()
        }
    }

    override fun render(state: VideoCallState) {
        super.render(state)
        this.state = state
        if (state.isEnd){
            finish()
        }
        binding.tvVideoTip.isVisible = state.isConnecting
        binding.tvConnectTip.isVisible = state.isConnecting
        binding.inputContainer.isVisible = !state.isConnecting
        binding.tvName.isVisible =  !state.isConnecting
        binding.tvTime.isVisible =  !state.isConnecting
        binding.ivMenu.isVisible =  !state.isConnecting
        binding.tvStatus.isVisible =  !state.isConnecting

        binding.tvVideoTip.isVisible = !state.isPlaying
        binding.tvName.isVisible =  state.isPlaying
        binding.tvTime.isVisible =  state.isPlaying
        binding.ivMenu.isVisible =  state.isPlaying
        binding.tvStatus.isVisible =  state.isPlaying
        if (state.isConnecting){
            binding.ivPhoneConnect.setImageResource(R.mipmap.icon_phone_connected)
        }
        if (state.isVoice){
            binding.inputContainer.isVisible = false
            binding.ivKeyboard.isVisible = true
        }else{
            binding.inputContainer.isVisible = true
            binding.ivKeyboard.isVisible = false
        }
        if (state.isPlaying) {
            binding.ivPhoneConnect.setImageResource(
                if (state.isMicrophoneOpen) {
                    R.mipmap.icon_enable_voice
                } else {
                    R.mipmap.icon_unenable_voice
                }
            )
        }
        binding.tvStatus.text = when (state.playStatus) {
            Constants.PLAY_SPEAKING -> "说话中..."
            Constants.PLAY_LISTENING -> "聆听中..."
            Constants.PLAY_THINKING -> "思考中..."
            else -> "" // 或者保留原样 binding.tvStatus.text
        }
        // 【缺少】处理 IMS 入会参数
        state.sdkConfig?.let { config ->
            if (currentSessionId != state.sessionId) {
                currentSessionId = state.sessionId
                isCalling = false // Session 变了，重置标志位
            }

            if (!isCalling && currentSessionId != null) {
                isCalling = true
                checkRequiredPermissions {
                    startImsCall(config,state.imsConfig)
                }
            }
        }

        // 更新字幕 TextView
        if (state.currentSubtitle.isNotBlank()) {
            binding.tvChatMessage.isVisible = true
            binding.tvChatMessage.text = state.currentSubtitle
        } else {
            // 如果当前没有字幕，可以隐藏或者显示占位符
            binding.tvChatMessage.isVisible = false
        }

    }
    /**
     * 加载背景图片
     */
    private fun loadImage(imageUrl: String?) {
        ImageUtils.loadImageToBG(this,imageUrl,binding.ivBackground)
        Glide.with(this)
            .load(imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .transform(CenterCrop(), CircleCrop())
            .into(binding.ivHeard)
    }
    /**
     * 结束本次通话
     */
    private fun showStopChatDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "是否结束本次通话\n",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
              sendIntent(VideoCallIntent.EndCall)
            }

            override fun onNegativeClick() {
                // 取消操作
            }
        })
        dialog.show(supportFragmentManager, "showStopChatDialog")
    }

    // 1. 修改启动器，支持多权限请求
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        val phoneStateGranted = permissions[android.Manifest.permission.READ_PHONE_STATE] ?: false

        if (recordAudioGranted && phoneStateGranted) {
            // 所有必要权限已获得，尝试发起通话
            state?.let { startImsCall(it.sdkConfig,it?.imsConfig) }
        } else {
            val msg = if (!recordAudioGranted) "需要麦克风权限才能通话" else "需要读取电话状态权限以优化通话体验"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            // 也可以选择不 finish，只提示用户，但某些设备上不给 phone_state 会导致 RTC 初始化失败
        }
    }

    /**
     * 修改检查权限的方法，同时检查两个权限
     */
    private fun checkRequiredPermissions(onGranted: () -> Unit) {
        val permissions = arrayOf(
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_PHONE_STATE
        )

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            onGranted()
        } else {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mEngine?.setEngineCallback(null) // 断开回调
        mEngine?.handup()
        mEngine?.destroy() // 如果 SDK 有 release 方法，务必调用
        mEngine = null
    }
}
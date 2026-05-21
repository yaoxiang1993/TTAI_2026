package com.example.ttai.ui.activity


import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.R
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.Character
import com.example.ttai.network.NetworkModule
import com.example.ttai.utils.Constants
import com.example.ttai.utils.KeyboardManager
import com.example.ttai.utils.LayoutUtils
import com.example.ttai.databinding.ActivityVideoCallBinding
import com.example.ttai.intent.AIDetailsIntent
import com.example.ttai.intent.VideoCallIntent
import com.example.ttai.state.VideoCallState
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.ui.vm.VideoCallViewModel
import com.example.ttai.ui.vm.VideoCallViewModelFactory
import com.example.ttai.utils.ImageUtils
import com.example.ttai.utils.SystemUIUtils

class VideoCallActivity : BaseMviActivity<VideoCallIntent, VideoCallState, VideoCallViewModel, ActivityVideoCallBinding>() {


    override val binding by viewBinding { ActivityVideoCallBinding.inflate(it) }
    private val character by lazy { intent.getParcelableExtra<Character>(Constants.CHARACTER_KEY) }

    override val viewModel: VideoCallViewModel by viewModels {
        VideoCallViewModelFactory(NetworkModule.provideApiService(), this)
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

        }
        sendIntent(VideoCallIntent.ConnectVideo)
        character?.let {
            loadImage(it.avatarUrl)
        }

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
              finish()
            }

            override fun onNegativeClick() {
                // 取消操作
            }
        })
        dialog.show(supportFragmentManager, "showStopChatDialog")
    }
}
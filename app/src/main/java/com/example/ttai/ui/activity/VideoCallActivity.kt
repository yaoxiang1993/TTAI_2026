package com.example.ttai.ui.activity


import android.content.Intent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.ttai.R
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.Character
import com.example.ttai.network.NetworkModule
import com.example.ttai.utils.Constants
import com.example.ttai.utils.KeyboardManager
import com.example.ttai.utils.LayoutUtils
import com.example.ttai.databinding.ActivityVideoCallBinding
import com.example.ttai.intent.UserHomeIntent
import com.example.ttai.intent.VideoCallIntent
import com.example.ttai.state.VideoCallState
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

        binding.tvVideoTip.isVisible = state.isConnect
        binding.tvConnectTip.isVisible = state.isConnect

        binding.rlyTitle.isVisible = !state.isConnect
        binding.inputContainer.isVisible = !state.isConnect

    }
    /**
     * 加载背景图片
     */
    private fun loadBackgroundImage(imageUrl: String) {
        ImageUtils.loadImageToBG(this,imageUrl,binding.ivBackground)
    }
}
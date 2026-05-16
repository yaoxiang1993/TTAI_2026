package com.example.ttai.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ttai.utils.ToastUtils
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ttai.R
import com.example.ttai.databinding.ActivityChatBinding
import com.example.ttai.adapter.ChatAdapter
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.intent.ChatIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.ChatState
import com.example.ttai.utils.Constants
import com.example.ttai.utils.KeyboardManager
import com.example.ttai.utils.LayoutUtils
import com.example.ttai.ui.vm.ChatViewModel
import com.example.ttai.ui.vm.ChatViewModelFactory
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.MyBluetoothManager
import com.example.ttai.event.AICreatedEvent
import com.example.ttai.event.ClearChatEvent
import com.example.ttai.intent.MyFragmentIntent
import com.example.ttai.utils.ImageUtils
import com.example.ttai.utils.SystemUIUtils
import org.greenrobot.eventbus.EventBus

class ChatActivity : BaseMviActivity<ChatIntent, ChatState, ChatViewModel, ActivityChatBinding>() {
    private var characterId = ""
    private var conversationId  =  ""
    private val fromSelectTag by lazy { intent.getBooleanExtra(Constants.FROM_SELECT_TAG, false) }
    override val viewModel: ChatViewModel by viewModels { 
        ChatViewModelFactory(NetworkModule.provideApiService(), this)
    }

    // 更新消息成功
    private val editMessageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 从EditeNameActivity返回，获取新的用户名
            val content = result.data?.getStringExtra("new_name")
            val messageId = result.data?.getStringExtra(Constants.STRING_ID_KEY)
            if (!content.isNullOrEmpty()) {
                sendIntent(ChatIntent.UpdateMessage(content,messageId))
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        characterId = intent?.getStringExtra(Constants.CHARACTER_ID_KEY)?:""
        conversationId = intent?.getStringExtra(Constants.CONVERSATION_ID_KEY)?:""
        if (characterId.isNotEmpty()) {
            // 加载指定角色详情
            sendIntent(ChatIntent.LoadCharacterDetail(characterId))
            if (conversationId.isNotEmpty()){
                sendIntent(ChatIntent.LoadMessages(conversationId))
            }else{
                // 加载默认角色聊天历史
                sendIntent(ChatIntent.LoadDefaultCharacterMessages())
            }
        }
    }


    override val binding by viewBinding { ActivityChatBinding.inflate(it) }
    private val chatAdapter = ChatAdapter(
        onRollbackClick = { message ->
            val dialog = TwoButtonDialogFragment.newInstance(
                     message = "是否回溯该条内容以下的所有内容",
                    positiveText = "确认",
                    negativeText = "取消"
             ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
                     override fun onPositiveClick() {
                    sendIntent(ChatIntent.RollbackMessages(message))
                    }

                 override fun onNegativeClick() {
                  }
              })
               dialog.show(supportFragmentManager, "RollbackMessages")

                          },
        onReloadMessageClick = { message ->
                val dialog = TwoButtonDialogFragment.newInstance(
                 message = "是否重新生成AI回复",
                 positiveText = "确认",
                negativeText = "取消"
              ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
                 override fun onPositiveClick() {
                    sendIntent(ChatIntent.RegenerateMessage)
                 }

                 override fun onNegativeClick() {
                  }
             })
              dialog.show(supportFragmentManager, "RegenerateMessage")

                               },
        onEditeClick = { message ->

            val intent = Intent(this, EditeMessageActivity::class.java).apply {
                putExtras(Bundle().apply{
                    putParcelable(Constants.OBJECT_KEY, message)
                })
            }
            editMessageLauncher.launch(intent)
                       },
        onDeteleClick = { message ->
            val dialog = TwoButtonDialogFragment.newInstance(
                message = "是否删除该条聊天内容",
                positiveText = "确认",
                negativeText = "取消"
            ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
                override fun onPositiveClick() {
                    sendIntent(ChatIntent.DeleteMessages(message))
                }

                override fun onNegativeClick() {
                }
            })
            dialog.show(supportFragmentManager, "Detele")

        }
    )
    private var loadingProgressDialog: android.app.AlertDialog? = null
    private var lastMessageCount: Int = 0
    private var keepPositionOnNextUpdate: Boolean = false
    private var anchorFirstVisible: Int = 0
    private var anchorOffset: Int = 0

    override fun setupViews() {
        characterId = intent?.getStringExtra(Constants.CHARACTER_ID_KEY)?:""
        conversationId = intent?.getStringExtra(Constants.CONVERSATION_ID_KEY)?:""
        SystemUIUtils.setTransparentStatusBarVisible(this)
        SystemUIUtils.setStatusBarIconColor(this, true) // 白色图标
        try {
             EventBus.getDefault().register(this)
             Log.d("ChatActivity", "EventBus注册成功")
        } catch (e: Exception) {
             Log.w("ChatActivity", "EventBus注册失败: ${e.message}")
        }

        // 设置点击外部区域隐藏键盘
        KeyboardManager.setupHideKeyboardOnTouchOutside(this, binding.root, binding.etMessage)
        Log.d("FeaturedFragment", "Navigated to ChatActivity with characterId: ${characterId}    conversationId :${conversationId}  ")

        // 设置characterId到ViewModel
        viewModel.characterId = characterId
        viewModel.conversationId = conversationId

        // 显示加载弹框
        showLoadingProgressDialog("")

        // 根据是否有characterId决定加载角色详情还是默认角色聊天历史
        if (characterId.isNotEmpty()) {
            // 加载指定角色详情
            sendIntent(ChatIntent.LoadCharacterDetail(characterId))
            if (conversationId.isNotEmpty()){
                sendIntent(ChatIntent.LoadMessages(conversationId))
            }else{
                // 加载默认角色聊天历史
                sendIntent(ChatIntent.LoadDefaultCharacterMessages())
            }
        } else {
            // 加载默认角色聊天历史
            sendIntent(ChatIntent.LoadDefaultCharacterMessages())
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // 滚动到底部
            }
            adapter = chatAdapter
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            android.util.Log.d("ChatActivity", "加载上一页")
            val lm = binding.recyclerView.layoutManager as LinearLayoutManager
            anchorFirstVisible = lm.findFirstVisibleItemPosition()
            anchorOffset = lm.findViewByPosition(anchorFirstVisible)?.top ?: 0
            keepPositionOnNextUpdate = true
            sendIntent(ChatIntent.LoadMoreMessages)
            binding.swipeRefreshLayout.isRefreshing = false
        }
        // 设置输入框监听
        setupInputListeners()

        // 设置软键盘监听
        setupKeyboardListener()
        // 设置导航栏不遮挡 输入框
        LayoutUtils.setupInputContainerPadding(this, R.id.input_container)

        // 设置返回按钮点击事件
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        binding.tvSendMessage.setOnClickListener {
            sendMessage()
        }
        binding.ivReload.setOnClickListener {
            // 清除聊天记录
            showClearHistoryDialog()
        }
        binding.ivMenu.setOnClickListener {
            // 进入AI设置页面
            val intent = Intent(this, AISettingActivity::class.java).apply {
                putExtra(Constants.CHARACTER_KEY, viewModel.character)
            }
            startActivity(intent)
        }

        // 为llyHeard控件添加点击事件，点击后打开AIDetailsActivity
        binding.llyHeard.setOnClickListener {
            val intent = Intent(this, AIDetailsActivity::class.java).apply {
                putExtras(Bundle().apply{
                    putExtra(Constants.CHARACTER_KEY,viewModel.character)
                }) // 可以根据实际需要传递AI的ID
            }
            startActivity(intent)
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
            if (hasFocus) {
                // 当输入框获得焦点时，延迟滚动到底部
                binding.recyclerView.postDelayed({
                    if (chatAdapter.itemCount > 0) {
                        binding.recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }, 300)
            }
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
                // 软键盘显示时，滚动到底部
                binding.recyclerView.postDelayed({
                    if (chatAdapter.itemCount > 0) {
                        binding.recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }, 100)

                // 手动调整输入框位置
                binding.inputContainer.post {
                    val params = binding.inputContainer.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
                    params.bottomMargin = imeInsets.bottom
                    binding.inputContainer.layoutParams = params
                }
            } else {
                // 软键盘隐藏时，恢复输入框位置
                binding.inputContainer.post {
                    val params = binding.inputContainer.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
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
            sendIntent(ChatIntent.SendMessage(message))
            // 发送后滚动到底部
            binding.recyclerView.postDelayed({
                if (chatAdapter.itemCount > 0) {
                    binding.recyclerView.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }, 100) // 延迟100ms确保消息已添加到列表
        }
    }

    private fun showClearHistoryDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "清空所有聊天记录，\n" +
                    "是否继续呢？\n",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 处理确认逻辑
                sendIntent(ChatIntent.ClearMessage)
            }

            override fun onNegativeClick() {
                // 处理取消逻辑
                ToastUtils.showShort(this@ChatActivity, "已取消")
            }
        })

        // 显示对话框
        dialog.show(supportFragmentManager, "TwoButtonDialog")
    }

    override fun render(state: ChatState) {
        super.render(state)

        // 处理加载状态
        if (state.isLoading || state.isLoadingDefaultChat) {
            showLoadingProgressDialog(state.loadingMessages)
        } else {
            hideLoadingProgressDialog()
        }

        chatAdapter.submitList(state.messages.toList()) {
            if (state.isTyping && chatAdapter.itemCount > 0) {
                binding.recyclerView.post {
                    val lm = binding.recyclerView.layoutManager as? LinearLayoutManager ?: return@post
                    lm.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }
        }

        // 更新输入框状态
        if (state.isTyping) {
            // 建议：流式传输时禁用发送按钮，防止请求堆叠
            binding.tvSendMessage.isEnabled = false
            binding.tvSendMessage.alpha = 0.5f
            binding.etMessage.hint = "角色正在思考中..."
        } else {
            binding.tvSendMessage.isEnabled = true
            binding.tvSendMessage.alpha = 1.0f
            binding.etMessage.hint = "发送信息给他/她吧~~"
        }

        state.character?.name.let { name ->
            binding.tvName.text = name
            chatAdapter.name = name
        }
        // 处理头像 - 添加安全检查
        state.character?.avatarUrl?.let { characterAvatar ->
            if (characterAvatar.isNotEmpty()) {
                // 检查是否是本地MediaDocumentsProvider URI，如果是则跳过加载
                if (characterAvatar.contains("com.android.providers.media.documents") ||
                    characterAvatar.startsWith("content://com.android.providers.media.documents")) {
                    Log.w("ChatActivity", "跳过加载本地MediaDocumentsProvider URI: $characterAvatar")
                    binding.ivHeard.setImageResource(android.R.drawable.ic_menu_gallery)
                    return@let
                }

                try {
                    loadBackgroundImage(characterAvatar)
                    Glide.with(binding.ivHeard.context)
                        .load(characterAvatar)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .transform(CenterCrop(), CircleCrop())
                        .into(binding.ivHeard)
                } catch (e: Exception) {
                    Log.e("ChatActivity", "Glide加载头像异常", e)
                    binding.ivHeard.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }
        if (state.isClearMessages) {
            // 清空了历史消息，要重新请求默认信息
            sendIntent(ChatIntent.LoadMessages(conversationId))
        }

    }

    /**
     * 显示加载弹框
     */
    private fun showLoadingProgressDialog(loadingMessages :String) {
        if (loadingProgressDialog?.isShowing == true) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_loading_progress, null)
        loadingProgressDialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        if (loadingMessages.isNotEmpty()){
            dialogView.findViewById<TextView>(R.id.tvloading).text = loadingMessages
            dialogView.findViewById<TextView>(R.id.tvContent).isVisible=false
        }

        // 设置弹框宽度为屏幕的70%
        loadingProgressDialog?.let { dialog ->
            dialog.show()
            val window = dialog.window
            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.7).toInt()
            window?.setLayout(width, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    /**
     * 隐藏加载弹框
     */
    private fun hideLoadingProgressDialog() {
        loadingProgressDialog?.dismiss()
        loadingProgressDialog = null
    }

    /**
     * 加载背景图片
     */
    private fun loadBackgroundImage(imageUrl: String) {
        ImageUtils.loadImageToBG(this,imageUrl,binding.ivBackground)
    }
    override fun onBackPressed() {
        if (fromSelectTag) {
            // 如果是从SelectTagActivity进入的，返回MainActivity
            Log.d("ChatActivity", "从SelectTagActivity进入，跳转到MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            // 其他方式进入的，使用默认的返回逻辑
            Log.d("ChatActivity", "其他方式进入，使用默认返回逻辑")
            finish()
        }
        super.onBackPressed()
    }


    /**
     * 处理AI创建成功事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onAICreated(event: AICreatedEvent) {
        if (event.success && characterId == event.createSuccessAIID ) {
            // 刷新AI角色列表
            android.util.Log.d("MyFragment", "开始刷新AI角色列表")
            if (characterId.isNotEmpty()) {
                // 加载指定角色详情
                sendIntent(ChatIntent.LoadCharacterDetail(characterId))
                if (conversationId.isNotEmpty()) {
                    sendIntent(ChatIntent.LoadMessages(conversationId))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确保加载弹框被关闭
        hideLoadingProgressDialog()
        try {
            EventBus.getDefault().unregister(this)
            Log.d("ChatActivity", "EventBus注销成功")
        } catch (e: Exception) {
            Log.w("ChatActivity", "EventBus注销失败: ${e.message}")
        }
    }
}
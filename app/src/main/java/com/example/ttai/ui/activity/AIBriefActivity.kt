package com.example.ttai.ui.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.example.ttai.utils.ToastUtils
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ttai.R
import com.example.ttai.databinding.ActivityAiBriefBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.ui.dialog.AIListDialogFragment
import com.example.ttai.intent.AIBriefIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.AIBriefState
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.AIBriefViewModel
import com.example.ttai.ui.vm.AIBriefViewModelFactory
import com.example.ttai.bean.Character
import com.example.ttai.bean.Conversation
import com.example.ttai.event.AICreatedEvent
import com.example.ttai.intent.ChatIntent
import com.example.ttai.utils.ImageUtils
import com.example.ttai.utils.LayoutUtils
import com.example.ttai.utils.MMKVUtils

class AIBriefActivity : BaseMviActivity<AIBriefIntent, AIBriefState, AIBriefViewModel, ActivityAiBriefBinding>() {
    
    override val viewModel: AIBriefViewModel by viewModels { 
        AIBriefViewModelFactory(NetworkModule.provideApiService(), this)
    }
    private val character by lazy { intent.getParcelableExtra<Character>(Constants.CHARACTER_KEY) }
    override val binding by viewBinding { ActivityAiBriefBinding.inflate(it) }

    // 展开/收起状态
    private var isExpanded = false
    // 展开/收起状态
    private var characterId :String?= ""

    override fun setupViews() {
        // 检查角色数据是否存在
        if (character == null) {
            ToastUtils.showShort(this, "角色数据无效")
            finish()
            return
        }
        // 设置点击事件
        setupClickListeners()
        
        // 设置tvBriefingNote的展开/收起功能
        setupBriefingNoteExpandCollapse()
        characterId = character!!.id
        Log.e("YXTEST"," characterID   ${character!!.id}   conversationId ${character!!.conversationId}  ")
        // 初始化，传递inChatSlots状态
        sendIntent(AIBriefIntent.Initialize(character!!))
        character?.let {
            if (isAiOwner()) {
                binding.tvToDream.text = "编辑智能体"
            }
        }
    }
    fun isAiOwner():Boolean{
        return character?.creatorId == MMKVUtils.getUserProfile()?.id
    }


    override fun render(state: AIBriefState) {
        super.render(state)

        // 更新角色信息显示
        updateCharacterDisplay(state)
        
        // 更新tvToDream按钮状态
        updateToDreamButtonState(state)
        
        // 处理导航到聊天页面
        if (state.navigateToChat) {
            navigateToChatActivity(state)
            viewModel.resetNavigationState()
        }
        
        // 显示错误信息
        state.error?.let { error ->
            ToastUtils.showShort(this, error)
            viewModel.clearError()
        }
        
        // 处理替换对话框显示
        if (state.showReplaceDialog) {
            showReplaceDialog(state.conversations)
        }

        if (state.toCreatAI == true){
            val intent = Intent(this, CreateAIActivity::class.java).apply {
                putExtra("my_ai", character)
            }
            startActivity(intent)
        }
    }

    /**
     * 设置点击事件
     */
    private fun setupClickListeners() {
        // 返回按钮
        binding.ivBack.setOnClickListener {
            finish()
        }

        // 进入详情页
        binding.llyName.setOnClickListener {
            character?.let { char ->
                val intent = Intent(this, AIDetailsActivity::class.java).apply {
                    putExtras(Bundle().apply{
                        putExtra(Constants.CHARACTER_KEY, char)
                    }) // 可以根据实际需要传递AI的ID
                }
                startActivity(intent)
            }
        }

        // 入梦按钮
        binding.tvToDream.setOnClickListener {
            character?.let { char ->
                val characterId = char.id
                if (isAiOwner()) {
                    sendIntent(AIBriefIntent.ToCreatAI(character))
                }else{
                    sendIntent(AIBriefIntent.ToggleChatSlot(characterId))
                }
            }
        }
    }
    
    /**
     * 更新角色信息显示
     */
    private fun updateCharacterDisplay(state: AIBriefState) {
        // 更新角色名称
        binding.tvUserName.text = state.characterName
        // 更新角色头像
        if (!state.characterAvatar.isNullOrEmpty()) {
            loadBackgroundImage(state.characterAvatar)
            Glide.with(this)
                .load(state.characterAvatar)
                .placeholder(R.mipmap.icon_heard_cricle)
                .error(R.mipmap.icon_heard_cricle)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.iv)
        } else {
            binding.iv.setImageResource(R.mipmap.icon_heard_cricle)
        }
        // 更新简介
        binding.tvBriefingNote.isVisible = state.briefIntro?.isNotEmpty() == true
        binding.tvBriefingNote.text = state.briefIntro
        
        // 如果简介不为空，重新检查是否需要显示展开按钮
        if (state.briefIntro?.isNotEmpty() == true) {
            checkAndShowExpandButton()
        }
        // 更新开场白
        binding.tvPrologue.isVisible = state.openingLine?.isNotEmpty() == true
        binding.tvPrologue.text = state.openingLine

    }
    
    /**
     * 更新tvToDream按钮状态
     */
    private fun updateToDreamButtonState(state: AIBriefState) {
        android.util.Log.d("AIBriefActivity", "updateToDreamButtonState - isAddingFriend: ${state.isAddingFriend}, inChatSlots: ${state.inChatSlots}")
        
        if (state.isAddingFriend) {
            // 显示处理中的状态
            val processingText = if (state.inChatSlots) "取消中..." else "添加中..."
            binding.tvToDream.text = processingText
            binding.tvToDream.isEnabled = false
            // 尝试使用setBackgroundDrawable方法
            try {
                val drawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_6b6b6b_r50)
                binding.tvToDream.setBackgroundDrawable(drawable)
            } catch (e: Exception) {
                android.util.Log.e("AIBriefActivity", "设置背景失败", e)
                binding.tvToDream.setBackgroundResource(R.drawable.bg_6b6b6b_r50)
            }
            android.util.Log.d("AIBriefActivity", "设置处理中状态背景: bg_6b6b6b_r50")
        } else {
            // 根据inChatSlots状态设置按钮
            if (state.inChatSlots) {
                // 已入梦状态
                binding.tvToDream.text = "已入梦"
                binding.tvToDream.isEnabled = true
                // 尝试使用setBackgroundDrawable方法
                try {
                    val drawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_6b6b6b_r50)
                    binding.tvToDream.setBackgroundDrawable(drawable)
                } catch (e: Exception) {
                    android.util.Log.e("AIBriefActivity", "设置背景失败", e)
                    binding.tvToDream.setBackgroundResource(R.drawable.bg_6b6b6b_r50)
                }
                android.util.Log.d("AIBriefActivity", "设置已入梦状态背景: bg_6b6b6b_r50")
            } else {
                // 未入梦状态
                binding.tvToDream.text = "入梦"
                binding.tvToDream.isEnabled = true
                // 尝试使用setBackgroundDrawable方法
                try {
                    val drawable = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_primary_r50)
                    binding.tvToDream.setBackgroundDrawable(drawable)
                } catch (e: Exception) {
                    android.util.Log.e("AIBriefActivity", "设置背景失败", e)
                    binding.tvToDream.setBackgroundResource(R.drawable.bg_primary_r50)
                }
                android.util.Log.d("AIBriefActivity", "设置未入梦状态背景: bg_primary_r50")
            }
        }
        
        // 强制刷新视图
        binding.tvToDream.invalidate()
        
        // 确保在主线程中执行UI更新
        binding.tvToDream.post {
            binding.tvToDream.requestLayout()
        }
    }
    /**
     * 跳转到聊天页面
     */
    private fun navigateToChatActivity(state: AIBriefState) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra(Constants.CHARACTER_ID_KEY, state.characterId)
            putExtra(Constants.CONVERSATION_ID_KEY, state.conversationId)
        }
        startActivity(intent)
        finish() // 关闭当前页面
    }
    
    /**
     * 显示替换对话框
     */
    private fun showReplaceDialog(conversations: List<Conversation>) {
        val dialog = AIListDialogFragment.newInstance(
            message = getString(R.string.ai_release_or_add),
            positiveText = "确定",
            negativeText = "取消",
            conversations = conversations
        ).setOnButtonClickListener(object : AIListDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 确定按钮点击，隐藏对话框
                sendIntent(AIBriefIntent.HideReplaceDialog)
            }

            override fun onNegativeClick() {
                // 取消按钮点击，隐藏对话框
                sendIntent(AIBriefIntent.HideReplaceDialog)
            }
        }).setOnReplaceClickListener(object : AIListDialogFragment.OnReplaceClickListener {
            override fun onReplaceClick(conversation: Conversation) {
                // 替换按钮点击，只是标记选中状态，不执行实际替换
                // 这里可以添加一些UI反馈，比如Toast提示
                android.util.Log.d("AIBriefActivity", "选中AI: ${conversation.character.name}")
            }
        }).setOnConfirmReplaceListener(object : AIListDialogFragment.OnConfirmReplaceListener {
            override fun onConfirmReplace(conversation: Conversation?) {
                // 取消按钮点击，隐藏对话框
                sendIntent(AIBriefIntent.HideReplaceDialog)
                // 确认替换按钮点击，执行真正的替换逻辑
                if (conversation != null) {
                    sendIntent(AIBriefIntent.ReplaceAI(conversation))
                } else {
                    // 没有选中任何AI，显示提示
                    ToastUtils.showShort(this@AIBriefActivity, "请先选择一个AI进行替换")
                }
            }
        })
        dialog.show(supportFragmentManager, "replace_dialog")
    }

    /**
     * 加载背景图片
     */
    private fun loadBackgroundImage(imageUrl: String) {
        ImageUtils.loadImageToBG(this,imageUrl,binding.ivBackground)
    }
    override fun shouldSetupNavigationBarAdapter(): Boolean {
        return true
    }
    
    override fun setupNavigationBarAdapter() {
        // 为输入框区域添加虚拟导航栏高度适配
        LayoutUtils.setupInputContainerPadding(this, R.id.clyGroup)
    }
    
    /**
     * 设置tvBriefingNote的展开/收起功能
     */
    private fun setupBriefingNoteExpandCollapse() {
        // 设置最大行数为3行
        binding.tvBriefingNote.maxLines = 3
        binding.tvBriefingNote.ellipsize = android.text.TextUtils.TruncateAt.END
        
        // 初始时隐藏展开按钮
        binding.ivText.visibility = android.view.View.GONE
        
        // 设置展开按钮的点击事件
        binding.ivText.setOnClickListener {
            toggleExpandCollapse()
        }
    }
    
    /**
     * 检查是否需要显示展开按钮
     */
    private fun checkAndShowExpandButton() {
        val textView = binding.tvBriefingNote
        val layout = textView.layout
        
        if (layout != null) {
            // 检查文本是否超过3行
            val lineCount = layout.lineCount
            val isTextOverflow = lineCount > 3 || (lineCount == 3 && layout.getEllipsisCount(2) > 0)
            
            if (isTextOverflow) {
                binding.ivText.visibility = android.view.View.VISIBLE
            } else {
                binding.ivText.visibility = android.view.View.GONE
            }
        } else {
            // 如果layout为null，使用ViewTreeObserver来延迟检查
            textView.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    // 移除监听器，避免重复调用
                    textView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    
                    val layout = textView.layout
                    if (layout != null) {
                        val lineCount = layout.lineCount
                        val isTextOverflow = lineCount > 3 || (lineCount == 3 && layout.getEllipsisCount(2) > 0)
                        
                        if (isTextOverflow) {
                            binding.ivText.visibility = android.view.View.VISIBLE
                        } else {
                            binding.ivText.visibility = android.view.View.GONE
                        }
                    }
                }
            })
        }
    }
    
    /**
     * 切换展开/收起状态
     */
    private fun toggleExpandCollapse() {
        isExpanded = !isExpanded
        
        if (isExpanded) {
            // 展开：显示全部内容
            binding.tvBriefingNote.maxLines = Int.MAX_VALUE
            binding.tvBriefingNote.ellipsize = null
        } else {
            // 收起：只显示3行
            binding.tvBriefingNote.maxLines = 3
            binding.tvBriefingNote.ellipsize = android.text.TextUtils.TruncateAt.END
        }
    }

 /*   @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onAICreated(event: AICreatedEvent) {
        if (event.success && characterId == event.createSuccessAIID ) {
            // 刷新AI角色列表
            android.util.Log.d("MyFragment", "开始刷新AI角色列表")
            if (characterId?.isNotEmpty() == true) {
                // 加载指定角色详情
            }
        }
    }
    */
}
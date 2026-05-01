package com.example.ttai.ui.activity

import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.ViewGroup
import com.example.ttai.utils.ToastUtils
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ttai.R
import com.example.ttai.databinding.ActivityAiDetailsBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.Character
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.intent.AIDetailsIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.AIDetailsState
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.AIDetailsViewModel
import com.example.ttai.ui.vm.AIDetailsViewModelFactory
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import com.bumptech.glide.Glide
import com.example.ttai.bean.Conversation
import com.example.ttai.ui.dialog.AIListDialogFragment
import com.example.ttai.utils.ImageUtils
import com.example.ttai.utils.SystemUIUtils
import com.example.ttai.utils.formatPopularity
import com.google.android.flexbox.FlexboxLayout

class AIDetailsActivity : BaseMviActivity<AIDetailsIntent, AIDetailsState, AIDetailsViewModel, ActivityAiDetailsBinding>() {
    override val viewModel: AIDetailsViewModel by viewModels{
        AIDetailsViewModelFactory(NetworkModule.provideApiService())
    }
    override val binding by viewBinding { ActivityAiDetailsBinding.inflate(it) }
    
    // 展开/收起状态
    private var isExpanded = false
    var creator_id :String = ""

    override fun setupViews() {
        SystemUIUtils.setTransparentStatusBarVisible(this)
        SystemUIUtils.setStatusBarIconColor(this, true) // 白色图标
        // 初始化ViewModel的Repository
        viewModel.initRepository(this)
        
        // 获取传递的参数
        val character = intent.getParcelableExtra<Character>(Constants.CHARACTER_KEY)
        viewModel.characterId = character?.id.toString()
        // 更新UI显示传递的数据
        binding.tvName.text = character?.name
        binding.tvId.text = "ID:${character?.id}"
        binding.tvBriefingNote.text = character?.briefIntro
        binding.tvPrologue.text = character?.openingLine
        
        // 设置tvBriefingNote的展开/收起功能
        setupBriefingNoteExpandCollapse()
        
        // 根据gender设置图标
        character?.gender?.let { gender ->
            val iconRes = when (gender.lowercase()) {
                "male" -> R.mipmap.icon_man
                "female" -> R.mipmap.icon_woman
                else ->  R.mipmap.icon_other
            }
            iconRes?.let { res ->
                binding.ivSex.setImageResource(res)
            }
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.llyAuthor.setOnClickListener {
            if (!TextUtils.isEmpty(creator_id) &&"system" != creator_id){
                val intent = Intent(this, UserHomeActivity::class.java).apply {
                    putExtra(Constants.USER_ID_KEY, character?.creatorId)
                }
                startActivity(intent)
            }else{
                ToastUtils.showShort(this,"作者为系统，无法进入主页")
            }
        }


        binding.tvCancelDream.setOnClickListener {
            // 检查当前状态，决定显示哪个对话框
            val currentState = viewModel.state.value
            if (character?.isMyCharacter == true){
                val intent = Intent(this, CreateAIActivity::class.java).apply {
                    putExtra("my_ai", character)
                }
                startActivity(intent)
            }else if (currentState.isInChatSlot) {
                showRemoveFromSlotDialog()
            } else {
                showAddToSlotDialog()
            }
        }
        binding.tvCancelDreamToMyAI.isVisible = character?.isMyCharacter == true
        binding.tvCancelDreamToMyAI.setOnClickListener{
            val currentState = viewModel.state.value
            if (currentState.isInChatSlot) {
                showRemoveFromSlotDialog()
            } else {
                showAddToSlotDialog()
            }
        }
        
        // 设置tvId点击复制功能
        binding.tvId.setOnClickListener {
            copyIdToClipboard()
        }
        sendIntent(AIDetailsIntent.LoadData)
    }

    override fun render(state: AIDetailsState) {
        super.render(state)
        
        // 更新按钮文案和状态
        updateButtonState(state)

        // 处理替换对话框显示
        if (state.showReplaceDialog) {
            showReplaceDialog(state.conversations)
        }
        
        // 处理操作结果
        if (state.slotToggleSuccess) {
            val message = if (state.isInChatSlot) "已成功入梦" else "已取消入梦"
            ToastUtils.showShort(this, message)
            // 重置成功状态
            viewModel.resetSlotToggleSuccess()
        }
        if (state.character?.avatarUrl?.isNotEmpty() == true){
            loadBackgroundImage(state.character.avatarUrl)
        }

        binding.tvHuahuo.text = state.character?.popularity?.formatPopularity()
        creator_id = state.character?.creatorId.toString()
        state.character?.creator_info?.let {
            binding.tvAuthorName.text = it.creator_name
            if (!TextUtils.isEmpty(it.creator_avatar)){
                Glide.with(this)
                    .load(it.creator_avatar)
                    .placeholder(R.mipmap.icon_heard_cricle)
                    .error(R.mipmap.icon_heard_cricle)
                    .circleCrop()
                    .into(binding.ivHeard)
            }
        }
        // 动态添加性格标签
        state.character?.personalityTags?.let { tags ->
            setupPersonalityTags(tags)
        }
    }
    
    /**
     * 更新按钮状态和文案
     */
    private fun updateButtonState(state: AIDetailsState) {
        if (state.isTogglingSlot) {
            binding.tvCancelDream.isEnabled = false
            binding.tvCancelDreamToMyAI.isEnabled = false
        } else {
            if (state.character?.isMyCharacter == true){
                binding.tvCancelDream.text = "编辑智能体"
                binding.tvCancelDream.setBackgroundResource(R.drawable.bg_primary_r50)
                if (state.isInChatSlot) {
                    binding.tvCancelDreamToMyAI.text = "已入梦"
                    binding.tvCancelDreamToMyAI.setBackgroundResource(R.drawable.bg_6b6b6b_r50)
                } else {
                    binding.tvCancelDreamToMyAI.text = "入梦"
                    binding.tvCancelDreamToMyAI.setBackgroundResource(R.drawable.bg_primary_r50)
                }
            }else if (state.isInChatSlot) {
                binding.tvCancelDream.text = "已入梦"
                binding.tvCancelDream.setBackgroundResource(R.drawable.bg_6b6b6b_r50)
            } else {
                binding.tvCancelDream.text = "入梦"
                binding.tvCancelDream.setBackgroundResource(R.drawable.bg_primary_r50)
            }
            binding.tvCancelDream.isEnabled = true
            binding.tvCancelDreamToMyAI.isEnabled = true
        }
    }
    
    /**
     * 显示添加到槽位的确认对话框
     */
    private fun showAddToSlotDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "将该AI智能体添加到聊天槽位，\n是否继续？",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 执行添加到槽位操作
                sendIntent(AIDetailsIntent.ToggleChatSlot(viewModel.characterId))
            }

            override fun onNegativeClick() {
                // 取消操作
            }
        })
        dialog.show(supportFragmentManager, "AddToSlotDialog")
    }
    
    /**
     * 显示从槽位移除的确认对话框
     */
    private fun showRemoveFromSlotDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "取消入梦该角色后将永久删除所有聊天记录，\n是否继续？",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 执行从槽位移除操作
                sendIntent(AIDetailsIntent.ToggleChatSlot(viewModel.characterId))
            }

            override fun onNegativeClick() {
                // 取消操作
            }
        })
        dialog.show(supportFragmentManager, "RemoveFromSlotDialog")
    }
    /**
     * 加载背景图片
     */
    private fun loadBackgroundImage(imageUrl: String) {
        ImageUtils.loadImageToBG(this,imageUrl,binding.ivBackground)
    }
    
    /**
     * 设置性格标签
     */
    private fun setupPersonalityTags(tags: List<String>) {
        // 清除现有的标签
        binding.llyTag.removeAllViews()
        
        // 为每个标签创建TextView
        tags.forEach { tag ->
            val tagTextView = createTagTextView(tag)
            binding.llyTag.addView(tagTextView)
        }
    }
    
    /**
     * 创建标签TextView
     */
    private fun createTagTextView(tag: String): TextView {
        return TextView(this).apply {
            text = "# $tag"
            textSize = 10f
            setTextColor(ContextCompat.getColor(context, R.color.white))
            setBackgroundResource(R.drawable.bg_b3383838_r10)
            
            // 设置内边距
            val padding = (8 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)
            
            // 设置外边距
            val margin = (5 * resources.displayMetrics.density).toInt()
            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                // 1. 设置右侧间距 (水平间距)
                this.marginEnd = margin
                // 2. 🌟 核心：设置底部间距 (垂直间距/上下间距)
                (this as ViewGroup.MarginLayoutParams).bottomMargin = margin
            }
            this.layoutParams = layoutParams
        }
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
                sendIntent(AIDetailsIntent.HideReplaceDialog)
            }

            override fun onNegativeClick() {
                // 取消按钮点击，隐藏对话框
                sendIntent(AIDetailsIntent.HideReplaceDialog)
            }
        }).setOnReplaceClickListener(object : AIListDialogFragment.OnReplaceClickListener {
            override fun onReplaceClick(conversation: Conversation) {
                // 替换按钮点击，只是标记选中状态，不执行实际替换
                // 这里可以添加一些UI反馈，比如Toast提示
                android.util.Log.d("FeaturedFragment", "选中AI: ${conversation.character.name}")
            }
        }).setOnConfirmReplaceListener(object : AIListDialogFragment.OnConfirmReplaceListener {
            override fun onConfirmReplace(conversation: Conversation?) {
                // 取消按钮点击，隐藏对话框
                sendIntent(AIDetailsIntent.HideReplaceDialog)
                // 确认替换按钮点击，执行真正的替换逻辑
                if (conversation != null) {
                    sendIntent(AIDetailsIntent.ReplaceAI(conversation))
                } else {
                    // 没有选中任何AI，显示提示
                    ToastUtils.showShort(this@AIDetailsActivity, "请先选择一个AI进行替换")
                }
            }
        })

        dialog.show(supportFragmentManager, "AIReplaceDialog")
    }
    /**
     * 复制ID到剪贴板
     */
    private fun copyIdToClipboard() {
        val idText = binding.tvId.text.toString()
        // 提取ID部分，去掉"ID:"前缀
        val id = if (idText.startsWith("ID:")) {
            idText.substring(3).trim()
        } else {
            idText
        }
        
        // 复制到剪贴板
        val clipboard = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("AI ID", id)
        clipboard.setPrimaryClip(clip)
        
        // 显示Toast提示
        ToastUtils.showShort(this, "ID已复制到剪贴板")
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
        
        // 使用ViewTreeObserver来监听布局完成后的状态
        binding.tvBriefingNote.viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 移除监听器，避免重复调用
                binding.tvBriefingNote.viewTreeObserver.removeOnGlobalLayoutListener(this)
                
                // 检查是否需要显示展开按钮
                checkAndShowExpandButton()
            }
        })
        
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

    override fun shouldSetupNavigationBarAdapter(): Boolean {
        return true
    }
}
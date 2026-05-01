package com.example.ttai.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import com.example.ttai.utils.ToastUtils
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ttai.R
import com.example.ttai.databinding.FragmentFeaturedBinding
import com.example.ttai.ui.activity.AIDetailsActivity
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.Character
import com.example.ttai.bean.Conversation
import com.example.ttai.event.AICreatedEvent
import com.example.ttai.ui.dialog.AIListDialogFragment
import com.example.ttai.event.ChatSlotChangedEvent
import com.example.ttai.intent.ChatIntent
import com.example.ttai.intent.FeaturedFragmentIntent
import com.example.ttai.intent.SynthesizeFragmentIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.FeaturedFragmentState
import com.example.ttai.ui.activity.ChatActivity
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.FeaturedFragmentViewModel
import com.example.ttai.ui.vm.FeaturedFragmentViewModelFactory
import com.example.ttai.ui.vm.SharedDataViewModel
import com.example.ttai.utils.ImageUtils

/**
 * 自定义NestedScrollView，用于处理手势检测
 */
class GestureNestedScrollView @JvmOverloads constructor(
    context: android.content.Context,
    attrs: android.util.AttributeSet? = null,
    defStyleAttr: Int = 0
) : androidx.core.widget.NestedScrollView(context, attrs, defStyleAttr) {
    
    var onGestureListener: ((MotionEvent) -> Boolean)? = null
    
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // 先让手势监听器处理
        onGestureListener?.invoke(ev)?.let { if (it) return true }
        
        // 然后让父类处理
        return super.onTouchEvent(ev)
    }
    
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // 先让手势监听器处理
        onGestureListener?.invoke(ev)?.let { if (it) return true }
        
        // 然后让父类处理
        return super.onInterceptTouchEvent(ev)
    }
}

/**
 *
 * 梦境中的 精选分类
 * */
class FeaturedFragment : BaseMviFragment<FeaturedFragmentIntent, FeaturedFragmentState, FeaturedFragmentViewModel, FragmentFeaturedBinding>() {
    override val viewModel: FeaturedFragmentViewModel by viewModels { 
        FeaturedFragmentViewModelFactory(NetworkModule.provideApiService(), requireContext())
    }
    private val sharedViewModel: SharedDataViewModel by viewModels({ requireParentFragment() })
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentFeaturedBinding.inflate(inflater, container, attachToParent)
    }
    private var currentCharacterId = ""
    private var conversationId = ""
    private var currentCharacter: Character? = null

    // 手势检测器
    private lateinit var gestureDetector: GestureDetectorCompat
    
    // 展开/收起状态
    private var isExpanded = false
    
    // 滑动动画相关
    private var isAnimating = false
    private var slideDirection = 0 // 1: 向右, -1: 向左
    
    // 防止重复跳转的标志
    private var hasNavigatedToChat = false


    var featuredCharacters: List<Character> = emptyList()

    override fun setupViews() {
        try {
            org.greenrobot.eventbus.EventBus.getDefault().register(this)
            Log.d("FeaturedFragment", "EventBus注册成功")
        } catch (e: Exception) {
            Log.w("FeaturedFragment", "EventBus注册失败: ${e.message}")
        }
        sharedViewModel.isUnlimited.observe(viewLifecycleOwner) { isUnlimited ->
            // 当 isUnlimited 变化时，将新值转发给 B 自己的 ViewModel
            viewModel.updateIsUnlimitedParam(isUnlimited)
            sendIntent(FeaturedFragmentIntent.Initialize)
        }
        setupGestureDetector()
        setupClickListeners()
        setupBriefIntroExpandCollapse()
        sendIntent(FeaturedFragmentIntent.Initialize)
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetectorCompat(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Log.d("FeaturedFragment", "onFling called - diffX: ${e2.x - (e1?.x ?: 0f)}, velocityX: $velocityX")
                
                if (e1 == null || isAnimating) return false
                
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                
                // 确保是水平滑动（水平距离大于垂直距离）
                if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
                    // 设置最小滑动距离和速度阈值
                    if (kotlin.math.abs(diffX) > 100 && kotlin.math.abs(velocityX) > 100) {
                        Log.d("FeaturedFragment", "Gesture detected - diffX: $diffX, velocityX: $velocityX")
                        if (diffX > 0) {
                            // 向右滑动，加载上一条数据
                            slideDirection = 1
                            performSlideAnimation(false) {
                                sendIntent(FeaturedFragmentIntent.LoadPreviousData)
                            }
                        } else {
                            // 向左滑动，加载下一条数据
                            slideDirection = -1
                            performSlideAnimation(true) {
                                sendIntent(FeaturedFragmentIntent.LoadNextData)
                            }
                        }
                        return true
                    }
                }
                return false
            }
        })
        
        // 设置根布局的触摸监听
        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        
        // 为自定义NestedScrollView设置手势监听器
        binding.nestedScrollView.onGestureListener = { event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    private fun setupClickListeners() {
        // 设置"入梦"按钮点击事件
        binding.tvAddAI.setOnClickListener {
            // 发送添加AI的Intent
            sendIntent(FeaturedFragmentIntent.AddAI)
        }
        
        // 设置用户头像点击事件
        binding.llyName.setOnClickListener {
            // 跳转到 AI 详情页面
            val intent = Intent(requireContext(), AIDetailsActivity::class.java).apply {
                putExtras(Bundle().apply{
                    putExtra(Constants.CHARACTER_KEY,currentCharacter)
                }) // 可以根据实际需要传递AI的ID
            }
            startActivity(intent)
        }
    }

    override fun render(state: FeaturedFragmentState) {
        super.render(state)
        featuredCharacters = state.featuredCharacters
        // 添加日志来跟踪render调用
        Log.d("FeaturedFragment", "render called - isInDream: ${state.isInDream}, isAddingAI: ${state.isAddingAI}, hasNavigatedToChat: $hasNavigatedToChat")
        
        // 更新UI内容
        updateUI(state)
        
        // 如果角色ID发生变化，重置跳转标志
        if (currentCharacterId != state.currentCharacterId) {
            hasNavigatedToChat = false
            Log.d("FeaturedFragment", "角色ID变化，重置跳转标志")
        }
        state.currentCharacterId?.let {
            currentCharacterId = it
        }
        state.conversationId?.let {
            conversationId = it
        }
        currentCharacter = state.currentCharacter
        // 处理错误信息
        state.error?.let { error ->
            ToastUtils.showShort(requireContext(), error)
        }
        
        // 处理替换对话框显示
        if (state.showReplaceDialog) {
            showReplaceDialog(state.conversations)
        }

        // 只有在用户主动点击"入梦"按钮后才跳转到聊天页面
        // 避免在滑动切换数据时触发跳转
        if (state.isInDream && !isAnimating && !hasNavigatedToChat){
            hasNavigatedToChat = true
            navigateToChatActivity(state.currentCharacterId,state.conversationId)
        }

    }
    
    private fun updateUI(state: FeaturedFragmentState) {
        // 更新介绍文本
        if (state.openingLine?.isNotEmpty() == true) {
            binding.tvOpeningLine.text = state.openingLine
        }
        
        // 更新详细文本
        if (state.briefIntro?.isNotEmpty() == true) {
            binding.tvBriefIntro.text = state.briefIntro
            
            // 如果简介不为空，重新检查是否需要显示展开按钮
            checkAndShowExpandButton()
        }
        
        // 更新用户信息
        if (state.userName?.isNotEmpty() == true) {
            binding.tvUserName.text = state.userName
        }
        if (!state.currentCharacter?.avatarUrl.isNullOrEmpty()) {
            state.currentCharacter?.avatarUrl?.let { loadBackgroundImage(it) }
            Glide.with(this)
                .load(state.currentCharacter?.avatarUrl)
                .placeholder(R.mipmap.icon_heard_cricle)
                .error(R.mipmap.icon_heard_cricle)
                .circleCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.iv)
        } else {
            binding.iv.setImageResource(R.mipmap.icon_heard_cricle)
        }
        
        // 如果正在动画中，添加淡入效果
        if (isAnimating) {
            addFadeInEffect()
        }
        // 更新入梦按钮文案和状态
        if (state.isAddingAI) {
            val processingText = if (state.inChatSlots) "取消中..." else "添加中..."
            binding.tvAddAI.text = processingText
            binding.tvAddAI.isEnabled = false
            binding.tvAddAI.setBackgroundResource(R.drawable.bg_6b6b6b_r50)
        } else {
            if (state.inChatSlots) {
                // 已入梦状态
                binding.tvAddAI.text = "已入梦"
                binding.tvAddAI.isEnabled = true
                binding.tvAddAI.setBackgroundResource(R.drawable.bg_6b6b6b_r50)
            } else {
                // 未入梦状态
                binding.tvAddAI.text = "入梦"
                binding.tvAddAI.isEnabled = true
                binding.tvAddAI.setBackgroundResource(R.drawable.bg_primary_r50)
            }
        }
        binding.tvAddAI.setTextColor(requireContext().getColor(R.color.white))

        // 更新数据索引信息（可选，用于调试）
        Log.d("FeaturedFragment", "当前数据索引: ${state.currentIndex + 1}/${state.totalCount}")

    }

    /**
     * 加载背景图片
     */
    private fun loadBackgroundImage(imageUrl: String) {
        ImageUtils.loadImageToBG(requireContext(),imageUrl,binding.ivBackground)
    }
    
    /**
     * 执行滑动动画
     * @param isRightSwipe 是否是向右滑动
     * @param onAnimationComplete 动画完成后的回调
     */
    private fun performSlideAnimation(isRightSwipe: Boolean, onAnimationComplete: () -> Unit) {
        if (isAnimating) return
        
        isAnimating = true
        
        // 获取需要动画的视图
        val textContainer = binding.clyText
        val userNameContainer = binding.llyName
        val addAIContainer = binding.tvAddAI
        
        // 计算动画距离
        val screenWidth = resources.displayMetrics.widthPixels
        val slideDistance = screenWidth * 0.3f // 滑动30%的屏幕宽度
        
        // 设置初始位置
        val startTranslationX = if (isRightSwipe) -slideDistance else slideDistance
        val endTranslationX = 0f
        
        // 创建文本容器动画
        val slideOutAnimator = android.animation.ObjectAnimator.ofFloat(
            textContainer, "translationX", 0f, startTranslationX
        ).apply {
            duration = 200
            interpolator = android.view.animation.AccelerateInterpolator()
        }
        
        val slideInAnimator = android.animation.ObjectAnimator.ofFloat(
            textContainer, "translationX", startTranslationX, endTranslationX
        ).apply {
            duration = 300
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        // 创建用户名容器动画
        val userNameSlideOutAnimator = android.animation.ObjectAnimator.ofFloat(
            userNameContainer, "translationX", 0f, startTranslationX * 0.5f
        ).apply {
            duration = 200
            interpolator = android.view.animation.AccelerateInterpolator()
        }
        
        val userNameSlideInAnimator = android.animation.ObjectAnimator.ofFloat(
            userNameContainer, "translationX", startTranslationX * 0.5f, endTranslationX
        ).apply {
            duration = 300
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        // 创建入梦按钮动画
        val addAISlideOutAnimator = android.animation.ObjectAnimator.ofFloat(
            addAIContainer, "translationX", 0f, startTranslationX * 0.7f
        ).apply {
            duration = 200
            interpolator = android.view.animation.AccelerateInterpolator()
        }
        
        val addAISlideInAnimator = android.animation.ObjectAnimator.ofFloat(
            addAIContainer, "translationX", startTranslationX * 0.7f, endTranslationX
        ).apply {
            duration = 300
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        // 创建滑出动画集合
        val slideOutAnimatorSet = android.animation.AnimatorSet().apply {
            playTogether(slideOutAnimator, userNameSlideOutAnimator, addAISlideOutAnimator)
        }
        
        // 创建滑入动画集合
        val slideInAnimatorSet = android.animation.AnimatorSet().apply {
            playTogether(slideInAnimator, userNameSlideInAnimator, addAISlideInAnimator)
        }
        
        // 设置动画监听器
        slideOutAnimatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // 滑动出动画结束后，执行数据加载回调
                onAnimationComplete()
                
                // 延迟一点时间后开始滑入动画，让数据有时间更新
                textContainer.postDelayed({
                    slideInAnimatorSet.start()
                }, 50)
            }
        })
        
        slideInAnimatorSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                // 动画完成，重置状态
                isAnimating = false
                slideDirection = 0
            }
        })
        
        // 开始动画
        slideOutAnimatorSet.start()
    }
    
    /**
     * 添加淡入效果
     */
    private fun addFadeInEffect() {
        val textContainer = binding.clyText
        val userNameContainer = binding.llyName
        val addAIContainer = binding.tvAddAI
        
        // 设置初始透明度
        textContainer.alpha = 0f
        userNameContainer.alpha = 0f
        addAIContainer.alpha = 0f
        
        // 创建淡入动画
        val textFadeInAnimator = android.animation.ObjectAnimator.ofFloat(textContainer, "alpha", 0f, 1f).apply {
            duration = 200
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        val userNameFadeInAnimator = android.animation.ObjectAnimator.ofFloat(userNameContainer, "alpha", 0f, 1f).apply {
            duration = 200
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        val addAIFadeInAnimator = android.animation.ObjectAnimator.ofFloat(addAIContainer, "alpha", 0f, 1f).apply {
            duration = 200
            interpolator = android.view.animation.DecelerateInterpolator()
        }
        
        // 同时执行淡入动画
        android.animation.AnimatorSet().apply {
            playTogether(textFadeInAnimator, userNameFadeInAnimator, addAIFadeInAnimator)
            start()
        }
    }


    /**
     * 跳转到 ChatActivity
     */
    private fun navigateToChatActivity(characterId: String?,conversationId:String?) {
        Log.d("FeaturedFragment", "Navigated to ChatActivity with characterId: ${characterId}    conversationId :${conversationId}  ")

        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra(Constants.CHARACTER_ID_KEY,  characterId)
            putExtra(Constants.CONVERSATION_ID_KEY, conversationId)
        }
        startActivity(intent)
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
                sendIntent(FeaturedFragmentIntent.HideReplaceDialog)
            }

            override fun onNegativeClick() {
                // 取消按钮点击，隐藏对话框
                sendIntent(FeaturedFragmentIntent.HideReplaceDialog)
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
                sendIntent(FeaturedFragmentIntent.HideReplaceDialog)
                // 确认替换按钮点击，执行真正的替换逻辑
                if (conversation != null) {
                    sendIntent(FeaturedFragmentIntent.ReplaceAI(conversation))
                } else {
                    // 没有选中任何AI，显示提示
                    ToastUtils.showShort(requireContext(), "请先选择一个AI进行替换")
                }
            }
        })
        
        dialog.show(childFragmentManager, "AIReplaceDialog")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // 注销EventBus
        try {
            org.greenrobot.eventbus.EventBus.getDefault().unregister(this)
            Log.d("FeaturedFragment", "EventBus注销成功")
        } catch (e: Exception) {
            Log.w("FeaturedFragment", "EventBus注销失败: ${e.message}")
        }
    }
    
    /**
     * 设置tvBriefIntro的展开/收起功能
     */
    private fun setupBriefIntroExpandCollapse() {
        // 设置最大行数为3行
        binding.tvBriefIntro.maxLines = 3
        binding.tvBriefIntro.ellipsize = android.text.TextUtils.TruncateAt.END
        
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
        val textView = binding.tvBriefIntro
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
            binding.tvBriefIntro.maxLines = Int.MAX_VALUE
            binding.tvBriefIntro.ellipsize = null
        } else {
            // 收起：只显示3行
            binding.tvBriefIntro.maxLines = 3
            binding.tvBriefIntro.ellipsize = android.text.TextUtils.TruncateAt.END
        }
    }

    /**
     * 处理AI创建成功事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onAICreated(event: AICreatedEvent) {
        var isContains : Boolean = featuredCharacters.any { character ->
            character.id == event.createSuccessAIID
        }
        if (event.success && isContains ) {
            sendIntent(FeaturedFragmentIntent.Initialize)
        }
    }


    /**
     * 处理聊天槽位变化事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onChatSlotChanged(event: ChatSlotChangedEvent) {
        Log.d("FeaturedFragmentIntent", "收到聊天槽位变化事件: action=${event.action}")

        // 当聊天槽位发生变化时，重新初始化数据
        sendIntent(FeaturedFragmentIntent.Initialize)
    }
}
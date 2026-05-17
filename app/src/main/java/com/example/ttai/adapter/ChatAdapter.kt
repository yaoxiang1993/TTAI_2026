package com.example.ttai.adapter
import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ttai.R
import com.example.ttai.databinding.ItemMessageReceivedBinding
import com.example.ttai.databinding.ItemMessageSentBinding
import com.example.ttai.bean.Message
import com.example.ttai.databinding.ItemMessageLuckyRewardBinding
import com.example.ttai.databinding.ItemMessageReloadBinding
import com.example.ttai.utils.ItemMapper
import com.example.ttai.utils.ToastUtils

/**
 * 聊天消息的 RecyclerView 适配器，用于在 ChatActivity 中显示发送和接收的消息
 */
class ChatAdapter(private val onRollbackClick: (Message?) -> Unit,
                  private val onReloadMessageClick: (Message?) -> Unit,
                  private val onEditeClick: (Message?) -> Unit,
                  private val onDeteleClick: (Message?) -> Unit) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {
    companion object {
        private const val VIEW_TYPE_SENT = 1 // 发送消息视图类型
        private const val VIEW_TYPE_RECEIVED = 2 // 接收消息视图类型
        private const val VIEW_TYPE_RELOAD_MESSAGE = 3 // 重试消息视图类型
        private const val VIEW_TYPE_LUCKY_REWARD = 4 // 红包消息视图类型
    }

    var name:String ?=""
        get() = field
        set(value) {
            field = value
        }

    // 记录每条消息当前已经打字（动画显示）到哪个长度，防止复用时状态错乱
    private val typewriterStates = mutableMapOf<String, Int>()

    override fun getItemViewType(position: Int): Int {
        val sender = getItem(position).sender
        return when(sender){
            "user" -> VIEW_TYPE_SENT
            "character" -> VIEW_TYPE_RECEIVED
            "reload_message" -> VIEW_TYPE_RELOAD_MESSAGE
            "lucky_reward" -> VIEW_TYPE_LUCKY_REWARD
            else -> VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else if (viewType == VIEW_TYPE_RELOAD_MESSAGE){
            val binding = ItemMessageReloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReloadMessageViewHolder(binding)
        } else if (viewType == VIEW_TYPE_LUCKY_REWARD){
            val binding = ItemMessageLuckyRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LuckRewardMessageViewHolder(binding, name)
        } else{
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    /**
     * 处理带有 Payload 的局部刷新（避免打字时画面闪烁）
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains("TYPING_UPDATE") && holder is ReceivedMessageViewHolder) {
            // 局部刷新，不重置 ViewHolder 的状态
            holder.bind(getItem(position), isPayloadUpdate = true)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        var isUser = false
        if (holder is SentMessageViewHolder) {
            isUser = true
            holder.bind(message)
            holder.itemView.setOnLongClickListener {
                showMessageMenu(holder.itemView, message, position,isUser)
                true // 消费事件
            }
        } else if (holder is ReceivedMessageViewHolder) {
            isUser = false
            // 全量刷新
            holder.bind(message, isPayloadUpdate = false)
            // 长按监听
            holder.itemView.setOnLongClickListener {
                showMessageMenu(holder.itemView, message, position,isUser)
                true // 消费事件
            }
        } else if (holder is ReloadMessageViewHolder) {
            holder.bind(message,onReloadMessageClick)
        } else if (holder is LuckRewardMessageViewHolder) {
            holder.bind(message)
        }
    }

    private fun showMessageMenu(view: View, message: Message, position: Int, isUser: Boolean = false) {
        val popupView = LayoutInflater.from(view.context).inflate(R.layout.popup_message_menu, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        var xoff :Int = if (isUser){ view.width}else{0}
        var yoff :Int = -view.height
        popupWindow.showAsDropDown(view, xoff, yoff)
        popupView.findViewById<TextView>(R.id.tvRollbackToMessage).setOnClickListener {
            onRollbackClick(message)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.tvRollbackToMessage).isVisible = !isUser
        popupView.findViewById<TextView>(R.id.tvCopy).setOnClickListener {
            val clipboard = view.context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("content", message.content)
            clipboard.setPrimaryClip(clip)
            ToastUtils.showShort(view.context, "消息已复制到剪贴板")
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.tvEdite).isVisible = !isUser
        popupView.findViewById<TextView>(R.id.tvEdite).setOnClickListener {
            onEditeClick(message)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.tvDelete).setOnClickListener {
            onDeteleClick(message)
            popupWindow.dismiss()
        }
    }

    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
        }
    }

    class ReloadMessageViewHolder(private val binding: ItemMessageReloadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, onReloadMessageClick: (Message?) -> Unit) {
            binding.root.setOnClickListener {
                onReloadMessageClick(message)
            }
        }
    }

    class LuckRewardMessageViewHolder(private val binding: ItemMessageLuckyRewardBinding, private val name: String?) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(message: Message ) {
            val itemName = ItemMapper.getItemName(message.reward_type)
            val fullText = "天降福利,${name} 赠送你${message.reward_amount} $itemName~"
            val targetText = "赠送"
            val spannableString = SpannableString(fullText)
            val startIndex = fullText.indexOf(targetText)
            val endIndex = startIndex + targetText.length
            if (startIndex != -1) {
                spannableString.setSpan(
                    ForegroundColorSpan(R.color.color_fbd149),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            binding.tvText.text = spannableString
        }
    }

    /**
     * 改为 inner class 以便访问外部 Adapter 的 typewriterStates
     */
    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        private var isExpanded = false
        private var currentMessageId: String? = null

        // 打字机状态控制
        private var targetText = ""
        private var typewriterRunnable: Runnable? = null
        private var currentMessage: Message? = null

        // 预编译正则，提高流式刷新时的性能
        private val bracketRegex = "[（(].*?[）)]".toRegex()

        fun bind(message: Message, isPayloadUpdate: Boolean = false) {
            currentMessage = message

            // 核心修复：如果发生了局部刷新，且 ID 变了（说明是从本地 UUID 变成了真实的 Server ID）
            // 我们必须把打字机的进度继承过来，防止打字动画断层并瞬间显示剩余内容！
            if (isPayloadUpdate && currentMessageId != null && currentMessageId != message.id) {
                val oldLen = typewriterStates.remove(currentMessageId)
                if (oldLen != null) {
                    typewriterStates[message.id] = oldLen
                }
            }

            // 如果不是局部刷新，且换了新的 messageID，则重置状态
            if (!isPayloadUpdate && currentMessageId != message.id) {
                resetViewHolderState()
            }
            currentMessageId = message.id

            val content = message.content?.toString() ?: ""
            targetText = content // 更新当前需要到达的目标完整文本

            val currentLen = typewriterStates[message.id]
            // 判断是否需要继续打字：1. 流还在传输; 2. 流传完了，但UI上还没打完字
            val isCatchingUp = currentLen != null && currentLen < targetText.length
            val needTypewriter = message.isTyping || isCatchingUp

            if (needTypewriter) {
                binding.ivMeme.isVisible = false
                binding.ivExpand.isVisible = false
                binding.tvMessage.isVisible = true

                // 如果是新来的打字流，初始化它的记录防止空指针中断
                if (currentLen == null) {
                    typewriterStates[message.id] = 0
                }

                if (content.isBlank() && message.isTyping) {
                    // 没有文本时，显示传统的 "正在输入..."
                    startTypingAnimation()
                } else {
                    // 收到文本，停止传统的动画，启动打字机效果
                    stopTypingAnimation()
                    // 🟢 修复：调用时不传死 ID，让它动态读取
                    startTypewriter()
                }
            } else {
                // 彻底回复完成，且UI已经展示完全部文字，或者历史消息
                stopTypingAnimation()
                stopTypewriter()
                // 直接标记此消息显示长度为全长
                typewriterStates[message.id] = content.length
                handleMessageContent(message)
            }
        }

        private fun resetViewHolderState() {
            isExpanded = false
            stopTypingAnimation()
            stopTypewriter()
            binding.ivExpand.visibility = View.GONE
            binding.ivMeme.visibility = View.GONE
            binding.tvMessage.visibility = View.VISIBLE
            binding.tvMessage.maxLines = Int.MAX_VALUE
        }

        /**
         * 启动打字机效果 (包含网络突发时的动态加速处理)
         */
        // 🟢 修复：移除 messageId 参数，解决 ID 切换时打字机意外自杀的问题
        private fun startTypewriter() {
            if (typewriterRunnable != null) return

            typewriterRunnable = object : Runnable {
                override fun run() {
                    // 🟢 核心修复：动态获取最新替换后的 ID，无缝衔接
                    val msgId = currentMessageId ?: return
                    val currentLen = typewriterStates[msgId] ?: 0

                    if (currentLen < targetText.length) {
                        // 防止把 Emoji 拆开
                        var step = 1
                        if (currentLen < targetText.length - 1) {
                            val c = targetText[currentLen]
                            if (Character.isHighSurrogate(c)) {
                                step = 2
                            }
                        }

                        val nextLen = currentLen + step
                        typewriterStates[msgId] = nextLen

                        val textToShow = targetText.substring(0, nextLen)
                        binding.tvMessage.text = formatMessageWithBracketColor(textToShow)

                        // 严格遵守用户要求：无论积压多少字，恒定以 50ms 逐字平滑显示到结尾
                        binding.tvMessage.postDelayed(this, 50L)
                    } else {
                        // 已经追赶上了当前的文本内容，暂停等待下一批数据
                        typewriterRunnable = null
                        // 触发最终排版(显示展开按钮等)
                        currentMessage?.let {
                            if (!it.isTyping) {
                                handleMessageContent(it)
                            }
                        }
                    }
                }
            }
            binding.tvMessage.post(typewriterRunnable)
        }

        private fun stopTypewriter() {
            typewriterRunnable?.let { binding.tvMessage.removeCallbacks(it) }
            typewriterRunnable = null
        }

        private fun handleMessageContent(message: Message) {
            val content = message.content?.toString() ?: ""

            if (message.message_type == "meme") {
                binding.ivMeme.isVisible = true
                binding.tvMessage.isVisible = false
                binding.ivExpand.isVisible = false
                Glide.with(binding.ivMeme.context)
                    .load(content)
                    .into(binding.ivMeme)
            } else {
                binding.ivMeme.isVisible = false
                binding.tvMessage.isVisible = true
                binding.tvMessage.text = formatMessageWithBracketColor(content)

                if (content.startsWith("简介:")) {
                    setupExpandableLogic()
                } else {
                    binding.ivExpand.isVisible = false
                    binding.tvMessage.maxLines = Int.MAX_VALUE
                }
            }
        }

        private fun setupExpandableLogic() {
            binding.tvMessage.post {
                val lineCount = binding.tvMessage.lineCount
                if (lineCount > 3) {
                    binding.ivExpand.visibility = View.VISIBLE
                    binding.tvMessage.maxLines = if (isExpanded) Int.MAX_VALUE else 3
                    binding.ivExpand.setOnClickListener {
                        isExpanded = !isExpanded
                        binding.tvMessage.maxLines = if (isExpanded) Int.MAX_VALUE else 3
                    }
                } else {
                    binding.ivExpand.visibility = View.GONE
                    binding.tvMessage.maxLines = Int.MAX_VALUE
                }
            }
        }

        private fun startTypingAnimation() {
            stopTypingAnimation()
            val animator = android.animation.ValueAnimator.ofInt(0, 3).apply {
                duration = 1000
                repeatCount = android.animation.ValueAnimator.INFINITE
                addUpdateListener { animation ->
                    if (!binding.tvMessage.isVisible) {
                        animation.cancel()
                        return@addUpdateListener
                    }
                    val count = animation.animatedValue as Int
                    val dots = ".".repeat((count % 3) + 1)
                    binding.tvMessage.text = "正在输入$dots"
                }
            }
            animator.start()
            itemView.tag = animator
        }

        private fun stopTypingAnimation() {
            (itemView.tag as? android.animation.ValueAnimator)?.let {
                it.removeAllUpdateListeners()
                it.cancel()
            }
            itemView.tag = null
        }

        private fun formatMessageWithBracketColor(text: String): SpannableStringBuilder {
            val ssb = SpannableStringBuilder(text)
            val bracketColor = ContextCompat.getColor(itemView.context, R.color.color_b6b6b6)

            bracketRegex.findAll(text).forEach { match ->
                ssb.setSpan(
                    ForegroundColorSpan(bracketColor),
                    match.range.first,
                    match.range.last + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return ssb
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            if (oldItem.id == newItem.id) return true

            // 核心修复：SSE 结束时，本地的 UUID 会被替换为服务器的真实 ID。
            if (oldItem.sender == "character" && newItem.sender == "character") {
                // 🟢 修复：只要旧消息是在打字状态，我们就放宽限制，认定它们是同一条。
                // 这能够保证100%触发 Payload 局部更新，完美继承打字进度！
                if (oldItem.isTyping) {
                    return true
                }
            }
            return false
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Message, newItem: Message): Any? {
            // 只要旧消息或新消息处于 typing 状态，或者遇到了上面处理的 ID 切换情况，就返回 PAYLOAD 避免闪烁
            if (oldItem.isTyping || newItem.isTyping || oldItem.id != newItem.id) {
                return "TYPING_UPDATE"
            }
            return super.getChangePayload(oldItem, newItem)
        }
    }
}
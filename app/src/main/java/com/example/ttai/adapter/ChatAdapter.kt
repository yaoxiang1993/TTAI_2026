package com.example.ttai.adapter
import android.annotation.SuppressLint
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
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
        private const val VIEW_TYPE_RELOAD_MESSAGE = 3 // 接收消息视图类型
        private const val VIEW_TYPE_LUCKY_REWARD = 4 // 红包消息视图类型
    }
      var name:String ?=""
        get() = field
        set(value) {
            field = value
        }

    /**
     * 根据消息的发送者决定视图类型（发送或接收）
     */
    override fun getItemViewType(position: Int): Int {
        val sender = getItem(position).sender
       val messageType = when(sender){
            "user" ->VIEW_TYPE_SENT
            "character" ->VIEW_TYPE_RECEIVED
            "reload_message" ->VIEW_TYPE_RELOAD_MESSAGE
            "lucky_reward" ->VIEW_TYPE_LUCKY_REWARD
           else -> {VIEW_TYPE_RECEIVED}
       }
        return messageType
    }


    /**
     * 创建 ViewHolder，分别为发送和接收消息加载不同布局
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val binding = ItemMessageSentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SentMessageViewHolder(binding)
        } else if (viewType == VIEW_TYPE_RELOAD_MESSAGE){
            val binding = ItemMessageReloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReloadMessageViewHolder(binding)
        } else if (viewType == VIEW_TYPE_LUCKY_REWARD){
            val binding = ItemMessageLuckyRewardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LuckRewardMessageViewHolder(binding,name)
        } else{
            val binding = ItemMessageReceivedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ReceivedMessageViewHolder(binding)
        }
    }

    /**
     * 绑定消息数据到 ViewHolder
     */
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
            holder.bind(message)
            // 长按监听
            holder.itemView.setOnLongClickListener {
                showMessageMenu(holder.itemView, message, position,isUser)
                true // 消费事件
            }
        }else if (holder is ReloadMessageViewHolder) {
            holder.bind(message,onReloadMessageClick)
            // 长按监听
        }else if (holder is LuckRewardMessageViewHolder) {
            holder.bind(message )
        }

    }

    private fun showMessageMenu(view: View, message: Message, position: Int,isUser: Boolean = false) {
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
        // 回溯消息
            onRollbackClick(message)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.tvRollbackToMessage).isVisible = !isUser
        popupView.findViewById<TextView>(R.id.tvCopy).setOnClickListener {
            // 复制到剪贴板
            val clipboard = view.context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("content", message.content)
            clipboard.setPrimaryClip(clip)
            // 显示Toast提示
            ToastUtils.showShort(view.context, "消息已复制到剪贴板")
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.tvEdite).isVisible = !isUser
        popupView.findViewById<TextView>(R.id.tvEdite).setOnClickListener {
            // 编辑
            onEditeClick(message)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.tvDelete).setOnClickListener {
            // 删除
            onDeteleClick(message)
            popupWindow.dismiss()
        }
    }

    /**
     * 发送消息的 ViewHolder，显示右侧对齐的消息和头像
     */
    class SentMessageViewHolder(private val binding: ItemMessageSentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            binding.tvMessage.text = message.content
        }
    }

    /**
     * 重新生成消息图标
     */
    class ReloadMessageViewHolder(private val binding: ItemMessageReloadBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message, onReloadMessageClick: (Message?) -> Unit) {
            binding.root.setOnClickListener {
                onReloadMessageClick(message)
            }
        }
    }

    /**
     * 红包信息
     */
    class LuckRewardMessageViewHolder(private val binding: ItemMessageLuckyRewardBinding,private val name: String?) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor")
        fun bind(message: Message ) {
            // 1. 组合完整的未着色字符串
            val itemName = ItemMapper.getItemName(message.reward_type)
            val fullText = "天降福利,${name} 赠送你${message.reward_amount} $itemName~"

            // 要着色的文本是 "赠送"
            val targetText = "赠送"

            // 2. 创建 SpannableString 对象
            val spannableString = SpannableString(fullText)

            // 3. 找到 "赠送" 的起始和结束位置
            val startIndex = fullText.indexOf(targetText)
            val endIndex = startIndex + targetText.length
            // 4. 应用颜色 Span，并设置 Span 的标志
            if (startIndex != -1) {
                spannableString.setSpan(
                    ForegroundColorSpan(R.color.color_fbd149), // 设置前景色
                    startIndex, // 开始位置
                    endIndex,   // 结束位置
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE // 标志：不包括起始和结束字符
                )
            }
            binding.tvText.text = spannableString
        }
    }

    /**
     * 接收消息的 ViewHolder，显示左侧对齐的消息、发送者和头像
     */
    class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) : RecyclerView.ViewHolder(binding.root) {
        private var isExpanded = false
        private var originalContent = ""
        private var currentMessageId: String? = null // 添加当前消息ID跟踪
        
        fun bind(message: Message) {
            // 如果消息ID发生变化，重置所有状态
            if (currentMessageId != message.id) {
                resetViewHolderState()
                currentMessageId = message.id
            }

            if (message.isTyping) {
                // 正在输入状态，显示动画省略号
                binding.tvMessage.text = "正在输入..."
                binding.ivExpand.visibility = android.view.View.GONE
                binding.tvMessage.maxLines = Int.MAX_VALUE
                startTypingAnimation()
            } else {
                // 正常消息
                originalContent = message.content.toString()
                handleMessageContent(message)
                stopTypingAnimation()
            }
        }
        
        /**
         * 重置ViewHolder状态，避免缓存问题
         */
        private fun resetViewHolderState() {
            isExpanded = false
            originalContent = ""
            stopTypingAnimation()
            binding.ivExpand.visibility =  View.GONE
            binding.ivMeme.visibility = View.GONE
            binding.tvMessage.visibility = View.VISIBLE
            binding.tvMessage.maxLines = Int.MAX_VALUE // 重置行数限制
        }
        
        private fun handleMessageContent(message: Message) {
            // 确保停止任何正在运行的动画
            stopTypingAnimation()
            
            if (message.content.toString().startsWith("简介:")) {
                // 处理以"简介:"开头的消息
                setupExpandableMessage(message.content.toString())
            } else {
                if (message.message_type == "meme"){
                    binding.ivMeme.isVisible  = true
                    binding.tvMessage.isVisible  = false
                    binding.ivExpand.isVisible = false
                    Glide.with(binding.ivMeme.context)
                        .load(message.content.toString())
                        .into(binding.ivMeme)
                }else{
                    // 普通消息，正常显示
                    binding.tvMessage.text = formatMessageWithBracketColor(message.content.toString())
                    binding.ivExpand.isVisible = false
                    binding.ivMeme.isVisible  = false
                    binding.tvMessage.maxLines = Int.MAX_VALUE
                }
            }
        }

        private fun setupExpandableMessage(content: String) {
            // 确保停止任何正在运行的动画
            stopTypingAnimation()
            
            val formattedContent = formatMessageWithBracketColor(content)
            binding.tvMessage.text = formattedContent

            // 使用post延迟检查，确保文本渲染完成后再检查行数
            binding.tvMessage.post {
                val lineCount = binding.tvMessage.lineCount
                
                // 临时修改：如果行数大于2就显示展开按钮（用于测试）
                if (lineCount > 3) {
                    // 内容超过2行，显示展开按钮
                    binding.ivExpand.visibility = android.view.View.VISIBLE
                    binding.tvMessage.maxLines = if (isExpanded) Int.MAX_VALUE else 3
                    
                    // 设置展开按钮点击事件
                    binding.ivExpand.setOnClickListener {
                        toggleExpand()
                    }
                } else {
                    // 内容不超过2行，隐藏展开按钮
                    binding.ivExpand.visibility = android.view.View.GONE
                    binding.tvMessage.maxLines = Int.MAX_VALUE
                }
            }
        }

        
        private fun toggleExpand() {
            isExpanded = !isExpanded
            if (isExpanded) {
                // 展开：显示全部内容
                binding.tvMessage.maxLines = Int.MAX_VALUE
                android.util.Log.d("ChatAdapter", "展开消息内容 - 显示全部")
            } else {
                // 收起：限制为2行
                binding.tvMessage.maxLines = 3
                android.util.Log.d("ChatAdapter", "收起消息内容到3行")
            }
        }
        
        private fun startTypingAnimation() {
            // 创建动画效果，让省略号闪烁
            val animator = android.animation.ValueAnimator.ofInt(0, 3)
            animator.duration = 1000
            animator.repeatCount = android.animation.ValueAnimator.INFINITE
            animator.addUpdateListener { animation ->
                val dots = ".".repeat(animation.animatedValue as Int + 1)
                binding.tvMessage.text = "正在输入$dots"
            }
            animator.start()
            
            // 保存动画引用以便停止
            itemView.tag = animator
        }
        
        private fun stopTypingAnimation() {
            // 停止动画
            (itemView.tag as? android.animation.ValueAnimator)?.cancel()
            itemView.tag = null
        }
        
        /**
         * 格式化消息文本，将括号内的内容设置为特定颜色
         * @param text 原始文本
         * @return 格式化后的文本
         */
        private fun formatMessageWithBracketColor(text: String): SpannableStringBuilder {
            val spannableStringBuilder = SpannableStringBuilder(text)
            val bracketColor = ContextCompat.getColor(itemView.context, R.color.color_b6b6b6)
            
            // 使用栈来处理嵌套括号，避免死循环
            val stack = mutableListOf<Int>()
            var i = 0
            
            while (i < text.length) {
                when (text[i]) {
                         '(' ,
                        '（' ->  {
                        stack.add(i) // 将左括号位置入栈
                       }
                    ')','）'-> {
                        if (stack.isNotEmpty()) {
                            val openIndex = stack.removeAt(stack.size - 1) // 弹出最近的左括号
                            // 设置括号内文本的颜色
                            spannableStringBuilder.setSpan(
                                ForegroundColorSpan(bracketColor),
                                openIndex,
                                i + 1, // 包含右括号
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        // 如果没有匹配的左括号，忽略这个右括号
                    }
                }
                i++
            }
            
            return spannableStringBuilder
        }
    }

    /**
     * DiffUtil 回调，用于高效比较和更新消息列表
     */
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            // 使用唯一ID来标识消息，确保准确性和避免冲突
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            // 检查消息内容、发送状态等是否相同
            // 当正在输入状态变为正常消息时，内容会不同，需要更新
            return oldItem == newItem
        }
    }
}
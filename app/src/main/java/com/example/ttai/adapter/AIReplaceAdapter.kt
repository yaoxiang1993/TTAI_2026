package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ttai.R
import com.example.ttai.databinding.ItemAiReplaceBinding
import com.example.ttai.bean.Conversation

class AIReplaceAdapter(
    private val onReplaceClick: (Conversation) -> Unit
) : ListAdapter<Conversation, AIReplaceAdapter.ViewHolder>(ConversationDiffCallback()) {
    
    private var selectedConversation: Conversation? = null
    
    fun getSelectedConversation(): Conversation? = selectedConversation

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAiReplaceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAiReplaceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(conversation: Conversation) {
            // 设置AI名称
            binding.tvName.text = conversation.character.name

            if (!conversation.character.avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(conversation.character.avatarUrl)
                    .placeholder(R.mipmap.icon_heard_cricle)
                    .error(R.mipmap.icon_heard_cricle)
                    .circleCrop()
                    .into(binding.ivIcon)
            } else {
                binding.ivIcon.setImageResource(R.mipmap.icon_heard_cricle)
            }

            // 检查是否为选中状态
            val isSelected = selectedConversation?.id == conversation.id
            
            // 设置替换按钮文案和样式
            if (isSelected) {
                binding.tvReplace.text = "已替换"
                binding.tvReplace.setBackgroundResource(com.example.ttai.R.drawable.bg_stroke_6b6b6b_r50)
                binding.tvReplace.setTextColor(binding.root.context.getColor(com.example.ttai.R.color.color_6b6b6b))
            } else {
                binding.tvReplace.text = "替换"
                binding.tvReplace.setBackgroundResource(com.example.ttai.R.drawable.bg_stroke_ac00ff_r50)
                binding.tvReplace.setTextColor(binding.root.context.getColor(com.example.ttai.R.color.color_ac00ff))
            }
            
            // 设置替换按钮点击事件
            binding.tvReplace.setOnClickListener {
                // 更新选中状态
                selectedConversation = if (isSelected) null else conversation
                // 通知适配器数据变化，重新绑定视图
                notifyDataSetChanged()
                // 回调选中状态变化
                onReplaceClick(conversation)
            }
        }
    }

    private class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
} 
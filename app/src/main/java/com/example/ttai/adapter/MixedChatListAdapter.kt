package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.databinding.ItemChatListBinding
import com.example.ttai.databinding.ItemAddAiChatListBinding
import com.example.ttai.bean.Conversation
import com.example.ttai.utils.CommontUtils

class MixedChatListAdapter(
    private val onConversationClick: (Conversation) -> Unit,
    private val onAddAIClick: () -> Unit
) : ListAdapter<ChatListItem, RecyclerView.ViewHolder>(ItemDiffCallback()) {

    companion object {
        private const val TYPE_CONVERSATION = 0
        private const val TYPE_ADD_AI = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatListItem.ConversationItem -> TYPE_CONVERSATION
            is ChatListItem.AddAIItem -> TYPE_ADD_AI
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CONVERSATION -> {
                val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ConversationViewHolder(binding)
            }
            TYPE_ADD_AI -> {
                val binding = ItemAddAiChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AddAIViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ConversationViewHolder -> {
                val item = getItem(position) as ChatListItem.ConversationItem
                holder.bind(item.conversation)
                holder.itemView.setOnClickListener { onConversationClick(item.conversation) }
            }
            is AddAIViewHolder -> {
                holder.bind()
                holder.itemView.setOnClickListener { onAddAIClick() }
            }
        }
    }

    class ConversationViewHolder(private val binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Conversation) {
            binding.apply {
                tvName.text = item.character.name
                tvChatMessage.text = item.lastMessage
                tvTime.text = CommontUtils.formatTime(item.lastMessageTime)
                ivPermanentMemory.isVisible = item.character.permanentMemoryEnabled == true
                // 安全加载头像，避免MediaDocumentsProvider权限问题
                val avatarUrl = item.character.avatarUrl
                avatarUrl?.let {
                    if (!avatarUrl.contains("com.android.providers.media.documents")) {
                        try {
                            Glide.with(ivIcon.context)
                                .load(avatarUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .error(android.R.drawable.ic_menu_report_image)
                                .transform(CenterCrop(), CircleCrop())
                                .into(ivIcon)
                        } catch (e: Exception) {
                            android.util.Log.e("MixedChatListAdapter", "头像加载失败", e)
                        }
                    }
                }
            }
        }
    }

    class AddAIViewHolder(private val binding: ItemAddAiChatListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.apply {
                tvName.text = "虚位待定"
                tvChatMessage.text = "可在梦境广场任意选择一个角色添加"
                tvTime.text = "去添加"
            }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<ChatListItem>() {
        override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
            return when {
                oldItem is ChatListItem.ConversationItem && newItem is ChatListItem.ConversationItem ->
                    oldItem.conversation.characterId == newItem.conversation.characterId
                oldItem is ChatListItem.AddAIItem && newItem is ChatListItem.AddAIItem -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem): Boolean {
            return oldItem == newItem
        }
    }
}

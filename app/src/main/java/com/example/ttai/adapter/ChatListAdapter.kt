package com.example.ttai.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.databinding.ItemChatListBinding
import com.example.ttai.bean.Conversation

class ChatListAdapter(private val onItemClick: (Conversation) -> Unit) : ListAdapter<Conversation, ChatListAdapter.ItemViewHolder>(
    ItemDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemChatListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val chatItem = getItem(position)
        holder.bind(chatItem)
        holder.itemView.setOnClickListener { onItemClick(chatItem) }
    }

    class ItemViewHolder(private val binding: ItemChatListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Conversation) {
            binding.apply {
                tvName.text = item.character.name
                tvChatMessage.text = item.lastMessage
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
                            android.util.Log.e("ChatListAdapter", "头像加载失败", e)
                        }
                    }
                }
            }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean =
            oldItem.characterId == newItem.characterId
        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean =
            oldItem == newItem
    }
}
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
import com.example.ttai.databinding.ItemSearchListBinding
import com.example.ttai.bean.Character
import com.example.ttai.bean.ChatItem

class SearchResultAdapter(private val onItemClick: (Character) -> Unit) : ListAdapter<Character, SearchResultAdapter.ItemViewHolder>(
    ItemDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemSearchListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val character = getItem(position)
        holder.bind(character)
        holder.itemView.setOnClickListener { onItemClick(character) }
    }

    class ItemViewHolder(private val binding: ItemSearchListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Character) {
            binding.apply {
                tvName.text = item.name
                tvChatMessage.text = item.openingLine
                // 加载头像
                try {
                    item.avatarUrl?.let {
                        Glide.with(ivIcon.context)
                            .load(item.avatarUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_menu_report_image)
                            .transform(CenterCrop(), CircleCrop())
                            .into(ivIcon)
                    }

                } catch (e: Exception) {
                    android.util.Log.e("SearchResultAdapter", "头像加载失败", e)
                    ivIcon.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }
    }

    class ItemDiffCallback : DiffUtil.ItemCallback<Character>() {
        override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean =
            oldItem == newItem
    }
}

package com.example.ttai.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.R
import com.example.ttai.bean.FollowsItem
import com.example.ttai.bean.NotificationsEntity
import com.example.ttai.databinding.ItemFancesBinding
import com.example.ttai.databinding.ItemNotifyBinding
import com.example.ttai.utils.CommontUtils

class NotifyListAdapter(
    private val onItemClick: ((NotificationsEntity) -> Unit)? = null,
    private val onMutualClick: ((NotificationsEntity) -> Unit)? = null
) : ListAdapter<NotificationsEntity, RecyclerView.ViewHolder>(DiffCallback()) {
    
    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }
    
    private var isLoadingMore = false
    
    fun setLoadingMore(loading: Boolean) {
        isLoadingMore = loading
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < currentList.size) VIEW_TYPE_ITEM else VIEW_TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = ItemNotifyBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                FancesViewHolder(binding, onItemClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FancesViewHolder -> {
                val item = getItem(position) as NotificationsEntity
                holder.bind(item)
            }
        }
    }
    
    override fun getItemCount(): Int {
        return currentList.size + if (isLoadingMore) 1 else 0
    }

    class FancesViewHolder(
        private val binding: ItemNotifyBinding,
        private val onItemClick: ((NotificationsEntity) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationsEntity) {
            binding.apply {
                // 设置基本信息
                tvName.text = item.title
                tvId.text =  item.content
                tvTime.text = CommontUtils.formatTime(item.created_at)
                view.isVisible = item.is_read != true
            }
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }
        }

    }
    class DiffCallback : DiffUtil.ItemCallback<NotificationsEntity>() {
        override fun areItemsTheSame(oldItem: NotificationsEntity, newItem: NotificationsEntity): Boolean {
            return oldItem.id == newItem.id && oldItem.is_read == newItem.is_read

        }

        override fun areContentsTheSame(oldItem: NotificationsEntity, newItem: NotificationsEntity): Boolean {
                // 优化比较逻辑，只比较关键字段
            return oldItem.id == newItem.id && oldItem.is_read == newItem.is_read
        }
    }
} 
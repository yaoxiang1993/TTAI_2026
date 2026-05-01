package com.example.ttai.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.R
import com.example.ttai.bean.FollowsItem
import com.example.ttai.databinding.ItemFancesBinding

class FollowersAdapter(
    private val onItemClick: ((FollowsItem) -> Unit)? = null,
    private val onMutualClick: ((FollowsItem) -> Unit)? = null
) : ListAdapter<FollowsItem, RecyclerView.ViewHolder>(FancesDiffCallback()) {
    
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
                val binding = ItemFancesBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                FancesViewHolder(binding, onItemClick,onMutualClick)
            }
            VIEW_TYPE_LOADING -> {
                val loadingView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading_more, parent, false)
                LoadingViewHolder(loadingView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is FancesViewHolder -> {
                val item = getItem(position) as FollowsItem
               // Log.d("SynthesizeAdapter", "onBindViewHolder: position=$position, item=$item")
                holder.bind(item)
            }
            is LoadingViewHolder -> {
                // 加载更多的ViewHolder不需要绑定数据
            }
        }
    }
    
    override fun getItemCount(): Int {
        return currentList.size + if (isLoadingMore) 1 else 0
    }

    class FancesViewHolder(
        private val binding: ItemFancesBinding,
        private val onItemClick: ((FollowsItem) -> Unit)?,
        private val onMutualClick: ((FollowsItem) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentItem: FollowsItem? = null

        init {
            // 在初始化时设置点击事件，避免在bind中重复设置
            binding.root.setOnClickListener {
                currentItem?.let { item ->
                    onItemClick?.invoke(item)
                }
            }
            binding.tvFocus.setOnClickListener {
                currentItem?.let { item ->
                    onMutualClick?.invoke(item)
                }
            }
        }

        fun bind(item: FollowsItem) {
            //Log.d("SynthesizeAdapter", "bind: item=${item.username}, description=${item.user_id}")
            
            // 缓存当前item，用于点击事件
            currentItem = item
            
            binding.apply {
                // 设置基本信息
                tvName.text = item.username
                // 优化图片加载
                loadAvatar(item.avatar_url)
                tvId.text = "ID:${item.user_id}"
                if(item.is_mutual == true){
                    tvFocus.text = "已关注"
                    tvFocus.setBackgroundResource(R.drawable.bg_9a9b9f_r50)
                }else{
                    tvFocus.text = "关注"
                    tvFocus.setBackgroundResource(R.drawable.bg_gradient_b2a4f9_e5b1fb)
                }
            }
        }
        /**
         * 加载头像图片
         */
        private fun loadAvatar(avatarUrl: String?) {
            //Log.d("SynthesizeFragment", "loadAvatar ${avatarUrl}")
            if (!avatarUrl.isNullOrEmpty()) {
                 Glide.with(binding.ivHeard.context)
                    .load(avatarUrl)
                    .placeholder( R.mipmap.icon_heard)
                    .error(R.mipmap.icon_heard)
                    .transform(CenterCrop(), CircleCrop())
                    .into(binding.ivHeard)
            } else {
                // 设置默认图片
                binding.ivHeard.setImageResource(R.mipmap.icon_heard)
            }
        }
    }
    
    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 加载更多的ViewHolder，可以在这里添加加载动画
    }

    class FancesDiffCallback : DiffUtil.ItemCallback<FollowsItem>() {
        override fun areItemsTheSame(oldItem: FollowsItem, newItem: FollowsItem): Boolean {
            return oldItem.user_id == newItem.user_id

        }

        override fun areContentsTheSame(oldItem: FollowsItem, newItem: FollowsItem): Boolean {
                // 优化比较逻辑，只比较关键字段
            return oldItem.user_id == newItem.user_id &&
                        oldItem.is_mutual == newItem.is_mutual&&
                        oldItem.username == newItem.username
        }
    }
} 
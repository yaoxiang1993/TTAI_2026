package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.databinding.ItemSynthesizeBinding
import com.example.ttai.bean.Character

class ShopAdapter(
    private val onItemClick: ((Character) -> Unit)? = null
) : ListAdapter<Character, ShopAdapter.SynthesizeViewHolder>(SynthesizeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SynthesizeViewHolder {
        val binding = ItemSynthesizeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SynthesizeViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: SynthesizeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SynthesizeViewHolder(
        private val binding: ItemSynthesizeBinding,
        private val onItemClick: ((Character) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentItem: Character? = null

        init {
            // 在初始化时设置点击事件，避免在bind中重复设置
            binding.root.setOnClickListener {
                currentItem?.let { item ->
                    onItemClick?.invoke(item)
                }
            }
        }

        fun bind(item: Character) {
            // 缓存当前item，用于点击事件
            currentItem = item
            
            binding.apply {
                // 设置基本信息
                tvName.text = item.name
                tvContent.text = item.description
                
                // 优化标签显示逻辑
                item.personalityTags?.let { setupTags(it) }
                
                // 优化图片加载
                loadAvatar(item.avatarUrl)
            }
        }

        /**
         * 设置标签显示
         */
        private fun setupTags(tags: List<String>) {
            val tagViews = listOf(binding.tvTag1, binding.tvTag2, binding.tvTag3)
            
            // 先隐藏所有标签
            tagViews.forEach { it.isVisible = false }
            
            // 显示可用的标签
            tags.take(3).forEachIndexed { index, tag ->
                if (index < tagViews.size) {
                    tagViews[index].apply {
                        text = "# ${tag}"
                        isVisible = true
                    }
                }
            }
        }

        /**
         * 加载头像图片
         */
        private fun loadAvatar(avatarUrl: String?) {
        //    android.util.Log.d("SynthesizeFragment", "loadAvatar ${avatarUrl}")
//            if (!avatarUrl.isNullOrEmpty()) {
//                Glide.with(binding.ivBg.context)
//                    .load(avatarUrl)
//                    .placeholder(R.mipmap.icon_synthesize_placeholder)
//                    .error(R.mipmap.icon_synthesize_placeholder)
//                    .into(binding.ivBg)
//
//                Glide.with(binding.ivHeard.context)
//                    .load(avatarUrl)
//                    .placeholder(R.mipmap.icon_synthesize_placeholder)
//                    .error(R.mipmap.icon_synthesize_placeholder)
//                    .transform(CenterCrop(), CircleCrop())
//                    .into(binding.ivHeard)
//            }
        }
    }

    class SynthesizeDiffCallback : DiffUtil.ItemCallback<Character>() {
        override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean {
            // 优化比较逻辑，只比较关键字段
            return oldItem.id == newItem.id &&
                   oldItem.name == newItem.name &&
                   oldItem.description == newItem.description &&
                   oldItem.avatarUrl == newItem.avatarUrl
        }
    }
} 
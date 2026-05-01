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
import com.example.ttai.utils.KTXUtils
import com.example.ttai.R
import com.example.ttai.databinding.ItemSynthesizeBinding
import com.example.ttai.bean.Character
import com.example.ttai.utils.formatPopularity
import java.math.RoundingMode
import java.text.DecimalFormat

class SynthesizeAdapter(
    private val onItemClick: ((Character) -> Unit)? = null
) : ListAdapter<Any, RecyclerView.ViewHolder>(SynthesizeDiffCallback()) {
    
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
                val binding = ItemSynthesizeBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SynthesizeViewHolder(binding, onItemClick)
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
            is SynthesizeViewHolder -> {
                val item = getItem(position) as Character
               // android.util.Log.d("SynthesizeAdapter", "onBindViewHolder: position=$position, item=$item")
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
            //android.util.Log.d("SynthesizeAdapter", "bind: item=${item.name},  ")
            
            // 缓存当前item，用于点击事件
            currentItem = item
            
            binding.apply {
                // 设置基本信息
                tvName.text = item.name
                tvContent.text = item.briefIntro
                tvFocus.text = item.popularity?.formatPopularity()
                tvAuthorName.text = "@${item.author_name}"

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
            tags.take(2).forEachIndexed { index, tag ->
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
          //  android.util.Log.d("SynthesizeFragment", "loadAvatar ${avatarUrl}")
            if (!avatarUrl.isNullOrEmpty()) {
                // 使用Glide加载图片
                   Glide.with(binding.ivBg.context)
                    .load(avatarUrl)
                    .placeholder( R.mipmap.icon_synthesize_placeholder)
                    .error(R.mipmap.icon_synthesize_placeholder)
                 //   .apply(RequestOptions().transform(RoundedCorners(DensityUtils.dpToPx(5.0f))))
                    .into(binding.ivBg)

                 Glide.with(binding.ivHeard.context)
                    .load(avatarUrl)
                    .placeholder( R.mipmap.icon_synthesize_placeholder)
                    .error(R.mipmap.icon_synthesize_placeholder)
                    .transform(CenterCrop(), CircleCrop())
                    .into(binding.ivHeard)
            } else {
                // 设置默认图片
                binding.ivBg.setImageResource(R.mipmap.icon_synthesize_placeholder)
                binding.ivHeard.setImageResource(R.mipmap.icon_synthesize_placeholder)
            }
        }
    }
    
    class LoadingViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        // 加载更多的ViewHolder，可以在这里添加加载动画
    }

    class SynthesizeDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Character && newItem is Character) {
                oldItem.id == newItem.id
            } else {
                oldItem == newItem
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is Character && newItem is Character) {
                // 优化比较逻辑，只比较关键字段
                oldItem.id == newItem.id &&
                oldItem.name == newItem.name
            } else {
                oldItem == newItem
            }
        }
    }
}

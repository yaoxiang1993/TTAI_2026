package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ttai.R
import com.example.ttai.databinding.ItemModelBinding
import com.example.ttai.bean.CollectItem
import com.example.ttai.utils.DensityUtils

class CollectAdapter(
    private val onPlayClick: (CollectItem?) -> Unit,
    private val onCollectClick: (CollectItem) -> Unit
) : ListAdapter<CollectItem, CollectAdapter.CollectViewHolder>(CollectDiffCallback()) {

    var playItem :CollectItem? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectViewHolder {
        val binding = ItemModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class CollectViewHolder(private val binding: ItemModelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CollectItem) {
            binding.tvName.text = item.name

            if( item.id == playItem?.id ){
                binding.ivPlay.setImageResource( R.mipmap.icon_pause)
                binding.ivGifPlay.isVisible = true
                Glide.with(binding.ivGifPlay.context)
                    .load(R.mipmap.gif_play_1)
                    .into(binding.ivGifPlay)
            }else{
                binding.ivPlay.setImageResource( R.mipmap.icon_play)
                binding.ivGifPlay.isVisible = false
            }
            
            // 设置收藏状态图标
            binding.ivCollected.setImageResource(
                if (item.isFavorited)
                   R.mipmap.icon_have_collected
                else 
                   R.mipmap.icon_have_collected_normal
            )
            item.imageUrl?.let {
                Glide.with(  binding.ivIcon.context)
                    .load(it)
                    .apply(RequestOptions().transform(RoundedCorners(DensityUtils.dpToPx(5.0f))))
                    .into(binding.ivIcon)
            }
            
            // 设置点击事件
            binding.ivPlay.setOnClickListener {
                if (playItem == null|| item.id!=playItem?.id){
                    playItem = item
                }else{
                    playItem = null
                }
                notifyDataSetChanged()
                onPlayClick(playItem)
            }
            binding.ivCollected.setOnClickListener {
                onCollectClick(item)
            }
        }
    }

    class CollectDiffCallback : DiffUtil.ItemCallback<CollectItem>() {
        override fun areItemsTheSame(oldItem: CollectItem, newItem: CollectItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CollectItem, newItem: CollectItem): Boolean {
            return oldItem == newItem
        }
    }
}
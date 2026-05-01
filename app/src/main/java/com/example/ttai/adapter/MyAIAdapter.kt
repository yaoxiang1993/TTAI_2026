package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ttai.R
import com.example.ttai.databinding.ItemMyAiListBinding
import com.example.ttai.bean.Character
import com.example.ttai.utils.CommontUtils
import com.example.ttai.utils.ReviewStatusUtils
import java.text.SimpleDateFormat
import java.util.*

class MyAIAdapter(
    private val onItemClick: (Character) -> Unit = {},
    private val onEditClick: (Character) -> Unit = {},
    private val onDeleteClick: (Character) -> Unit = {},
    private val onTopClick: (Character) -> Unit = {},
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private var myAIList: List<Character> = emptyList()
    private var isLoadingMore = false

    fun updateData(newList: List<Character>) {
        android.util.Log.d("MyAIAdapter", "更新数据: 旧数量=${myAIList.size}, 新数量=${newList.size}")
        if (newList.isNotEmpty()) {
            android.util.Log.d("MyAIAdapter", "第一个角色: id=${newList[0].id}, name=${newList[0].name}")
        }
        myAIList = newList
        notifyDataSetChanged()
    }
    
    fun setLoadingMore(loading: Boolean) {
        isLoadingMore = loading
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < myAIList.size) VIEW_TYPE_ITEM else VIEW_TYPE_LOADING
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = ItemMyAiListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                MyAIViewHolder(binding)
            }
            VIEW_TYPE_LOADING -> {
                val loadingView = LayoutInflater.from(parent.context).inflate(R.layout.item_loading_more, parent, false)
                LoadingViewHolder(loadingView)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MyAIViewHolder -> {
                holder.bind(myAIList[position])
            }
            is LoadingViewHolder -> {
                // 不需要绑定数据
            }
        }
    }

    override fun getItemCount(): Int {
        return myAIList.size + if (isLoadingMore) 1 else 0
    }

    inner class MyAIViewHolder(
        private val binding: ItemMyAiListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(myAIList[position])
                }
            }
            
            // 添加长按事件
            binding.ivMore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showContextMenu(myAIList[position])
                }
                true
            }
        }

        fun bind(myAI: Character) {
            // 设置角色名称
            binding.tvName.text = myAI.name

            // 设置状态（公开/私密 + 审核状态）
            val statusText = buildStatusText(myAI)
            binding.tvState.text = statusText
            setStateUI(myAI)

            binding.ivPermanentMemory.isVisible = myAI.permanentMemoryEnabled == true

            // 设置最后聊天消息
            binding.tvChatMessage.text = myAI.lastMessage ?: "暂无聊天记录"

            binding.tvTime.text = CommontUtils.formatTime(myAI.lastMessageTime)

            binding.ivIsPinned.isVisible = myAI.isPinned == true

            // 加载头像
            if (!myAI.avatarUrl.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(myAI.avatarUrl)
                    .placeholder(R.mipmap.icon_heard_cricle)
                    .error(R.mipmap.icon_heard_cricle)
                    .circleCrop()
                    .into(binding.ivIcon)
            } else {
                binding.ivIcon.setImageResource(R.mipmap.icon_heard_cricle)
            }
        }

        /**
         * 显示上下文菜单（编辑和删除选项）
         */
        private fun showContextMenu(myAI: Character) {
            val popupView = LayoutInflater.from(binding.ivMore.context).inflate(R.layout.popup_my_function, null)
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )
            var yoff :Int = -binding.ivMore.height
            popupWindow.showAsDropDown(binding.ivMore, 0, yoff)
            popupView.findViewById<TextView>(R.id.tvEdite).setOnClickListener {
                // 编辑
                onEditClick(myAI)
                popupWindow.dismiss()
            }
            popupView.findViewById<TextView>(R.id.tvDelete).setOnClickListener {
                // 删除
                onDeleteClick(myAI)
                popupWindow.dismiss()
            }
           var textView :TextView = popupView.findViewById<TextView>(R.id.tvTop)
            textView.text = if (myAI.isPinned == true){
                "取消置顶"
            }else{
                "置顶"
            }
            popupView.findViewById<TextView>(R.id.tvTop).setOnClickListener {
                // 回溯消息
                onTopClick(myAI)
                popupWindow.dismiss()
            }
        }

        private fun buildStatusText(myAI: Character): String {
            val context = binding.root.context
            
            // 首先检查审核状态
            val reviewStatusText = when {
                ReviewStatusUtils.isPending(myAI.reviewStatus,myAI.isPublic) ->
                    context.getString(R.string.review_status_pending)
                ReviewStatusUtils.isRejected(myAI.reviewStatus,myAI.isPublic) ->
                    context.getString(R.string.review_status_rejected)
                ReviewStatusUtils.isApproved(myAI.reviewStatus) -> {
                    // 审核通过后显示公开/私密状态
                    if (myAI.isPublic == true) "已公开" else "私密"
                }
                else -> {
                    // 默认显示公开/私密状态
                    if (myAI.isPublic == true) "已公开" else "私密"
                }
            }
            
            return reviewStatusText
        }

        private fun setStateUI(myAI: Character)  {
            val context = binding.root.context
             var background = 0
             var textColor =  when {
                 ReviewStatusUtils.isPending(myAI.reviewStatus, myAI.isPublic) -> {
                 background = R.drawable.bg_stroke_88888a_r50
                 context.getColor(R.color.color_88888a)
             }
                ReviewStatusUtils.isApproved(myAI.reviewStatus) -> {
                    // 审核通过后根据公开状态显示颜色
                    if (myAI.isPublic == true) {
                        background = R.drawable.bg_stroke_primary_r50
                        context.getColor(R.color.primary_color)
                    } else {
                        background = R.drawable.bg_stroke_88888a_r50
                        context.getColor(R.color.color_6b6b6b)
                    }
                }
                else -> {
                    // 默认根据公开状态显示颜色
                    if (myAI.isPublic == true) {
                        background = R.drawable.bg_stroke_primary_r50
                        context.getColor(R.color.primary_color)
                    } else {
                        background = R.drawable.bg_stroke_88888a_r50
                        context.getColor(R.color.color_6b6b6b)
                    }
                }
            }

            binding.tvState.setTextColor(textColor)
            binding.tvState.setBackgroundResource(background)
        }
        
        private fun formatTime(timeString: String): String {
            if (timeString.isEmpty()) return "未知时间"
            
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val date = inputFormat.parse(timeString)
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                
                when {
                    diffInMillis < 60 * 1000 -> "刚刚"
                    diffInMillis < 60 * 60 * 1000 -> "${diffInMillis / (60 * 1000)}分钟前"
                    diffInMillis < 24 * 60 * 60 * 1000 -> "${diffInMillis / (60 * 60 * 1000)}小时前"
                    diffInMillis < 30 * 24 * 60 * 60 * 1000L -> "${diffInMillis / (24 * 60 * 60 * 1000)}天前"
                    else -> {
                        val outputFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
                        outputFormat.format(date)
                    }
                }
            } catch (e: Exception) {
                "未知时间"
            }
        }
    }
    
    class LoadingViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView)
}
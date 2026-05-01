package com.example.ttai.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.ttai.R
import com.example.ttai.bean.AiModel
import com.example.ttai.bean.BranchItem
import com.example.ttai.bean.Character
import com.example.ttai.bean.ChatModelItem
import com.example.ttai.databinding.ItemModelBinding
import com.example.ttai.bean.CollectItem
import com.example.ttai.databinding.ItemBranchManagerBinding
import com.example.ttai.databinding.ItemChatModelBinding
import com.example.ttai.utils.CommontUtils
import com.example.ttai.utils.DensityUtils

class BranchManagerListAdapter(
    private val onModelClick: (BranchItem) -> Unit ={},
    private val onEditClick: (BranchItem) -> Unit = {},
    private val onDeleteClick: (BranchItem) -> Unit = {},
    private val onTopClick: (BranchItem) -> Unit = {},
) : ListAdapter<BranchItem, BranchManagerListAdapter.BranchManagerViewHolder>(ChatModelDiffCallback()) {

    private var selectedModel: BranchItem? = null
    fun getSelectedModel(): BranchItem? = selectedModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchManagerViewHolder {
        val binding = ItemBranchManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BranchManagerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BranchManagerViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class BranchManagerViewHolder(private val binding: ItemBranchManagerBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val model = getItem(position)
                    selectedModel = model
                    onModelClick(model)
                    notifyDataSetChanged()
                }
            }
            binding.ivMore.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val model = getItem(position)
                    showContextMenu(model)
                }

            }
        }
        /**
         * 显示上下文菜单（编辑和删除选项）
         */
        private fun showContextMenu(item: BranchItem) {
            val popupView = LayoutInflater.from(binding.ivMore.context).inflate(R.layout.popup_branch_manager, null)
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
                onEditClick(item)
                popupWindow.dismiss()
            }
            popupView.findViewById<TextView>(R.id.tvDelete).setOnClickListener {
                // 删除
                onDeleteClick(item)
                popupWindow.dismiss()
            }
            var textView :TextView = popupView.findViewById<TextView>(R.id.tvTop)
            textView.text = if (item.is_pinned == true){
                "取消置顶"
            }else{
                "置顶"
            }
            popupView.findViewById<TextView>(R.id.tvTop).setOnClickListener {
                // 置顶消息
                onTopClick(item)
                popupWindow.dismiss()
            }
        }
        fun bind(item: BranchItem) {
            binding.tvName.text = item.name
            binding.tvDescription.text = item.last_message
            binding.tvTime.text = CommontUtils.formatTime(item.last_message_time)
            var isSelected = false
            if (item.isDefaultModel || item.branch_id == selectedModel?.branch_id ){
                isSelected = true
                selectedModel = item
                item.isDefaultModel = false
            }
            binding.ivMore.isVisible = item.branch_id?.isNotEmpty() == true
            binding.ivActive.visibility = if (item.is_active) View.VISIBLE else View.GONE
            binding.ivIsPinned.visibility = if (item.is_pinned) View.VISIBLE else View.GONE
            binding.root.setBackgroundResource(if (isSelected)R.drawable.bg_stroke_primary_r10 else R.drawable.bg_383838_r10)
        }
    }
    class ChatModelDiffCallback : DiffUtil.ItemCallback<BranchItem>() {
        override fun areItemsTheSame(oldItem: BranchItem, newItem: BranchItem): Boolean {
            return oldItem.branch_id == newItem.branch_id
        }

        override fun areContentsTheSame(oldItem: BranchItem, newItem: BranchItem): Boolean {
            return oldItem == newItem
        }
    }
}
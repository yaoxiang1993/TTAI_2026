package com.example.ttai.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.example.ttai.bean.ChatModelItem
import com.example.ttai.databinding.ItemModelBinding
import com.example.ttai.bean.CollectItem
import com.example.ttai.databinding.ItemChatModelBinding
import com.example.ttai.utils.DensityUtils

class ChatModelListAdapter(
    private val onModelClick: (ChatModelItem) -> Unit
) : ListAdapter<ChatModelItem, ChatModelListAdapter.ChatModelViewHolder>(ChatModelDiffCallback()) {

    private var selectedModel: ChatModelItem? = null
    fun getSelectedModel(): ChatModelItem? = selectedModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val binding = ItemChatModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatModelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatModelViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class ChatModelViewHolder(private val binding: ItemChatModelBinding) : RecyclerView.ViewHolder(binding.root) {

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
        }

        fun bind(item: ChatModelItem) {
            binding.tvName.text = item.name
            applyPriceStyle(binding.tvPriceText,item)
            binding.tvDescription.text = item.description
            var isSelected = false
            if (item.isDefaultModel || item.id == selectedModel?.id ){
                isSelected = true
                selectedModel = item
                item.isDefaultModel = false
            }
            // 更新选中状态
            binding.iv.visibility = if (isSelected) android.view.View.VISIBLE else android.view.View.GONE
            binding.root.setBackgroundResource(if (isSelected)R.drawable.bg_stroke_primary_r10 else R.drawable.bg_383838_r10)
        }
    }

    fun applyPriceStyle(priceTextView: TextView, item: ChatModelItem) {
        val originalText = item.original_price.toString() // "20"
        val currentText = item.cost_per_message.toString() // "10"

        // 完整文本结构: "仙玉·仙贝" + "20" + "10" + "/次"
        val prefix = item.currency // "仙玉·仙贝"
        val suffix = "/次"

        // 构造完整的字符串: "仙玉·仙贝2010/次"
        val fullText = "$prefix$originalText$currentText$suffix"

        // --- 计算各个部分的起始/结束位置 ---

        val spannableString = SpannableString(fullText)

        // "20" 的位置
        val originalPriceStart = prefix.length // "仙玉·仙贝" 后面
        val originalPriceEnd = originalPriceStart + originalText.length // "20" 后面

        // "10" 的位置
        val currentPriceStart = originalPriceEnd // "20" 后面
        val currentPriceEnd = fullText.length // 整个字符串的末尾

        // --- 获取颜色资源 ---

        // 替换为您项目中的实际 ID
        val grayColor = ContextCompat.getColor(priceTextView.context, R.color.white)
        val highlightColor = ContextCompat.getColor(priceTextView.context, R.color.primary_color)

        // ----------------------------------------------------------------
        // 1. 设置 "20" 的样式 (置灰 + 中划线)
        // ----------------------------------------------------------------

        // 中划线 (横线划掉)
        spannableString.setSpan(
            StrikethroughSpan(),
            originalPriceStart,
            originalPriceEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 置灰颜色
        spannableString.setSpan(
            ForegroundColorSpan(grayColor),
            originalPriceStart,
            originalPriceEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // ----------------------------------------------------------------
        // 2. 设置 "仙玉·仙贝" 和 "10/次" 的颜色 (通常用突出色或默认色)
        // 这里我们假设将 "10/次" 设为突出色
        // ----------------------------------------------------------------
        spannableString.setSpan(
            ForegroundColorSpan(highlightColor),
            currentPriceStart,
            currentPriceEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        // 可选：设置前缀 "仙玉·仙贝" 的颜色（如果需要，否则它将使用 TextView 默认颜色）
        // spannableString.setSpan(
        //     ForegroundColorSpan(highlightColor),
        //     0,
        //     prefix.length,
        //     Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        // )

        // 3. 应用到 TextView
        priceTextView.text = spannableString
    }
    class ChatModelDiffCallback : DiffUtil.ItemCallback<ChatModelItem>() {
        override fun areItemsTheSame(oldItem: ChatModelItem, newItem: ChatModelItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatModelItem, newItem: ChatModelItem): Boolean {
            return oldItem == newItem
        }
    }
}
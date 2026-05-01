package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.databinding.ItemAiModelBinding
import com.example.ttai.bean.AiModel

class AiModelAdapter(
    private val onModelClick: (AiModel) -> Unit
) : ListAdapter<AiModel, AiModelAdapter.ViewHolder>(AiModelDiffCallback()) {

    private var selectedModel: AiModel? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAiModelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedModel(): AiModel? = selectedModel

    inner class ViewHolder(
        private val binding: ItemAiModelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

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

        fun bind(model: AiModel) {
            binding.tvModelName.text = model.name
            binding.tvModelDescription.text = model.description
            
            // 更新选中状态
            val isSelected = selectedModel?.id == model.id
            binding.tvSelected.visibility = if (isSelected) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private class AiModelDiffCallback : DiffUtil.ItemCallback<AiModel>() {
        override fun areItemsTheSame(oldItem: AiModel, newItem: AiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AiModel, newItem: AiModel): Boolean {
            return oldItem == newItem
        }
    }
} 
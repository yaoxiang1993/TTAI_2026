package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.R
import com.example.ttai.bean.VoiceOption

class VoiceConfigAdapter(
    private var items: List<VoiceOption>,
    private val onItemClick: (VoiceOption) -> Unit
) : RecyclerView.Adapter<VoiceConfigAdapter.VoiceConfigViewHolder>() {

    class VoiceConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val ivState: ImageView = itemView.findViewById(R.id.iv_state)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoiceConfigViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_voice_config, parent, false)
        return VoiceConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoiceConfigViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.voiceName // 假设 MembershipItem 已改为包含 voiceName
        // 根据选中状态更新 UI
        if (item.isSelected) {
            holder.ivState.setImageResource(R.mipmap.icon_publis_type_1)
        } else {
            holder.ivState.setImageResource(R.mipmap.icon_publis_type_2)
        }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<VoiceOption>?) {
        items = newItems?:emptyList()
        notifyDataSetChanged()
    }
} 
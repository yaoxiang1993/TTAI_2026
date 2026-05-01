package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.R
import com.example.ttai.bean.MembershipItem

class MembershipAdapter(
    private var items: List<MembershipItem>,
    private val onItemClick: (MembershipItem) -> Unit
) : RecyclerView.Adapter<MembershipAdapter.MembershipViewHolder>() {

    class MembershipViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVipName: TextView = itemView.findViewById(R.id.tvVipName)
        val tvValue: TextView = itemView.findViewById(R.id.tvValue)
        val tvTip: TextView = itemView.findViewById(R.id.tvTip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembershipViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_membership, parent, false)
        return MembershipViewHolder(view)
    }

    override fun onBindViewHolder(holder: MembershipViewHolder, position: Int) {
        val item = items[position]
        
        // 添加调试日志
        android.util.Log.d("MembershipAdapter", "绑定ViewHolder - 位置: $position, 项目: ${item.vipName}")
        
        holder.tvVipName.text = item.vipName
        holder.tvValue.text = item.value
        holder.tvTip.text = item.tip
        
        // 设置选中状态
        if (item.isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_stroke_ffd400_1dp_rounded)
            holder.tvVipName.setTextColor(holder.itemView.context.getColor(R.color.color_ffd400))
            holder.tvValue.setTextColor(holder.itemView.context.getColor(R.color.color_ffd400))
            holder.tvTip.setTextColor(holder.itemView.context.getColor(R.color.color_ffd400))
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_stroke_cc6b6b6b_26221f_1dp_rounded)
            holder.tvVipName.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            holder.tvValue.setTextColor(holder.itemView.context.getColor(android.R.color.white))
            holder.tvTip.setTextColor(holder.itemView.context.getColor(R.color.color_bdbdbd))
        }
        
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MembershipItem>) {
        android.util.Log.d("MembershipAdapter", "updateItems - 新项目数量: ${newItems.size}")
        android.util.Log.d("MembershipAdapter", "updateItems - 项目详情: ${newItems.map { "${it.vipName}:${it.value}" }}")
        items = newItems
        notifyDataSetChanged()
    }

    fun updateSelection(selectedId: String) {
        val updatedItems = items.map { 
            it.copy(isSelected = it.id == selectedId) 
        }
        updateItems(updatedItems)
    }
} 
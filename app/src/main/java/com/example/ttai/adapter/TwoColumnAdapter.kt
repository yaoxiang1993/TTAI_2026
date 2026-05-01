package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.R
import com.example.ttai.bean.GridItem

class TwoColumnAdapter(
    private val items: List<GridItem>,
    private val onItemClick: (GridItem) -> Unit
) : RecyclerView.Adapter<TwoColumnAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imageView: ImageView = itemView.findViewById(R.id.itemImage)
//        val textView: TextView = itemView.findViewById(R.id.itemText)

        init {
            // 点击事件
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(items[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_synthesize, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
//        holder.textView.text = item.text
//        holder.imageView.setImageResource(item.imageRes)
    }

    override fun getItemCount(): Int = items.size
}
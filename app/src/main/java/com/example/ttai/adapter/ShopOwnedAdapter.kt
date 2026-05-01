package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.R
import com.example.ttai.databinding.ItemShopBinding
import com.example.ttai.bean.OwnedItem

/**
 * 已拥有商品适配器
 */
class ShopOwnedAdapter(
    private val onItemClick: (OwnedItem) -> Unit
) : RecyclerView.Adapter<ShopOwnedAdapter.ShopOwnedViewHolder>() {

    private var ownedItems: List<OwnedItem> = emptyList()

    /**
     * 更新已拥有商品列表
     */
    fun updateItems(newItems: List<OwnedItem>) {
        ownedItems = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopOwnedViewHolder {
        val binding = ItemShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopOwnedViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopOwnedViewHolder, position: Int) {
        holder.bind(ownedItems[position])
    }

    override fun getItemCount(): Int = ownedItems.size

    /**
     * ViewHolder
     */
    inner class ShopOwnedViewHolder(
        private val binding: ItemShopBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(ownedItems[position])
                }
            }
        }

        fun bind(ownedItem: OwnedItem) {
            binding.apply {
                // 设置商品名称
                tvShopName.text = ownedItem.itemName
                
                // 设置数量显示
                tvPrice.text = "数量: ${ownedItem.totalQuantity}"
                tvPrice.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, null, null
                )
                // 根据商品类型设置图标
                binding.ivHeard.setImageResource(R.mipmap.icon_shop_item_card)
            }
        }

    }
}

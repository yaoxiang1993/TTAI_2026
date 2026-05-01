package com.example.ttai.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ttai.R
import com.example.ttai.databinding.ItemShopBinding
import com.example.ttai.bean.ShopItem

/**
 * 商店商品列表适配器
 */
class ShopItemsAdapter(
    private val onItemClick: (ShopItem) -> Unit,
    private val onShowDialogClick: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopItemsAdapter.ShopItemViewHolder>() {

    private var shopItems: List<ShopItem> = emptyList()
    private var selectedItemId: String? = null

    /**
     * 更新商品列表
     */
    fun updateItems(newItems: List<ShopItem>) {
        shopItems = newItems
        notifyDataSetChanged()
    }
    
    /**
     * 设置选中状态
     */
    fun setSelectedItem(itemId: String?) {
        selectedItemId = itemId
        notifyDataSetChanged()
    }
    
    /**
     * 获取当前选中的商品ID
     */
    fun getSelectedItemId(): String? = selectedItemId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopItemViewHolder {
        val binding = ItemShopBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ShopItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopItemViewHolder, position: Int) {
        holder.bind(shopItems[position])
    }

    override fun getItemCount(): Int = shopItems.size

    /**
     * ViewHolder
     */
    inner class ShopItemViewHolder(
        private val binding: ItemShopBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(shopItems[position])
                }
            }
            binding.tvShopName.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onShowDialogClick(shopItems[position])
                }
            }

        }

        fun bind(shopItem: ShopItem) {
            binding.apply {
                // 设置商品名称
                tvShopName.text = shopItem.name
                
                // 设置商品价格
                tvPrice.text = "${shopItem.price}"
                
                // 根据商品状态设置UI
                if (shopItem.isActive) {
                    // 商品可用状态
                    root.isEnabled = true
                    clyCard.alpha = 1.0f
                } else {
                    // 商品不可用状态
                    root.isEnabled = false
                    clyCard.alpha = 0.5f
                }
                
                // 设置选中状态背景
                val isSelected = selectedItemId == shopItem.itemId
                if (isSelected) {
                    clyCard.setBackgroundResource(R.drawable.bg_stroke_ab00ff_1dp_383838)
                } else {
                    clyCard.setBackgroundResource(R.drawable.bg_383838_r10)
                }
                
                // 根据商品类型设置图标
                setItemIcon(shopItem.itemId)
            }
        }

        /**
         * 设置商品图标
         */
        private fun setItemIcon(itemId: String) {
            binding.ivHeard.setImageResource( R.mipmap.icon_shop_item_card)
        }


    }
}

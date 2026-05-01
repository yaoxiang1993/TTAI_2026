package com.example.ttai.ui.fragment

import android.util.Log
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.databinding.FragmentShopBinding
import com.example.ttai.adapter.ShopOwnedAdapter
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.OwnedItem
import com.example.ttai.event.BuyShopEvent
import com.example.ttai.event.ClearChatEvent
import com.example.ttai.intent.ChatIntent
import com.example.ttai.intent.ShopOwnedFragmentIntent
import com.example.ttai.state.ShopOwnedFragmentState
import com.example.ttai.ui.vm.ShopOwnedFragmentViewModel
import com.example.ttai.ui.vm.ShopOwnedFragmentViewModelFactory
import org.greenrobot.eventbus.EventBus

/**
 * 已拥有商品Fragment
 * 显示用户已购买的商品
 */
class ShopOwnedFragment : BaseMviFragment<ShopOwnedFragmentIntent, ShopOwnedFragmentState, ShopOwnedFragmentViewModel, FragmentShopBinding>() {
    
    override val viewModel: ShopOwnedFragmentViewModel by viewModels { ShopOwnedFragmentViewModelFactory(requireContext()) }
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentShopBinding.inflate(inflater, container, attachToParent)
    }
    
    private lateinit var shopOwnedAdapter: ShopOwnedAdapter

    override fun setupViews() {
        try {
            EventBus.getDefault().register(this)
            Log.d("ShopOwnedFragment", "EventBus注册成功")
        } catch (e: Exception) {
            Log.w("ShopOwnedFragment", "EventBus注册失败: ${e.message}")
        }

        binding.tvBuy.isVisible = false
        setupRecyclerView()
        sendIntent(ShopOwnedFragmentIntent.Initialize)
    }

    override fun render(state: ShopOwnedFragmentState) {
        // 更新已拥有商品列表
        updateOwnedItems(state.ownedItems)

        // 处理错误信息
        state.errorMessage?.let { errorMessage ->
            // TODO: 显示错误提示
        }
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        shopOwnedAdapter = ShopOwnedAdapter { ownedItem ->
            // 已拥有商品点击事件
            onOwnedItemClick(ownedItem)
        }

        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shopOwnedAdapter
        }
    }



    /**
     * 更新已拥有商品列表
     */
    private fun updateOwnedItems(ownedItems: List<OwnedItem>) {
        shopOwnedAdapter.updateItems(ownedItems)
        
        // 由于fragment_shop.xml没有空状态视图，这里只更新适配器数据
        // 如果需要空状态显示，可以考虑在Activity层面实现
    }

    /**
     * 已拥有商品点击事件处理
     */
    private fun onOwnedItemClick(ownedItem: OwnedItem) {
        // TODO: 实现已拥有商品点击逻辑
        // 例如：显示商品详情或使用商品

    }

    /**
     * 显示记忆之芯使用对话框
     */
    private fun showMemoryCoreUsageDialog(ownedItem: OwnedItem) {
        val message = "记忆之芯\n\n${ownedItem.description}\n\n您可以使用记忆之芯为角色分配永久记忆权益。\n\n拥有数量：${ownedItem.totalQuantity}"
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("记忆之芯")
            .setMessage(message)
            .setPositiveButton("分配记忆") { _, _ ->
                // TODO: 跳转到记忆分配页面
                navigateToMemoryAllocation()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 显示商品详情对话框
     */
    private fun showItemDetailDialog(ownedItem: OwnedItem) {
        val message = "${ownedItem.itemName}\n\n${ownedItem.description}\n\n拥有数量：${ownedItem.totalQuantity}\n购买时间：${ownedItem.lastPurchaseTime}"
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("商品详情")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }

    /**
     * 跳转到记忆分配页面
     */
    private fun navigateToMemoryAllocation() {
        // TODO: 实现跳转到记忆分配页面的逻辑
        // 例如：startActivity(Intent(context, MemoryAllocationActivity::class.java))
    }

    /**
     * 购买商品成功
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onBuyShopEvent(event: BuyShopEvent?) {
        sendIntent(ShopOwnedFragmentIntent.Initialize)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            EventBus.getDefault().unregister(this)
            Log.d("shopOwnerFragment", "EventBus注销成功")
        } catch (e: Exception) {
            Log.w("shopOwnerFragment", "EventBus注销失败: ${e.message}")
        }
    }

}

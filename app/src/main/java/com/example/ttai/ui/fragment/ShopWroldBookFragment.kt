package com.example.ttai.ui.fragment

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.databinding.FragmentShopBinding
import com.example.ttai.adapter.ShopItemsAdapter
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.ShopItem
import com.example.ttai.databinding.FragmentShopBeautifyBinding
import com.example.ttai.databinding.FragmentShopWroldBookBinding
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.event.BuyShopEvent
import com.example.ttai.event.UserBalanceUpdateEvent
import com.example.ttai.intent.ShopBeautifyFragmentIntent
import com.example.ttai.intent.ShopFragmentIntent
import com.example.ttai.intent.ShopWroldBookFragmentIntent
import com.example.ttai.state.ShopBeautifyFragmentState
import com.example.ttai.state.ShopFragmentState
import com.example.ttai.state.ShopWroldBookFragmentState
import com.example.ttai.ui.vm.ShopBeautifyFragmentViewModel
import com.example.ttai.ui.vm.ShopBeautifyFragmentViewModelFactory
import com.example.ttai.ui.vm.ShopFragmentViewModel
import com.example.ttai.ui.vm.ShopFragmentViewModelFactory
import com.example.ttai.ui.vm.ShopWroldBookFragmentViewModel
import com.example.ttai.ui.vm.ShopWroldBookFragmentViewModelFactory
import org.greenrobot.eventbus.EventBus

/**
 * 商店商品列表Fragment
 * 显示商店中可购买的商品
 */
class ShopWroldBookFragment : BaseMviFragment<ShopWroldBookFragmentIntent, ShopWroldBookFragmentState, ShopWroldBookFragmentViewModel, FragmentShopWroldBookBinding>() {
    
    override val viewModel: ShopWroldBookFragmentViewModel by viewModels {
        ShopWroldBookFragmentViewModelFactory(
            requireContext()
        )
    }
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentShopWroldBookBinding.inflate(inflater, container, attachToParent)
    }
    
    private lateinit var shopItemsAdapter: ShopItemsAdapter
    private var selectedItemId: String? = null
    private var lastPurchaseSuccessTime: Long = 0 // 防止重复显示购买成功提示

    override fun setupViews() {
        binding.tvBuy.setOnClickListener {
            onBuyButtonClick()
        }
        binding.mRecyclerView.isVisible = false
        binding.tvBuy.isVisible = false

        //setupRecyclerView()
        // setupSwipeRefresh()
        //sendIntent(ShopBeautifyFragmentIntent.Initialize)
    }

    override fun render(state: ShopWroldBookFragmentState) {
        // 更新商品列表
       // updateShopItems(state.shopItems)
        
        // 处理加载状态
        if (state.isLoading) {
            showLoading()
        } else {
            hideLoading()
        }
        
        // 处理错误信息
        state.errorMessage?.let { errorMessage ->
            showToast(errorMessage)
        }
        
        // 处理购买成功消息
        state.purchaseSuccess?.let { message ->
            if (message) {
                val currentTime = System.currentTimeMillis()
                // 防止在短时间内重复显示购买成功提示（1秒内）
                if (currentTime - lastPurchaseSuccessTime > 1000) {
                    showToast("购买成功")
                    EventBus.getDefault().post(BuyShopEvent())
                    EventBus.getDefault().post(UserBalanceUpdateEvent())
                    // 清除选中状态
                    clearSelection()
                    lastPurchaseSuccessTime = currentTime
                }
                // 延迟重置购买成功状态，避免在render方法中直接修改状态
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.resetPurchaseSuccess()
                }, 100)
            }
        }
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        shopItemsAdapter = ShopItemsAdapter(
            onItemClick = { shopItem ->
                // 点击AI角色时跳转到聊天页面
                onShopItemClick(shopItem)
            },
            onShowDialogClick = { shopItem ->
                // 点击AI角色时跳转到聊天页面
               // onShowDialogClick(shopItem)
            }

        )

        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shopItemsAdapter
        }
    }

    /**
     * 设置下拉刷新
     */
    private fun setupSwipeRefresh() {
        // 由于fragment_shop.xml没有SwipeRefreshLayout，这里暂时不实现下拉刷新
        // 如果需要下拉刷新功能，可以考虑在Activity层面实现
    }

    /**
     * 更新商品列表数据
     */
    fun updateShopItems(shopItems: List<ShopItem>) {
        shopItemsAdapter.updateItems(shopItems)
        
        // 由于fragment_shop.xml没有空状态视图，这里只更新适配器数据
        // 如果需要空状态显示，可以考虑在Activity层面实现
    }

    /**
     * 显示加载状态
     */
    fun showLoading() {
        // 由于fragment_shop.xml没有加载指示器，这里暂时不实现
        // 如果需要加载状态显示，可以考虑在Activity层面实现
    }

    /**
     * 隐藏加载状态
     */
    fun hideLoading() {
        // 由于fragment_shop.xml没有加载指示器，这里暂时不实现
        // 如果需要加载状态显示，可以考虑在Activity层面实现
    }

    /**
     * 商品点击事件处理
     */
    private fun onShopItemClick(shopItem: ShopItem) {
        // 更新选中状态
        selectedItemId = if (selectedItemId == shopItem.itemId) {
            null // 如果点击的是已选中的商品，则取消选中
        } else {
            shopItem.itemId // 否则选中当前商品
        }
        
        // 更新适配器的选中状态
        shopItemsAdapter.setSelectedItem(selectedItemId)

    }

    /**
     * 获取当前选中的商品ID
     */
    fun getSelectedItemId(): String? = selectedItemId
    
    /**
     * 设置选中状态
     */
    fun setSelectedItem(itemId: String?) {
        selectedItemId = itemId
        shopItemsAdapter.setSelectedItem(itemId)
    }
    
    /**
     * 清除选中状态
     */
    fun clearSelection() {
        selectedItemId = null
        shopItemsAdapter.setSelectedItem(null)
    }
    
    /**
     * 购买按钮点击事件
     */
    private fun onBuyButtonClick() {
        val selectedId = selectedItemId
        if (selectedId == null) {
            // 没有选中商品，提示用户
            showToast("请先选择要购买的商品")
            return
        }
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "确认购买当前商品",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 执行购买操作
                performPurchase(selectedId)
            }

            override fun onNegativeClick() {
            }
        })
        dialog.show(childFragmentManager, "RemoveFromSlotDialog")

    }
    
    /**
     * 执行购买操作
     */
    private fun performPurchase(itemId: String) {
        sendIntent(ShopWroldBookFragmentIntent.PurchaseItem(itemId, 1))
    }
    
    /**
     * 显示Toast消息
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

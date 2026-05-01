package com.example.ttai.ui.fragment

import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.databinding.FragmentShopBinding
import com.example.ttai.adapter.ShopItemsAdapter
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.ShopItem
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.event.BuyShopEvent
import com.example.ttai.event.UserBalanceUpdateEvent
import com.example.ttai.intent.MyFragmentIntent
import com.example.ttai.intent.ShopFragmentIntent
import com.example.ttai.state.ShopFragmentState
import com.example.ttai.ui.dialog.OneButtonDialogFragment
import com.example.ttai.ui.vm.ShopFragmentViewModel
import com.example.ttai.ui.vm.ShopFragmentViewModelFactory
import org.greenrobot.eventbus.EventBus

/**
 * 商店商品列表Fragment
 * 显示商店中可购买的商品
 */
class ShopItemsFragment : BaseMviFragment<ShopFragmentIntent, ShopFragmentState, ShopFragmentViewModel, FragmentShopBinding>() {
    
    override val viewModel: ShopFragmentViewModel by viewModels { ShopFragmentViewModelFactory(requireContext()) }
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentShopBinding.inflate(inflater, container, attachToParent)
    }
    
    private lateinit var shopItemsAdapter: ShopItemsAdapter
    private var selectedItemId: String? = null
    private var lastPurchaseSuccessTime: Long = 0 // 防止重复显示购买成功提示

    override fun setupViews() {
        binding.tvBuy.setOnClickListener {
            onBuyButtonClick()
        }
        setupRecyclerView()
        setupSwipeRefresh()
        sendIntent(ShopFragmentIntent.Initialize)
    }

    override fun render(state: ShopFragmentState) {
        // 更新商品列表
        updateShopItems(state.shopItems)
        
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
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
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
                onShowDialogClick(shopItem)
            }

        )
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shopItemsAdapter
        }
    }

    private fun onShowDialogClick(shopItem: ShopItem) {
        val dialog = OneButtonDialogFragment.newInstance(
            message = "激活“记忆之芯”后，您与角色的\n" +
                    "对话记录将在非转义、非缩写的逻\n" +
                    "辑基础上被角色深度学习。角色将\n" +
                    "在交流中不断理解并学习您的偏好\n" +
                    "与风格。启用该功能的角色，能够\n" +
                    "在后续故事发展中主动提及过往出\n" +
                    "现的人物、事件，并以多样方式灵\n" +
                    "活调用其记忆内容，让互动更贴合\n" +
                    "历史、更具连贯性与沉浸感。",
            positiveText = "好的"
        ).setOnButtonClickListener(object : OneButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 确认删除
            }
        })
        dialog.show(parentFragmentManager, "OneButtonDialogFragment")
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
        sendIntent(ShopFragmentIntent.PurchaseItem(itemId, 1))
    }
    
    /**
     * 显示Toast消息
     */
    private fun showToast(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

package com.example.ttai.ui.fragment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.R
import com.example.ttai.databinding.FragmentShopBinding
import com.example.ttai.adapter.ShopToyItemsAdapter
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.ShopItem
import com.example.ttai.bean.ToyShopItem
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.intent.ShopToyFragmentIntent
import com.example.ttai.state.ShopToyFragmentState
import com.example.ttai.ui.dialog.OneButtonDialogFragment
import com.example.ttai.ui.vm.ShopToyFragmentViewModel
import com.example.ttai.ui.vm.ShopToyFragmentViewModelFactory

/**
 * 商店商品列表Fragment
 * 显示 玩具
 */
class ShopToyFragment : BaseMviFragment<ShopToyFragmentIntent, ShopToyFragmentState, ShopToyFragmentViewModel, FragmentShopBinding>() {
    
    override val viewModel: ShopToyFragmentViewModel by viewModels {
        ShopToyFragmentViewModelFactory(
            requireContext()
        )
    }
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentShopBinding.inflate(inflater, container, attachToParent)
    }
    
    private lateinit var shopToyItemsAdapter: ShopToyItemsAdapter
    private var selectedItemId: String? = null
    private var lastPurchaseSuccessTime: Long = 0 // 防止重复显示购买成功提示

    override fun setupViews() {
        setupRecyclerView()
        setupSwipeRefresh()
        sendIntent(ShopToyFragmentIntent.Initialize)
    }

    override fun render(state: ShopToyFragmentState) {
        val toyList = mutableListOf<ToyShopItem>()
        state.jdLink.let {
            toyList.add(ToyShopItem("JD","京东",it,"京东购买链接",R.mipmap.icon_jd))
        }
        state.taobaoLink.let {
            toyList.add(ToyShopItem("TB","淘宝",it,"淘宝购买链接",R.mipmap.icon_tb))
        }

        // 更新商品列表
        shopToyItemsAdapter.updateItems(toyList)
        
        // 处理加载状态c
        if (state.isLoading) {
            showLoading()
        } else {
            hideLoading()
        }
        
        // 处理错误信息
        state.errorMessage?.let { errorMessage ->
            showToast(errorMessage)
        }
    }

    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        shopToyItemsAdapter = ShopToyItemsAdapter(
            onItemClick = { shopItem ->
                // 点击AI角色时跳转到聊天页面
                onShopItemClick(shopItem)
            },
            onShowDialogClick = { shopItem ->
                // 点击AI角色时跳转到聊天页面
//                onShowDialogClick(shopItem)
            }

        )
        binding.mRecyclerView.apply {
            layoutManager =  GridLayoutManager(context, 3)
            adapter = shopToyItemsAdapter
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
    private fun onShopItemClick(shopItem: ToyShopItem) {
        if (shopItem.type == "TB") {
            openTaobaoItem(context = requireContext(), shopItem.value)
        } else if (shopItem.type == "JD") {
            openJDItem(context = requireContext(), shopItem.value)
        }
    }

    fun openTaobaoItem(context: Context, url: String) {
        try {
            // 1. 尝试构造淘宝原生跳转协议 (taobao://)
            // 淘宝协议只需要保留 id 参数即可精确跳转
            val itemId = Uri.parse(url).getQueryParameter("id")
            val taobaoUri = "taobao://item.taobao.com/item.htm?id=$itemId"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(taobaoUri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 检查是否有淘宝客户端
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // 2. 如果没安装淘宝，则调用浏览器打开原始 H5 链接
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // 3. 兜底方案：如果解析失败，直接尝试浏览器打开
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(browserIntent)
        }
    }

    fun openJDItem(context: Context, url: String) {
        try {
            // 1. 从链接中提取 SKU ID (例如 10206672754676)
            // 链接格式通常为 .../123456.html
            val skuId = url.substringAfterLast("/").substringBefore(".html")

            // 2. 构造京东原生跳转协议的 JSON 参数
            // 注意：必须严格按照这个 JSON 格式，否则京东可能无法识别
            val jsonParams = """
            {"category":"jump","des":"productDetail","skuId":"$skuId","sourceType":"JSHOP_SOURCE_TYPE","sourceValue":"JSHOP_SOURCE_VALUE"}
        """.trimIndent()

            val jdUri = "openapp.jdmobile://virtual?params=$jsonParams"

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(jdUri))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 3. 检查是否安装京东
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // 未安装则浏览器打开
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        } catch (e: Exception) {
            // 兜底方案
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
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
        shopToyItemsAdapter.setSelectedItem(itemId)
    }
    
    /**
     * 清除选中状态
     */
    fun clearSelection() {
        selectedItemId = null
        shopToyItemsAdapter.setSelectedItem(null)
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

            }

            override fun onNegativeClick() {
            }
        })
        dialog.show(childFragmentManager, "RemoveFromSlotDialog")

    }

    /**
     * 显示Toast消息
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

package com.example.ttai.ui.activity

import com.example.ttai.utils.ToastUtils
import com.example.ttai.databinding.ActivityChargeMoneyBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.ChargeMoneyIntent
import com.example.ttai.state.ChargeMoneyState
import com.example.ttai.ui.vm.ChargeMoneyViewModel
import com.example.ttai.ui.vm.ChargeMoneyViewModelFactory
import androidx.activity.viewModels
import com.example.ttai.R
import android.view.View
import com.example.ttai.event.UserBalanceUpdateEvent
import org.greenrobot.eventbus.EventBus
import android.content.Intent
import android.app.Activity
import com.example.ttai.ui.dialog.BlindBoxDialogFragment

class ChargeMoneyActivity : BaseMviActivity<ChargeMoneyIntent, ChargeMoneyState, ChargeMoneyViewModel, ActivityChargeMoneyBinding>() {
    override val viewModel: ChargeMoneyViewModel by viewModels { ChargeMoneyViewModelFactory(this) }
    override val binding by viewBinding { ActivityChargeMoneyBinding.inflate(it) }
    var selectedIndex = 0 // 默认选中第一个
    var isBuyBlindBox : Boolean = false //是购买的盲盒
    var openImmediately : Boolean = false //购买的盲盒是否直接打开
    var orderId : String = "" //  订单号


    private val itemViews = mutableListOf<View>() // 记录所有itemView，便于切换选中状态

    override fun setupViews() {
        // 返回按钮
        binding.ivBack.setOnClickListener { finish() }
        // 充值按钮
        binding.btnChargeMoney.setOnClickListener {
            val packages = viewModel.state.value.rechargePackages
            if (packages.isNotEmpty() && selectedIndex < packages.size) {
                val selectedPackage = packages[selectedIndex]
                // 使用套餐ID作为orderNo，fairyJade作为充值金额
                sendIntent(ChargeMoneyIntent.Charge(selectedPackage.fairyJade))
            } else {
                ToastUtils.showShort(this, "请先选择充值套餐")
            }
        }
        // 立即购买盲盒
        binding.btnChargeBlindBox.setOnClickListener {
            sendIntent(ChargeMoneyIntent.rechargeBlindBox)
        }

        // 加载充值套餐列表
        sendIntent(ChargeMoneyIntent.LoadRechargePackages)
        // 加载充值套餐列表
        sendIntent(ChargeMoneyIntent.LoadBlindBoxInfo)
    }
    
    private fun setupRechargePackages() {
        val inflater = layoutInflater
        binding.gridLayout.removeAllViews()
        itemViews.clear()

        fun updateSelection(newIndex: Int) {
            for (i in itemViews.indices) {
                val itemView = itemViews[i]

                val tvValue = itemView.findViewById<android.widget.TextView>(R.id.tvValue)
                val tvBonusJade = itemView.findViewById<android.widget.TextView>(R.id.tvBonusJade)
                val tvMoney = itemView.findViewById<android.widget.TextView>(R.id.tvMoney)
                if (i == newIndex) {
                    itemView.setBackgroundResource(R.drawable.bg_stroke_ffd400_1dp_rounded)
                    tvValue.setTextColor(itemView.context.getColor(R.color.color_ffd400))
                    tvMoney.setTextColor(itemView.context.getColor(R.color.color_ffd400))
                    tvBonusJade.setTextColor(itemView.context.getColor(R.color.color_ffd400))
                } else {
                    itemView.setBackgroundResource(R.drawable.bg_stroke_3a3a3a_6b6b6b_1dp_rounded)
                    tvValue.setTextColor(itemView.context.getColor(android.R.color.white))
                    tvMoney.setTextColor(itemView.context.getColor(R.color.color_bdbdbd))
                    tvBonusJade.setTextColor(itemView.context.getColor(R.color.color_bdbdbd))
                }
            }
            selectedIndex = newIndex
        }

        val packages = viewModel.state.value.rechargePackages
        if (packages.isNotEmpty()) {
            for ((index, packageItem) in packages.withIndex()) {
                val itemView = inflater.inflate(R.layout.item_charge_money, binding.gridLayout, false)
                val tvBonusJade = itemView.findViewById<android.widget.TextView>(R.id.tvBonusJade)
                val tvValue = itemView.findViewById<android.widget.TextView>(R.id.tvValue)
                val tvMoney = itemView.findViewById<android.widget.TextView>(R.id.tvMoney)
                tvBonusJade.text = "加增${packageItem.bonus_jade}"
                tvValue.text = packageItem.fairyJade.toString()
                tvMoney.text = "￥${packageItem.price}"
                itemView.setOnClickListener {
                    updateSelection(index)
                }
                // 设置margin，保证item间隔10dp
                val params = itemView.layoutParams as androidx.gridlayout.widget.GridLayout.LayoutParams
                val marginPx = (8 * itemView.context.resources.displayMetrics.density).toInt() // 14dp转px
                params.setMargins(marginPx/2, marginPx/2, marginPx/2, marginPx/2)
                itemView.layoutParams = params
                itemViews.add(itemView)
                binding.gridLayout.addView(itemView)
            }
            // 默认选中第一个
            updateSelection(0)
        }
    }

    override fun render(state: ChargeMoneyState) {
        super.render(state)
        // 处理加载状态
        binding.btnChargeMoney.isEnabled = !state.isLoading && !state.isLoadingPackages
        // 显示错误信息
        state.error?.let { error ->
            ToastUtils.showShort(this, error)
        }
        // 充值成功
        if (state.chargeSuccess) {
            ToastUtils.showShort(this, "充值成功！")
            EventBus.getDefault().post(UserBalanceUpdateEvent())
            finish()
        }
        if (state.isBuyBlindBox == true) {
            isBuyBlindBox = true
        }
        if (state.openImmediately == true) {
            openImmediately = true
        }
        state.orderId?.let { orderId = it }
        if (!state.payUrl.isNullOrEmpty()) {
            openPaymentWebView(state.payUrl, state.orderId)
            sendIntent(ChargeMoneyIntent.ClearPayUrl)
        }
        
        // 处理充值套餐数据 - 只在有数据且UI为空时设置
        if (state.rechargePackages.isNotEmpty() && itemViews.isEmpty()) {
            setupRechargePackages()
        }
        binding.tvTitleTip.text = state.promotionText

        state.blindBoxDataResponse?.let {
            binding.tvText.text = it.blindBox.name
            binding.tvPrice.text = "¥ ${it.blindBox.price}"
            binding.tvText1.text = it.blindBox.rewardDescription
        }
        if (state.isShowBlindBox == true) {
            sendIntent(ChargeMoneyIntent.ClearShowBlindBox)
            state.blindBoxResult?.rewardJade?.let {
                val dialog = BlindBoxDialogFragment.newInstance(rewardJade = it)
                dialog.show(supportFragmentManager, "BlindBoxDialogFragment")
            }
        }
    }
    
    private fun openPaymentWebView(payUrl: String, orderId: String?) {
        val intent = Intent(this, PayMethodActivity::class.java).apply {
            putExtra(PaymentWebViewActivity.EXTRA_PAY_URL, payUrl)
            putExtra(PaymentWebViewActivity.EXTRA_ORDER_ID, orderId)
        }
        startActivityForResult(intent, REQUEST_CODE_PAYMENT)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    EventBus.getDefault().post(UserBalanceUpdateEvent())
                    val paidOrderId = data?.getStringExtra(PaymentWebViewActivity.EXTRA_ORDER_ID)
                        ?.takeIf { it.isNotBlank() }
                        ?: orderId
                    if (paidOrderId.isNotBlank()) {
                        orderId = paidOrderId
                    }
                    if (isBuyBlindBox && openImmediately) {
                        isBuyBlindBox = false
                        openImmediately = false
                        sendIntent(ChargeMoneyIntent.BlindBoxOrderResult(orderId))
                    } else {
                        finish()
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // 支付取消或失败
                }
            }
        }
    }
    
    companion object {
        const val REQUEST_CODE_PAYMENT = 1001
    }
} 
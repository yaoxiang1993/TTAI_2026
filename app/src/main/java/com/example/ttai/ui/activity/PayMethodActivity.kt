package com.example.ttai.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ttai.R
import com.example.ttai.ui.activity.ChargeMoneyActivity.Companion.REQUEST_CODE_PAYMENT
import com.example.ttai.ui.activity.PaymentWebViewActivity.Companion.EXTRA_ORDER_ID
import com.example.ttai.ui.activity.PaymentWebViewActivity.Companion.EXTRA_PAY_URL
import com.example.ttai.utils.ToastUtils


class PayMethodActivity : AppCompatActivity() {
    var isALiPay = true

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_method)
        var ivAliPay :ImageView=  findViewById<ImageView>(R.id.ivAliPay)
        var rlyALiPay :RelativeLayout = findViewById(R.id.rlyALiPay)
        rlyALiPay.setOnClickListener {
            isALiPay=!isALiPay
            if (isALiPay){
                ivAliPay.setImageResource(R.mipmap.icon_select_2)
            }else{
                ivAliPay.setImageResource(R.mipmap.icon_normal_2)
            }

        }
        val payUrl = intent.getStringExtra(EXTRA_PAY_URL)
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID)
        findViewById<TextView>(R.id.tvPay).setOnClickListener {
        if (isALiPay){
            val intent = Intent(this, PaymentWebViewActivity::class.java).apply {
                putExtra(PaymentWebViewActivity.EXTRA_PAY_URL, payUrl)
                putExtra(PaymentWebViewActivity.EXTRA_ORDER_ID, orderId)
            }
            startActivityForResult(intent, REQUEST_CODE_PAYMENT)
        }else{
            ToastUtils.showShort(this@PayMethodActivity,"暂时只支持支付宝支付")
        }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PAYMENT) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    // 支付成功，刷新用户余额
                    ToastUtils.showShort(this, "支付成功！")
                    finish()
                }
                Activity.RESULT_CANCELED -> {
                    // 支付取消或失败，不做处理
                }
            }
        }
    }
}
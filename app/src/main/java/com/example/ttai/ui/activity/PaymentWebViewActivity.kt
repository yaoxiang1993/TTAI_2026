package com.example.ttai.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.ttai.R
import com.example.ttai.databinding.ActivityPaymentWebviewBinding
import com.example.ttai.utils.ToastUtils

class PaymentWebViewActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPaymentWebviewBinding
    
    companion object {
        const val EXTRA_PAY_URL = "extra_pay_url"
        const val EXTRA_ORDER_ID = "extra_order_id"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
        loadPaymentUrl()
    }
    
    private fun setupViews() {
        // 设置WebView
        setupWebView()
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                builtInZoomControls = true
                displayZoomControls = false
                setSupportZoom(true)
                // 允许混合内容（HTTP和HTTPS）
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                // 允许文件访问
                allowFileAccess = true
                allowContentAccess = true
                // 设置用户代理
                userAgentString = userAgentString + " TTAIApp/1.0"
            }
            
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString()
                    url?.let { 
                        // 先处理URL跳转逻辑
                        handleUrl(it)
                        // 如果是支付相关的URL，不拦截，让WebView正常加载
                        return false
                    }
                    return super.shouldOverrideUrlLoading(view, request)
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // 页面加载完成，隐藏加载指示器
                    binding.progressBar.visibility = android.view.View.GONE
                }
                
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    // 页面开始加载，显示加载指示器
                    binding.progressBar.visibility = android.view.View.VISIBLE
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    binding.progressBar.progress = newProgress
                    if (newProgress == 100) {
                        binding.progressBar.visibility = android.view.View.GONE
                    } else {
                        binding.progressBar.visibility = android.view.View.VISIBLE
                    }
                }
                
                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    // 可以根据页面标题更新Activity标题
                    title?.let {
                        // 这里可以根据需要设置Activity标题
                        // setTitle(it)
                    }
                }
            }
        }
    }
    
    private fun loadPaymentUrl() {
        val payUrl = intent.getStringExtra(EXTRA_PAY_URL)
        val orderId = intent.getStringExtra(EXTRA_ORDER_ID)
        
        if (payUrl.isNullOrEmpty()) {
            ToastUtils.showShort(this, "支付链接无效")
            finish()
            return
        }
        
        // 显示加载指示器
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        // 加载支付页面
        binding.webView.loadUrl(payUrl)
    }
    
    private fun handleUrl(url: String) {
        // 处理支付相关的URL跳转
        when {
            // 支付宝支付回调
            url.startsWith("alipay://") || url.startsWith("alipays://") -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    ToastUtils.showShort(this, "请安装支付宝客户端")
                }
            }
            // 微信支付回调
            url.startsWith("weixin://") -> {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    ToastUtils.showShort(this, "请安装微信客户端")
                }
            }
            // 支付成功页面 - 支持多种可能的成功标识
            url.contains("payment_success") || url.contains("pay_success") || 
            url.contains("success") || url.contains("paid") || 
            url.contains("complete") -> {
                handlePaymentSuccess()
            }
            // 支付失败页面 - 支持多种可能的失败标识
            url.contains("payment_failed") || url.contains("pay_failed") || 
            url.contains("failed") || url.contains("error") -> {
                handlePaymentFailed()
            }
            // 支付取消页面 - 支持多种可能的取消标识
            url.contains("payment_cancel") || url.contains("pay_cancel") || 
            url.contains("cancel") || url.contains("cancelled") -> {
                handlePaymentCancel()
            }
        }
    }
    
    private fun handlePaymentSuccess() {
        ToastUtils.showShort(this, "支付成功！")
        setResult(RESULT_OK)
        finish()
    }
    
    private fun handlePaymentFailed() {
        ToastUtils.showShort(this, "支付失败，请重试")
        setResult(RESULT_CANCELED)
        finish()
    }
    
    private fun handlePaymentCancel() {
        ToastUtils.showShort(this, "支付已取消")
        setResult(RESULT_CANCELED)
        finish()
    }
    
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            // 如果WebView无法回退，则关闭Activity
            finish()
        }
    }
}

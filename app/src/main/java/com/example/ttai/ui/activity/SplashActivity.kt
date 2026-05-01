package com.example.ttai.ui.activity

import android.content.Intent
import android.text.Spanned
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ttai.R
import com.example.ttai.databinding.ActivitySplashBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.SplashIntent
import com.example.ttai.state.SplashState
import com.example.ttai.utils.CommontUtils
import com.example.ttai.utils.DebugUtils
import com.example.ttai.utils.DeviceUtils
import com.example.ttai.utils.KeyboardManager
import com.example.ttai.utils.MMKVUtils
import com.example.ttai.utils.ToastUtils
import com.example.ttai.utils.LayoutUtils
import com.example.ttai.ui.vm.SplashViewModel
import com.example.ttai.ui.vm.SplashViewModelFactory
import com.example.ttai.utils.Constants

class SplashActivity : BaseMviActivity<SplashIntent, SplashState, SplashViewModel, ActivitySplashBinding>() {
    override val viewModel: SplashViewModel by viewModels { SplashViewModelFactory(this) }
    override val binding by viewBinding { ActivitySplashBinding.inflate(it) }
    
    // 协议同意状态
    private var isAgreementChecked = false
    
    // 登录进度弹框
    private var loginProgressDialog: android.app.AlertDialog? = null
    
    // 登录弹框显示时间控制
    private var loginDialogShowTime: Long = 0
    private val minShowDuration = 1000L // 最小显示时间1秒

    override fun setupViews() {
        // 打印当前环境信息（仅用于调试）
        DebugUtils.logCurrentEnvironment()
        val phone = MMKVUtils.getString(MMKVUtils.PHONE)
        viewModel.deviceId = DeviceUtils.getDeviceId(this)
        val notLogin = intent.getBooleanExtra(Constants.NOT_LOGIN, false)
        if (phone.isEmpty()) {
            sendIntent(SplashIntent.OtherLogin)
        } else if (!notLogin) {
            // 自动登录时也显示进度弹框
            showLoginProgressDialog()
            sendIntent(SplashIntent.Login)
        }
        binding.tvPhone.text = CommontUtils.maskPhoneNumber(phone)
        
        // 设置点击外部区域隐藏键盘
        KeyboardManager.setupHideKeyboardOnTouchOutside(this)
        
        // 设置用户协议和隐私政策文字颜色
        setupAgreementTextColor()
        
        // 设置协议同意ImageView点击事件
        setupAgreementImageView()

        // tvLogin: 一键登录，跳转到SelectTagActivity
        binding.tvLogin.setOnClickListener {
            if (isAgreementChecked) {
                // 显示登录进度弹框
                showLoginProgressDialog()
                sendIntent(SplashIntent.Login)
            } else {
                // 显示提示信息
                ToastUtils.showShort(this, "请先同意用户协议和隐私政策")
            }
        }
        // tvOtherLogin: 其他手机号登录，跳转到LoginActivity
        binding.tvOtherLogin.setOnClickListener {
          sendIntent(SplashIntent.OtherLogin)
        }
    }

    override fun render(state: SplashState) {
        // 处理登录状态
        if (state.isLoading) {
            // 登录中，显示进度弹框
            showLoginProgressDialog()
        } else {
            // 登录完成，隐藏进度弹框
            hideLoginProgressDialog()
        }
        
        // 跳转到登录页
        if (state.navigateToLogin) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        // 跳转到选择标签页（首次使用）
        if (state.navigateToSeletcTag) {
            startActivity(Intent(this, SelectTagActivity::class.java))
            finish()
        }
        // 跳转到主页面（非首次使用）
        if (state.navigateToMain) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }
    
    /**
     * 设置用户协议和隐私政策文字颜色
     * 《用户协议》和《隐私政策》使用白色，其他文字使用灰色
     * 《隐私政策》可点击跳转到PrivacyPolicyActivity
     */
    private fun setupAgreementTextColor() {
        val fullText = "已阅读并同意《用户协议》和《隐私政策》"
        val spannableString = SpannableString(fullText)
        
        // 先设置整个文字为灰色
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_8a8a8a)),
            0,
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 设置《用户协议》为白色
        val userAgreementStart = fullText.indexOf("《用户协议》")
        val userAgreementEnd = userAgreementStart + "《用户协议》".length
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_ffffff)),
            userAgreementStart,
            userAgreementEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // 跳转到隐私政策页面
                    startActivity(Intent(this@SplashActivity, PrivacyPolicyActivity1::class.java))
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    // 保持白色，不显示下划线
                    ds.color = ContextCompat.getColor(this@SplashActivity, R.color.color_ffffff)
                    ds.isUnderlineText = false
                }
            },
            userAgreementStart,
            userAgreementEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 设置《隐私政策》为白色并添加点击事件
        val privacyPolicyStart = fullText.indexOf("《隐私政策》")
        val privacyPolicyEnd = privacyPolicyStart + "《隐私政策》".length
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_ffffff)),
            privacyPolicyStart,
            privacyPolicyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 为《隐私政策》添加点击事件
        spannableString.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // 跳转到隐私政策页面
                    startActivity(Intent(this@SplashActivity, PrivacyPolicyActivity::class.java))
                }
                
                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    // 保持白色，不显示下划线
                    ds.color = ContextCompat.getColor(this@SplashActivity, R.color.color_ffffff)
                    ds.isUnderlineText = false
                }
            },
            privacyPolicyStart,
            privacyPolicyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 应用文字
        binding.tvAgreement.text = spannableString
        
        // 设置TextView为可点击（重要：必须设置才能响应ClickableSpan）
        binding.tvAgreement.movementMethod = android.text.method.LinkMovementMethod.getInstance()
    }
    
    /**
     * 设置用户协议ImageView
     */
    private fun setupAgreementImageView() {
        // 设置ImageView为未选中状态
        isAgreementChecked = false
        binding.ivAgreement.setImageResource(R.mipmap.icon_publis_type_2)
        
        // 设置ImageView点击事件
        binding.ivAgreement.setOnClickListener {
            isAgreementChecked = !isAgreementChecked
            // 根据状态切换图标
            if (isAgreementChecked) {
                binding.ivAgreement.setImageResource(R.mipmap.icon_publis_type_1)
            } else {
                binding.ivAgreement.setImageResource(R.mipmap.icon_publis_type_2)
            }
            android.util.Log.d("SplashActivity", "协议同意状态: $isAgreementChecked")
        }
        
        // 注意：由于《隐私政策》现在有独立的点击事件，我们不再为整个TextView设置点击事件
        // 用户可以通过点击ImageView来切换协议同意状态
        
        // 打印初始状态
        android.util.Log.d("SplashActivity", "初始协议同意状态: $isAgreementChecked")
    }
    
    /**
     * 判断是否应该设置虚拟导航栏适配
     */
    override fun shouldSetupNavigationBarAdapter(): Boolean {
        return true
    }
    
    /**
     * 显示登录进度弹框
     */
    private fun showLoginProgressDialog() {
        if (loginProgressDialog?.isShowing == true) {
            return
        }
        
        val dialogView = layoutInflater.inflate(R.layout.dialog_login_progress, null)
        loginProgressDialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        // 设置对话框样式
        loginProgressDialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            
            // 获取屏幕宽度
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            
            // 设置弹框宽度为屏幕宽度的70%
            val dialogWidth = (screenWidth * 0.7).toInt()
            
            setLayout(
                dialogWidth,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        
        loginProgressDialog?.show()
        
        // 记录弹框显示时间
        loginDialogShowTime = System.currentTimeMillis()
    }
    
    /**
     * 隐藏登录进度弹框
     */
    private fun hideLoginProgressDialog() {
        val currentTime = System.currentTimeMillis()
        val showDuration = currentTime - loginDialogShowTime
        
        if (showDuration < minShowDuration) {
            // 如果显示时间不足1秒，延迟隐藏
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                loginProgressDialog?.let { dialog ->
                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
                loginProgressDialog = null
            }, minShowDuration - showDuration)
        } else {
            // 如果显示时间已经超过1秒，立即隐藏
            loginProgressDialog?.let { dialog ->
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
            }
            loginProgressDialog = null
        }
    }
    
    /**
     * 设置虚拟导航栏高度适配
     */
    override fun setupNavigationBarAdapter() {
        // 为协议同意区域添加虚拟导航栏高度适配
        LayoutUtils.setupViewNavigationBarPadding(this, R.id.llAgreement)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 确保登录进度弹框被关闭（立即关闭，不等待最小显示时间）
        loginProgressDialog?.let { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        loginProgressDialog = null
    }
}
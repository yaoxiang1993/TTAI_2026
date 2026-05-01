package com.example.ttai.ui.activity

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.ttai.R
import com.example.ttai.databinding.ActivityLoginBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.LoginIntent
import com.example.ttai.state.LoginState
import com.example.ttai.utils.CommontUtils
import com.example.ttai.utils.DeviceUtils
import com.example.ttai.utils.KeyboardManager
import com.example.ttai.utils.ToastUtils
import com.example.ttai.ui.vm.LoginViewModel
import com.example.ttai.ui.vm.LoginViewModelFactory
import com.example.ttai.utils.Constants

class LoginActivity  : BaseMviActivity<LoginIntent, LoginState, LoginViewModel, ActivityLoginBinding>() {
    override val viewModel: LoginViewModel by viewModels { LoginViewModelFactory(this) }
    override val binding by viewBinding { ActivityLoginBinding.inflate(it) }
    
    // 协议同意状态
    private var isAgreementChecked = false
    
    // 倒计时相关变量
    private var countDownTime = 60
    private var isCountingDown = false
    private val handler = Handler(Looper.getMainLooper())
    private val countDownRunnable = object : Runnable {
        override fun run() {
            if (countDownTime > 0) {
                binding.tvSendCode.text = "${countDownTime}s后重新发送"
                binding.tvSendCode.isEnabled = false
                binding.tvSendCode.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.color_6b6b6b))
                countDownTime--
                handler.postDelayed(this, 1000)
            } else {
                // 倒计时结束，恢复按钮状态
                binding.tvSendCode.text = "发送验证码"
                binding.tvSendCode.isEnabled = true
                binding.tvSendCode.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.color_6f6e70))
                isCountingDown = false
                countDownTime = 60
            }
        }
    }

    override fun setupViews() {
        viewModel.deviceId = DeviceUtils.getDeviceId(this)
        
        // 设置点击外部区域隐藏键盘
        KeyboardManager.setupHideKeyboardOnTouchOutside(this, binding.root, binding.edtPhone, binding.edtCode)
        
        // 设置用户协议和隐私政策文字颜色
        setupAgreementTextColor()
        
        // 设置协议同意ImageView点击事件
        setupAgreementImageView()
        
        binding.tvLogin.setOnClickListener {
            val phoneNumber = binding.edtPhone.text.toString()
            
            // 校验电话号码格式
            if (!CommontUtils.isValidPhoneNumber(phoneNumber)) {
                ToastUtils.showShort(this, "请输入正确的手机号码")
                return@setOnClickListener
            }
            
            if (isAgreementChecked) {
                val smsCode = binding.edtCode.text.toString()
                val invitationCode = binding.edtInvitationCode.text.toString()

                sendIntent(LoginIntent.VerifyCodeAuth(phoneNumber, smsCode,invitationCode))
            } else {
                // 显示提示信息
                ToastUtils.showShort(this, "请先同意用户协议和隐私政策")
            }
        }

        binding.tvSendCode.setOnClickListener {
            if (!isCountingDown) {
                val phoneNumber = binding.edtPhone.text.toString()
                
                // 校验电话号码格式
                if (!CommontUtils.isValidPhoneNumber(phoneNumber)) {
                    ToastUtils.showShort(this, "请输入正确的手机号码")
                    return@setOnClickListener
                }
                
                // 点击按钮时立即开始倒计时
                startCountDown()
                
                sendIntent(LoginIntent.SendCode(phoneNumber))
            }
        }
    }

    override fun render(state: LoginState) {
        super.render(state)
        
        // 处理发送验证码成功
        if (state.sendCodeSuccess) {
            ToastUtils.showShort(this, "验证码发送成功")
            // 重置成功状态
            viewModel.resetSendCodeSuccess()
        }

        if (state.navigateToMain) {

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constants.SHOW_DIALOG,true)
            startActivity(intent)
            finish()
            viewModel.state.value.copy(navigateToMain = false)
        }
        if (state.navigateToTag) {
            navigateTo(SelectTagActivity::class.java, finishCurrent = true)
            viewModel.state.value.copy(navigateToTag = false)
        }
    }
    
    /**
     * 启动倒计时
     */
    private fun startCountDown() {
        if (!isCountingDown) {
            isCountingDown = true
            countDownTime = 60
            handler.post(countDownRunnable)
        }
    }
    
    /**
     * 停止倒计时
     */
    private fun stopCountDown() {
        if (isCountingDown) {
            handler.removeCallbacks(countDownRunnable)
            isCountingDown = false
            countDownTime = 60
            binding.tvSendCode.text = "发送验证码"
            binding.tvSendCode.isEnabled = true
            binding.tvSendCode.setTextColor(ContextCompat.getColor(this, R.color.color_6f6e70))
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 页面销毁时停止倒计时，避免内存泄漏
        stopCountDown()
    }
    
    /**
     * 设置用户协议和隐私政策文字颜色
     * 《用户协议》和《隐私政策》使用白色，其他文字使用灰色
     */
    private fun setupAgreementTextColor() {
        val fullText = "已阅读并同意《用户协议》和《隐私政策》"
        val spannableString = android.text.SpannableString(fullText)
        
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
                    startActivity(Intent(this@LoginActivity, PrivacyPolicyActivity1::class.java))
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    // 保持白色，不显示下划线
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.color_ffffff)
                    ds.isUnderlineText = false
                }
            },
            userAgreementStart,
            userAgreementEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        
        // 设置《隐私政策》为白色
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
                    startActivity(Intent(this@LoginActivity, PrivacyPolicyActivity::class.java))
                }

                override fun updateDrawState(ds: android.text.TextPaint) {
                    super.updateDrawState(ds)
                    // 保持白色，不显示下划线
                    ds.color = ContextCompat.getColor(this@LoginActivity, R.color.color_ffffff)
                    ds.isUnderlineText = false
                }
            },
            privacyPolicyStart,
            privacyPolicyEnd,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 应用文字
        binding.tvAgreement.text = spannableString
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
            android.util.Log.d("LoginActivity", "协议同意状态: $isAgreementChecked")
        }
        
        // 设置文字点击事件，点击文字也可以切换状态
        binding.tvAgreement.setOnClickListener {
            isAgreementChecked = !isAgreementChecked
            // 根据状态切换图标
            if (isAgreementChecked) {
                binding.ivAgreement.setImageResource(R.mipmap.icon_publis_type_1)
            } else {
                binding.ivAgreement.setImageResource(R.mipmap.icon_publis_type_2)
            }
            android.util.Log.d("LoginActivity", "协议同意状态: $isAgreementChecked")
        }
    }
}
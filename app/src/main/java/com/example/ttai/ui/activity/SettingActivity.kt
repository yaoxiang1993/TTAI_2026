package com.example.ttai.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.example.ttai.databinding.ActivitySettingBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.ui.dialog.TwoButtonDialogFragment
import com.example.ttai.intent.SettingIntent
import com.example.ttai.network.NetworkModule
import com.example.ttai.state.SettingState

import com.example.ttai.ui.vm.SettingViewModel
import com.example.ttai.utils.Constants

class SettingActivity : BaseMviActivity<SettingIntent, SettingState, SettingViewModel, ActivitySettingBinding>() {
    override val viewModel: SettingViewModel by viewModels()
    override val binding by viewBinding { ActivitySettingBinding.inflate(it) }

    override fun setupViews() {
        // 返回按钮已在BaseMviActivity中自动处理
        
        binding.tvCloseAnAccount.setOnClickListener {
            showDialog()
        }

        binding.tvLogout.setOnClickListener {
            NetworkModule.clearAuthToken()

            val intent = Intent(this@SettingActivity, SplashActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(Constants.NOT_LOGIN,true)
            }
            startActivity(intent)
            finish()
        }
        sendIntent(SettingIntent.LoadData)
    }

    override fun render(state: SettingState) {
        super.render(state)
//        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
//        binding.tvContent.text = state.content
//        if (state.error != null) {
//            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
//        }
    }

    private fun showDialog() {
        val dialog = TwoButtonDialogFragment.newInstance(
            message = "注销该账号\n" +
                    "是否继续呢？\n",
            positiveText = "确认",
            negativeText = "取消"
        ).setOnButtonClickListener(object : TwoButtonDialogFragment.OnButtonClickListener {
            override fun onPositiveClick() {
                // 处理确认逻辑
                val intent = Intent(this@SettingActivity, SplashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(Constants.NOT_LOGIN,true)
                }
                startActivity(intent)
                finish()
            }

            override fun onNegativeClick() {
                // 处理取消逻辑
            }
        })
// 显示对话框
        dialog.show(supportFragmentManager, "TwoButtonDialog")
    }

}
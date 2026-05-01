package com.example.ttai.ui.activity

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import com.example.ttai.utils.ToastUtils
import androidx.activity.viewModels
import com.example.ttai.databinding.ActivityEditeNameBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.databinding.ActivityEditeBriefBinding
import com.example.ttai.intent.EditeBriefIntent
import com.example.ttai.intent.EditeNameIntent
import com.example.ttai.state.EditeBriefState
import com.example.ttai.state.EditeNameState
import com.example.ttai.ui.vm.EditeBriefViewModel
import com.example.ttai.ui.vm.EditeBriefViewModelFactory
import com.example.ttai.ui.vm.EditeNameViewModel
import com.example.ttai.ui.vm.EditeNameViewModelFactory

class EditeBriefActivity : BaseMviActivity<EditeBriefIntent, EditeBriefState, EditeBriefViewModel, ActivityEditeBriefBinding>() {
    
    override val viewModel: EditeBriefViewModel by viewModels { EditeBriefViewModelFactory(this) }
    override val binding by viewBinding { ActivityEditeBriefBinding.inflate(it) }

    override fun setupViews() {
        // 从Intent中获取当前用户名
        val currentName = intent.getStringExtra("current_name") ?: ""
        viewModel.setCurrentBrief(currentName)
        
        // 设置返回按钮
        binding.ivBack.setOnClickListener {
            sendIntent(EditeBriefIntent.Back)
        }
        
        // 设置保存按钮
        binding.tvSave.setOnClickListener {
            sendIntent(EditeBriefIntent.SaveName)
        }
        
        // 设置输入框监听
        setupTextWatcher()
        
        // 加载当前数据
        sendIntent(EditeBriefIntent.LoadData)
    }
    
    override fun render(state: EditeBriefState) {
        super.render(state)
        
        // 设置输入框的文本
        if (state.brief != binding.edtBrief.text.toString()) {
            binding.edtBrief.setText(state.brief)
        }
        
        // 更新字符计数
        binding.tvNameLength.text = "${state.briefLength}/60"
        
        // 更新保存按钮状态
        binding.tvSave.isEnabled = state.isBriefValid && !state.isSaving
        binding.tvSave.text = if (state.isSaving) "保存中..." else "保存"
        
        // 处理保存成功
        if (state.saveSuccess) {
            ToastUtils.showShort(this, "简介保存成功")
            // 返回新的用户名
            val resultIntent = Intent().apply {
                putExtra("new_name", state.brief)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        
        // 处理错误
        state.errorMessage?.let { error ->
            ToastUtils.showShort(this, error)
        }
    }
    
    private fun setupTextWatcher() {
        binding.edtBrief.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendIntent(EditeBriefIntent.UpdateName(s?.toString() ?: ""))
            }
        })
    }

}
package com.example.ttai.ui.activity

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import com.example.ttai.utils.ToastUtils
import androidx.activity.viewModels
import com.example.ttai.databinding.ActivityEditeNameBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.Message
import com.example.ttai.databinding.ActivityEditeMessageBinding
import com.example.ttai.intent.EditeMessageIntent
import com.example.ttai.state.EditeMessageState
import com.example.ttai.ui.vm.EditeMessageViewModel
import com.example.ttai.ui.vm.EditeMessageViewModelFactory
import com.example.ttai.utils.Constants

class EditeMessageActivity : BaseMviActivity<EditeMessageIntent, EditeMessageState, EditeMessageViewModel, ActivityEditeMessageBinding>() {
    
    override val viewModel: EditeMessageViewModel by viewModels { EditeMessageViewModelFactory(this) }
    override val binding by viewBinding { ActivityEditeMessageBinding.inflate(it) }
    var message : Message? = null

    override fun setupViews() {
        message = intent.getParcelableExtra(Constants.OBJECT_KEY)
        viewModel.setCurrentUserName(message?.content)
        
        // 设置返回按钮
        binding.ivBack.setOnClickListener {
            sendIntent(EditeMessageIntent.Back)
        }
        
        // 设置保存按钮
        binding.tvSave.setOnClickListener {
            message?.content = binding.edtName.text.trim().toString()
            sendIntent(EditeMessageIntent.SaveName(message))
        }
        
        // 设置输入框监听
        setupTextWatcher()
        
        // 加载当前数据
        sendIntent(EditeMessageIntent.LoadData)
    }
    
    override fun render(state: EditeMessageState) {
        super.render(state)
        
        // 设置输入框的文本
        if (state.currentName != binding.edtName.text.toString()) {
            binding.edtName.setText(state.currentName)
        }

        binding.tvSave.text = if (state.isSaving) "保存中..." else "保存"
        
        // 处理保存成功
        if (state.saveSuccess) {
            ToastUtils.showShort(this, "消息保存成功")
            // 返回新的用户名
            val resultIntent = Intent().apply {
                putExtra("new_name", state.currentName)
                putExtra(Constants.STRING_ID_KEY, message?.id)
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
        binding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendIntent(EditeMessageIntent.UpdateName(s?.toString() ?: ""))
            }
        })
    }

}
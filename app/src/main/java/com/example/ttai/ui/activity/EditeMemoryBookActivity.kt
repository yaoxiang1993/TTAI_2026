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
import com.example.ttai.databinding.ActivityEditeMemoryBookBinding
import com.example.ttai.intent.EditeBriefIntent
import com.example.ttai.intent.EditeMemoryBookIntent
import com.example.ttai.intent.EditeNameIntent
import com.example.ttai.state.EditeBriefState
import com.example.ttai.state.EditeMemoryBookState
import com.example.ttai.state.EditeNameState
import com.example.ttai.ui.vm.EditeBriefViewModel
import com.example.ttai.ui.vm.EditeBriefViewModelFactory
import com.example.ttai.ui.vm.EditeMemoryBookViewModel
import com.example.ttai.ui.vm.EditeMemoryBookViewModelFactory
import com.example.ttai.ui.vm.EditeNameViewModel
import com.example.ttai.ui.vm.EditeNameViewModelFactory
import com.example.ttai.utils.Constants

class EditeMemoryBookActivity : BaseMviActivity<EditeMemoryBookIntent, EditeMemoryBookState, EditeMemoryBookViewModel, ActivityEditeMemoryBookBinding>() {
    
    override val viewModel: EditeMemoryBookViewModel by viewModels {
        EditeMemoryBookViewModelFactory(
            this
        )
    }
    override val binding by viewBinding { ActivityEditeMemoryBookBinding.inflate(it) }

    override fun setupViews() {
        // 从Intent中获取当前用户名
        val character_id = intent.getStringExtra(Constants.CHARACTER_ID_KEY) ?: ""
        // 设置返回按钮
        binding.ivBack.setOnClickListener {
            sendIntent(EditeMemoryBookIntent.Back)
        }
        
        // 设置保存按钮
        binding.tvSave.setOnClickListener {
            sendIntent(EditeMemoryBookIntent.SaveMemoryBook)
        }
        
        // 设置输入框监听
        setupTextWatcher()
        
        // 加载当前数据
        sendIntent(EditeMemoryBookIntent.LoadData(character_id))
    }
    
    override fun render(state: EditeMemoryBookState) {
        super.render(state)
        
        // 设置输入框的文本
        if (state.content != binding.edtBrief.text.toString()) {
            binding.edtBrief.setText(state.content)
        }
        
        // 更新字符计数
        binding.tvNameLength.text = "${state.contentLength}/2000"
        
        // 更新保存按钮状态
        binding.tvSave.isEnabled = state.isContentValid && !state.isSaving
        binding.tvSave.text = if (state.isSaving) "保存中..." else "保存"
        
        // 处理保存成功
        if (state.saveSuccess) {
            ToastUtils.showShort(this, "保存成功")
            // 返回新的用户名
            val resultIntent = Intent().apply {
                putExtra("new_name", state.content)
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
                sendIntent(EditeMemoryBookIntent.UpdateMemoryBook(s?.toString() ?: ""))
            }
        })
    }

}
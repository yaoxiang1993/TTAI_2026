package com.example.ttai.ui.activity

import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ttai.MyBluetoothManager
import com.example.ttai.R
import com.example.ttai.databinding.ActivityEditBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.databinding.ActivityMyChatSettingBinding
import com.example.ttai.intent.AISettingIntent
import com.example.ttai.intent.CreateAIIntent
import com.example.ttai.intent.EditIntent
import com.example.ttai.intent.MyChatSettingIntent
import com.example.ttai.state.EditState
import com.example.ttai.state.MyChatSettingState
import com.example.ttai.ui.vm.EditViewModel
import com.example.ttai.ui.vm.MyChatSettingViewModel
import com.example.ttai.ui.vm.MyChatSettingViewModelFactory
import com.example.ttai.utils.Constants
import com.example.ttai.utils.ImagePickerUtil
import com.example.ttai.utils.ToastUtils

class MyChatSettingActivity : BaseMviActivity<MyChatSettingIntent, MyChatSettingState, MyChatSettingViewModel, ActivityMyChatSettingBinding>() {
    override val viewModel: MyChatSettingViewModel by viewModels { MyChatSettingViewModelFactory(this) }
    override val binding by viewBinding { ActivityMyChatSettingBinding.inflate(it) }

    var gender :String=""
    var genderCN :String=""
    var identity :String=""
    override fun setupViews() {
        var characterId = intent.getStringExtra(Constants.CHARACTER_ID_KEY)
        sendIntent(MyChatSettingIntent.Initialize(characterId))
        // 为ivPhone添加点击事件，使用工具类处理图片选择
        binding.tvSave.setOnClickListener {
           // imagePickerUtil.showImageSelectionDialog()
            sendIntent(MyChatSettingIntent.SaveChatSetting(
                characterId,
                binding.edtName.text.toString(),
                identity,
                genderCN,
                binding.edtSetting.text.toString()
                ))
        }
        binding.tvMale.setOnClickListener {
            genderCN = "男"
            sendIntent(MyChatSettingIntent.SelectGender(Constants.MALE_CN))
        }

        binding.tvFemale.setOnClickListener {
            genderCN = "女"
            sendIntent(MyChatSettingIntent.SelectGender(Constants.FEMALE_CN))
        }

        binding.tvOther.setOnClickListener {
            genderCN = "其他"
            sendIntent(MyChatSettingIntent.SelectGender(Constants.OTHER_CN))
        }

        binding.edtName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tvNameLength.text = "${s.toString().length}/10"
            }
        })
        binding.edtSetting.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.tvSettingLength.text = "${s.toString().length}/2000"
            }
        })
    }

    override fun render(state: MyChatSettingState) {
        super.render(state)

        // 更新性别选择状态
        updateGenderSelection(state.gender)
        genderCN
        identity = state.identity.toString()
        binding.edtName.setText( state.nickname)
        binding.edtSetting.setText( state.personality_description)
        if (state.saveSuccess==true){
            ToastUtils.showShort(this,"保存成功")
            finish()
        }
    }
    private fun updateGenderSelection(gender: String?) {
        // 重置所有按钮为未选中状态
        binding.tvMale.apply {
            background = getDrawable(R.drawable.bg_stroke_545454_r50)
            setTextColor(getColor(R.color.color_545454))
        }
        binding.tvFemale.apply {
            background = getDrawable(R.drawable.bg_stroke_545454_r50)
            setTextColor(getColor(R.color.color_545454))
        }
        binding.tvOther.apply {
            background = getDrawable(R.drawable.bg_stroke_545454_r50)
            setTextColor(getColor(R.color.color_545454))
        }

        // 设置选中按钮的样式
        when (gender) {
            Constants.MALE_CN -> binding.tvMale.apply {
                genderCN = "男"
                background = getDrawable(R.drawable.bg_stroke_primary_r50)
                setTextColor(getColor(R.color.primary_color))
            }
            Constants.FEMALE_CN -> binding.tvFemale.apply {
                genderCN = "女"
                background = getDrawable(R.drawable.bg_stroke_primary_r50)
                setTextColor(getColor(R.color.primary_color))
            }
            Constants.OTHER_CN -> binding.tvOther.apply {
                genderCN = "其他"
                background = getDrawable(R.drawable.bg_stroke_primary_r50)
                setTextColor(getColor(R.color.primary_color))
            }
        }
    }

}
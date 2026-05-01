package com.example.ttai.ui.activity

import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ttai.MyBluetoothManager
import com.example.ttai.R
import com.example.ttai.databinding.ActivityEditBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.databinding.ActivityMyChatSettingBinding
import com.example.ttai.intent.EditIntent
import com.example.ttai.intent.MyChatSettingIntent
import com.example.ttai.intent.SwitchModeIntent
import com.example.ttai.state.EditState
import com.example.ttai.state.MyChatSettingState
import com.example.ttai.state.SwitchModeState
import com.example.ttai.ui.vm.EditViewModel
import com.example.ttai.ui.vm.MyChatSettingViewModel
import com.example.ttai.ui.vm.SwitchModeViewModel
import com.example.ttai.utils.ImagePickerUtil
import com.example.ttai.utils.ToastUtils

class SwitchModeActivity : BaseMviActivity<SwitchModeIntent, SwitchModeState, SwitchModeViewModel, ActivityMyChatSettingBinding>() {
    override val viewModel: SwitchModeViewModel by viewModels()
    override val binding by viewBinding { ActivityMyChatSettingBinding.inflate(it) }


    override fun setupViews() {
        // 为ivPhone添加点击事件，使用工具类处理图片选择
        binding.tvSave.setOnClickListener {
           // imagePickerUtil.showImageSelectionDialog()
        }
    }

}
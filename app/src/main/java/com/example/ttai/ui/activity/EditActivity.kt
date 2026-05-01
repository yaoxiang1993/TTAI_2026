package com.example.ttai.ui.activity

import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ttai.MyBluetoothManager
import com.example.ttai.R
import com.example.ttai.databinding.ActivityEditBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.EditIntent
import com.example.ttai.state.EditState
import com.example.ttai.ui.vm.EditViewModel
import com.example.ttai.utils.ImagePickerUtil
import com.example.ttai.utils.ToastUtils

class EditActivity : BaseMviActivity<EditIntent, EditState, EditViewModel, ActivityEditBinding>() {
    override val viewModel: EditViewModel by viewModels()
    override val binding by viewBinding { ActivityEditBinding.inflate(it) }
    
    // 图片选择工具类
    private val imagePickerUtil = ImagePickerUtil(this) { bitmap ->
        // 图片选择回调，将选择的图片设置到头像控件
        binding.ivHeard.setImageBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
        MyBluetoothManager.connectedDevice?.let {
            binding.tvName.text = it?.name
        }

    }

    override fun setupViews() {
        // 为ivPhone添加点击事件，使用工具类处理图片选择
        binding.ivPhone.setOnClickListener {
           // imagePickerUtil.showImageSelectionDialog()
        }
        binding.tvDelete.setOnClickListener {
            MyBluetoothManager.cleanup()
            ToastUtils.showShort(this,"玩具删除成功")
            finish()
        }
        Glide.with(this)
            .load(R.mipmap.icon_device_defulte)
            .circleCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.ivHeard)
        sendIntent(EditIntent.LoadData)
    }

    override fun render(state: EditState) {
        super.render(state)
//        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
//        binding.tvContent.text = state.content
    }
}
package com.example.ttai.ui.activity

import androidx.activity.viewModels
import com.example.ttai.databinding.ActivityAcquisitionModeBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.AcquisitionModeIntent
import com.example.ttai.state.AcquisitionModeState
import com.example.ttai.ui.vm.AcquisitionModeViewModel

/**
 * 采集模式界面，展示采集相关内容并支持用户操作
 */
class AcquisitionModeActivity : BaseMviActivity<AcquisitionModeIntent, AcquisitionModeState, AcquisitionModeViewModel, ActivityAcquisitionModeBinding>() {
    override val viewModel: AcquisitionModeViewModel by viewModels()
    override val binding by viewBinding { ActivityAcquisitionModeBinding.inflate(it) }

    override fun setupViews() {
//        binding.btnAction.setOnClickListener {
//            sendIntent(AcquisitionModeIntent.PerformAction("执行采集"))
//        }
        sendIntent(AcquisitionModeIntent.LoadData)
    }

    override fun render(state: AcquisitionModeState) {
        super.render(state)
//        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
//        binding.tvContent.text = state.content
//        if (state.error != null) {
//            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
//        }
    }

}
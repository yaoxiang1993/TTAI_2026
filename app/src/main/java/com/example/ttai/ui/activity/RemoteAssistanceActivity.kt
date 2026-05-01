package com.example.ttai.ui.activity

import androidx.activity.viewModels
import com.example.ttai.databinding.ActivityRemoteAssistanceBinding
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.RemoteAssistanceIntent
import com.example.ttai.state.RemoteAssistanceState

import com.example.ttai.ui.vm.RemoteAssistanceViewModel

class RemoteAssistanceActivity : BaseMviActivity<RemoteAssistanceIntent, RemoteAssistanceState, RemoteAssistanceViewModel, ActivityRemoteAssistanceBinding>() {
    override val viewModel: RemoteAssistanceViewModel by viewModels()
    override val binding by viewBinding { ActivityRemoteAssistanceBinding.inflate(it) }

    override fun setupViews() {

        
        sendIntent(RemoteAssistanceIntent.LoadData)
    }

    override fun render(state: RemoteAssistanceState) {
        super.render(state)
//        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
//        binding.tvContent.text = state.content
    }
}
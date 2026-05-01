package com.example.ttai.ui.activity

import CollectViewModelFactory
import RequestModelViewModelFactory
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.MyBluetoothManager
import com.example.ttai.databinding.ActivityRequestModelBinding
import com.example.ttai.adapter.CollectAdapter
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.intent.CollectIntent
import com.example.ttai.intent.RequestModelIntent
import com.example.ttai.state.RequestModelState
import com.example.ttai.utils.Constants
import com.example.ttai.ui.vm.RequestModelViewModel
import com.example.ttai.utils.CommontUtils
import com.google.android.material.snackbar.Snackbar

class RequestModelActivity : BaseMviActivity<RequestModelIntent, RequestModelState, RequestModelViewModel, ActivityRequestModelBinding>() {
    override val viewModel: RequestModelViewModel by viewModels {
        RequestModelViewModelFactory(this)
    }
    override val binding by viewBinding { ActivityRequestModelBinding.inflate(it) }
    private val collectAdapter = CollectAdapter(
        onPlayClick = { mode -> MyBluetoothManager.writeCharacteristic(this,
            CommontUtils.convertHexToBytes(mode?.bluetoothModeId))},
        onCollectClick = { mode ->
            sendIntent(RequestModelIntent.SetFavoriteState(mode))
        }
    )

    override fun setupViews() {
        // 返回按钮已在BaseMviActivity中自动处理
        
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectAdapter
        }
//        binding.tvContent.text = "请求模式 - Character ID: ${characterId ?: Constants.DEFAULT_CHARACTER_ID}"
        sendIntent(RequestModelIntent.Initialize)
    }

    override fun render(state: RequestModelState) {
        super.render(state)
//        binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        collectAdapter.submitList(state.modes)
        if (state.error != null) {
            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
        }
    }
}
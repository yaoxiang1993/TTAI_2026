package com.example.ttai.ui.activity

import CollectViewModelFactory
import android.content.Intent
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.MyBluetoothManager
import com.example.ttai.databinding.ActivityCollectBinding
import com.example.ttai.adapter.CollectAdapter
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.CollectItem
import com.example.ttai.intent.CollectIntent
import com.example.ttai.intent.ControlFragmentIntent
import com.example.ttai.state.CollectState
import com.example.ttai.ui.vm.CollectViewModel
import com.example.ttai.network.NetworkModule
import com.example.ttai.utils.CommontUtils

class CollectActivity : BaseMviActivity<CollectIntent, CollectState, CollectViewModel, ActivityCollectBinding>() {
    override val viewModel: CollectViewModel by viewModels { 
        CollectViewModelFactory(this)
    }
    override val binding by viewBinding { ActivityCollectBinding.inflate(it) }
    private val collectAdapter = CollectAdapter(
        onPlayClick = { mode -> MyBluetoothManager.writeCharacteristic(this,
            CommontUtils.convertHexToBytes(mode?.bluetoothModeId))},
        onCollectClick = { mode ->
            sendIntent(CollectIntent.SetFavoriteState(mode))
        }
    )

    override fun setupViews() {

        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectAdapter
        }
        sendIntent(CollectIntent.Initialize)

        binding.tvRequestModel.setOnClickListener {
            val intent = Intent(this, RequestModelActivity::class.java)
            startActivity(intent)
        }

    }

    override fun render(state: CollectState) {
        super.render(state)

        var list = state.modes.filter { it.isFavorited } as List<CollectItem?>?
        if (list.isNullOrEmpty()){
            binding.mRecyclerView.isVisible = false
            binding.gpDataIsEmpty.isVisible = true
        }else{
            binding.mRecyclerView.isVisible = true
            binding.gpDataIsEmpty.isVisible = false
        }
        // 更新适配器数据
        collectAdapter.submitList(list)
        
        // 显示错误信息
        if (state.error != null) {
            com.google.android.material.snackbar.Snackbar.make(binding.root, state.error, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
    }
}
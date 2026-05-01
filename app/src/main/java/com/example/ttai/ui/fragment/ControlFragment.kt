package com.example.ttai.ui.fragment

import android.content.Intent
import android.util.Log
import android.widget.SeekBar
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.MyBluetoothManager
import com.example.ttai.adapter.CollectAdapter
import com.example.ttai.databinding.FragmentControlBinding
import com.example.ttai.manager.BluetoothDeviceManager
import com.example.ttai.manager.BluetoothState
import com.example.ttai.ui.activity.CollectActivity
import com.example.ttai.ui.activity.EditActivity
import com.example.ttai.ui.activity.RemoteAssistanceActivity
import com.example.ttai.base.BaseMviFragment
import com.example.ttai.base.viewBinding
import com.example.ttai.bean.CollectItem
import com.example.ttai.event.AICreatedEvent
import com.example.ttai.event.CloseBLEEvent
import com.example.ttai.intent.ChatIntent
import com.example.ttai.intent.ControlFragmentIntent
import com.example.ttai.state.ControlFragmentState
import com.example.ttai.ui.activity.BlueTestActivity
import com.example.ttai.ui.view.TextThumbDrawable
import com.example.ttai.ui.vm.ControlFragmentViewModel
import com.example.ttai.ui.vm.ControlFragmentViewModelFactory
import com.example.ttai.utils.CommontUtils
import org.greenrobot.eventbus.EventBus


class ControlFragment : BaseMviFragment<ControlFragmentIntent, ControlFragmentState, ControlFragmentViewModel, FragmentControlBinding>() {
    override val viewModel: ControlFragmentViewModel by viewModels { ControlFragmentViewModelFactory(requireContext()) }
    override val binding by viewBinding { inflater, container, attachToParent ->
        FragmentControlBinding.inflate(inflater, container, attachToParent)
    }

    private val collectAdapter = CollectAdapter(
        onPlayClick = { mode ->
            MyBluetoothManager.writeCharacteristic(requireContext(),CommontUtils.convertHexToBytes(mode?.bluetoothModeId))}
                      ,
        onCollectClick = { mode ->
            sendIntent(ControlFragmentIntent.SetFavoriteState(mode))
        }
    )

    var progressSlider1 :Int = 0

    var progressSlider2 :Int = 0
    override fun setupViews() {
        try {
            EventBus.getDefault().register(this)
            android.util.Log.d("ControlFragment", "EventBus注册成功")
        } catch (e: Exception) {
            android.util.Log.w("ControlFragment", "EventBus注册失败: ${e.message}")
        }
        // 添加设备：跳转到 SearchBluetoothActivity
        binding.llyAddArticles.setOnClickListener {
            val intent = Intent(requireContext(), BlueTestActivity::class.java)
            startActivity(intent)
        }

        // 编辑设备：跳转到 EditActivity
        binding.clyEdit.setOnClickListener {
                val intent = Intent(requireContext(), EditActivity::class.java).apply {
                    putExtra("deviceName", viewModel.state.value.deviceName)
                }
               startActivity(intent)

        }

        // 收藏模式：跳转到 CollectActivity
        binding.tvCollect.setOnClickListener {
            val intent = Intent(requireContext(), CollectActivity::class.java).apply {
            }
            startActivity(intent)
        }

        // 远程互助：跳转到 RemoteAssistanceActivity
        binding.tvRemoteControl.setOnClickListener {
            val intent = Intent(requireContext(), RemoteAssistanceActivity::class.java).apply {
            }
            startActivity(intent)
        }

        // AI 控制：跳转到 ChatActivity
        binding.tvAIControl.setOnClickListener {
            Log.d("ControlFragment", "tvAIControl clicked, try send commands")
            BluetoothDeviceManager.get().requestDeviceInfo()
            BluetoothDeviceManager.get().requestBattery()
            BluetoothDeviceManager.get().requestRunningStatus()
        }

        // 切换设备状态
        binding.tvSelected.setOnClickListener {
            MyBluetoothManager.cleanup()
            binding.clyEdit.isVisible = false
        }
        binding.progressSlider1.thumb = TextThumbDrawable("暂停")
        binding.progressSlider2.thumb = TextThumbDrawable("暂停")
        // 震动强度调整
        binding.progressSlider1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.progressSlider1.thumb = TextThumbDrawable(if (progress==0){"暂停"}else{progress.toString()})
                    Log.d("ControlFragment", "progressSlider1 progress: $progress")
                    // Example: Send progress value to Bluetooth device
                    progressSlider1 = progress
                    MyBluetoothManager.writeCharacteristic(requireContext(),byteArrayOf(0x02, 0x01, progress.toByte(), progressSlider2.toByte()))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Optional: Handle touch start
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Optional: Handle touch stop
            }
        })   // 吮吸强度调整
        binding.progressSlider2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    Log.d("ControlFragment", "progressSlider2 progress: $progress")
                    binding.progressSlider2.thumb = TextThumbDrawable(if (progress==0){"暂停"}else{progress.toString()})
                    progressSlider2 = progress
                    MyBluetoothManager.writeCharacteristic(requireContext(),byteArrayOf(0x02, 0x01, progressSlider1.toByte(), progress.toByte()))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Optional: Handle touch start
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Optional: Handle touch stop
            }
        })
        sendIntent(ControlFragmentIntent.Initialize)
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectAdapter
        }
    }

    override fun render(state: ControlFragmentState) {
        super.render(state)
        collectAdapter.submitList(state.modes)

    }

    override fun onResume() {
        super.onResume()
        if (MyBluetoothManager.connectedDevice == null){
            binding.clyEdit.isVisible = false
        }else{
            binding.tvName.text = MyBluetoothManager.connectedDevice?.name
            binding.clyEdit.isVisible = true
        }
    }

    private var lastConnectedAddress: String? = null
    private val btObserver: (BluetoothState) -> Unit = { s ->
        Log.d(
            "ControlFragment",
            "BT state: isScanning=${s.isScanning}, connected=${s.connectedDevice?.address}, status=${s.connectionStatus}, error=${s.error}, notify=${s.notificationData?.take(60)}"
        )
        s.connectedDevice?.let { dev ->
            if (dev.address != lastConnectedAddress) {
                lastConnectedAddress = dev.address
                Log.d("ControlFragment", "Connected to ${dev.address}, auto request info/battery/status")
                BluetoothDeviceManager.get().requestDeviceInfo()
                BluetoothDeviceManager.get().requestBattery()
                BluetoothDeviceManager.get().requestRunningStatus()
            }
        }
    }

    /**
     * 处理AI创建成功事件
     */
    @org.greenrobot.eventbus.Subscribe(threadMode = org.greenrobot.eventbus.ThreadMode.MAIN)
    fun onCloseBLEEvent(event: CloseBLEEvent) {
        binding.clyEdit.isVisible = false
    }

    override fun onStart() {
        super.onStart()
        BluetoothDeviceManager.get().addObserver(btObserver)
    }

    override fun onStop() {
        super.onStop()
        BluetoothDeviceManager.get().removeObserver(btObserver)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // 注销EventBus
        try {
            EventBus.getDefault().unregister(this)
            android.util.Log.d("ControlFragment", "EventBus注销成功")
        } catch (e: Exception) {
            android.util.Log.w("ControlFragment", "EventBus注销失败: ${e.message}")
        }
    }
}
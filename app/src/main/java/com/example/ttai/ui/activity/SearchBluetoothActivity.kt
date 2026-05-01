package com.example.ttai.ui.activity

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.adapter.BluetoothDeviceAdapter
import com.example.ttai.manager.BluetoothDeviceManager
import com.example.ttai.manager.BluetoothState
import com.example.ttai.base.BaseMviActivity
import com.example.ttai.base.viewBinding
import com.example.ttai.databinding.ActivitySearchBluetoothBinding
import com.example.ttai.intent.SearchBluetoothIntent
import com.example.ttai.manager.BOND_BONDED
import com.example.ttai.manager.CONNECTED
import com.example.ttai.state.SearchBluetoothState
import com.example.ttai.ui.vm.SearchBluetoothViewModel
import com.example.ttai.utils.JsonUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/*class SearchBluetoothActivity : BaseMviActivity<SearchBluetoothIntent, SearchBluetoothState, SearchBluetoothViewModel, ActivitySearchBluetoothBinding>() {
    override val viewModel: SearchBluetoothViewModel by viewModels()
    override val binding by viewBinding { ActivitySearchBluetoothBinding.inflate(it) }
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val deviceAdapter = BluetoothDeviceAdapter(
        onDeviceClick = { selectedDevice ->
            val resultIntent = Intent().apply {
                putExtra("selected_device", selectedDevice)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        },
        onToggleClick = { device ->
            Log.d("SearchBluetoothActivity", "onToggleClick connectedAddresses $connectedAddresses")
            if (connectedAddresses.contains(device.address)) {
                disconnectFromDevice(device)
            } else {
                connectToDevice(device)
            }
        }
    )
    private val connectedAddresses = mutableSetOf<String>()
    private val pairingAttempted = mutableSetOf<String>()
    private var pairingRetryCount = 0

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startBluetoothDiscovery()
        } else {
            Snackbar.make(binding.root, "蓝牙权限被拒绝", Snackbar.LENGTH_SHORT)
                .setAction("重试") { requestBluetoothPermissions() }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BluetoothDeviceManager.get().addObserver(observer)
    }

    override fun setupViews() {
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchBluetoothActivity)
            adapter = deviceAdapter
        }

        if (bluetoothAdapter == null) {
            Snackbar.make(binding.root, "设备不支持蓝牙", Snackbar.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Snackbar.make(binding.root, "请先启用蓝牙", Snackbar.LENGTH_SHORT).show()
            return
        }

        requestBluetoothPermissions()
        sendIntent(SearchBluetoothIntent.SearchBluetooth)
    }

    override fun render(state: SearchBluetoothState) {
        super.render(state)
        if (state.error != null) {
            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_SHORT).show()
        }
        deviceAdapter.submitList(state.devices)
        if (state.isLoading) {
            Snackbar.make(binding.root, "正在搜索设备...", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        permissionLauncher.launch(permissions)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun startBluetoothDiscovery() {
        deviceAdapter.submitList(emptyList())
        sendIntent(SearchBluetoothIntent.SearchBluetooth)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private fun stopBleScan() {
        sendIntent(SearchBluetoothIntent.StopScan)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun connectToDevice(device: BluetoothDevice) {
        Log.d("SearchBluetoothActivity", "connectToDevice ${device}")
        stopBleScan()
        Snackbar.make(binding.root, "正在连接 ${device.name ?: device.address}", Snackbar.LENGTH_SHORT).show()
        BluetoothDeviceManager.get().connect(device)
        pairingRetryCount = 0
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun disconnectFromDevice(device: BluetoothDevice) {
        Log.d("SearchBluetoothActivity", "disconnectFromDevice")
        BluetoothDeviceManager.get().disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onStop() {
        super.onStop()
        stopBleScan()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    override fun onDestroy() {
        super.onDestroy()
        stopBleScan()
        BluetoothDeviceManager.get().removeObserver(observer)
    }

    private val observer: (BluetoothState) -> Unit = { s ->
        runOnUiThread {
            Log.d("SearchBluetoothActivity", "observer BluetoothState ${s.toString()}")
            when (s.connectionStatus) {
                BOND_BONDED -> {
                    BluetoothDeviceManager.get().requestDeviceInfo()
                    BluetoothDeviceManager.get().requestBattery()
                    BluetoothDeviceManager.get().requestRunningStatus()
                }

                CONNECTED -> {
                    s.connectedDevice?.let { dev ->
                        connectedAddresses.add(dev.address)
                        deviceAdapter.updateConnectedAddresses(dev.address)
                        attemptBondIfNeeded(dev)
                    }
                }
            }
            s.error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show() }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun attemptBondIfNeeded(device: BluetoothDevice) {
        if (device.bondState != BluetoothDevice.BOND_BONDED && !pairingAttempted.contains(device.address)) {
            pairingAttempted.add(device.address)
            val ok = device.createBond()
            Log.d("SearchBluetoothActivity", "createBond called for ${device.address}, result=$ok")
            if (!ok) {
                Snackbar.make(binding.root, "配对失败，请检查设备或手动配对", Snackbar.LENGTH_LONG).show()
            }
        } else if (device.bondState == BluetoothDevice.BOND_NONE && pairingRetryCount < 3) {
            pairingRetryCount++
            Log.d("SearchBluetoothActivity", "配对失败，重试 $pairingRetryCount 次")
            lifecycleScope.launch(Dispatchers.Main) {
                delay(2000)
                BluetoothDeviceManager.get().disconnect()
                device.createBond()
            }
        } else {
            Snackbar.make(binding.root, "配对失败，请在系统蓝牙设置中手动配对 ${device.address}", Snackbar.LENGTH_LONG).show()
            pairingRetryCount = 0
            pairingAttempted.remove(device.address)
        }
    }
}*/
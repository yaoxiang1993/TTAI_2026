package com.example.ttai.ui.vm

import androidx.lifecycle.viewModelScope
import com.example.ttai.base.MviViewModel
import com.example.ttai.intent.SearchBluetoothIntent
import com.example.ttai.manager.BluetoothDeviceManager
import com.example.ttai.state.SearchBluetoothState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchBluetoothViewModel : MviViewModel<SearchBluetoothIntent, SearchBluetoothState>() {
    private val _state = MutableStateFlow(SearchBluetoothState())
    override val state: StateFlow<SearchBluetoothState> = _state.asStateFlow()

    init {
        // 订阅 BluetoothDeviceManager 的状态，同步设备列表
        BluetoothDeviceManager.get().addObserver { bluetoothState ->
            viewModelScope.launch {
                _state.update { current ->
                    current.copy(
                        devices = bluetoothState.discoveredDevices,
                        isLoading = bluetoothState.isScanning,
                        error = bluetoothState.error,
                        searchBluetooth = bluetoothState.isScanning
                    )
                }
            }
        }
    }

    override fun processIntent(intent: SearchBluetoothIntent) {
        viewModelScope.launch {
            when (intent) {
                is SearchBluetoothIntent.SearchBluetooth -> {
                    if (!_state.value.isLoading && !BluetoothDeviceManager.get().state.isScanning) {
                        _state.update { it.copy(isLoading = true, error = null, searchBluetooth = true) }
                        BluetoothDeviceManager.get().startScan()
                        // 10秒后停止扫描
                        delay(10000)
                        stopScan() // Call stopScan to update state
                    }
                }
                is SearchBluetoothIntent.StopScan -> {
                    stopScan()
                }
            }
        }
    }

    private fun stopScan() {
        BluetoothDeviceManager.get().stopScan()
        _state.update { it.copy(isLoading = false, searchBluetooth = false) }
    }
}
package com.example.ttai.manager

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import android.util.Log
import com.example.ttai.utils.BluetoothProtocolUtils
import com.example.ttai.utils.JsonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

// UUID 常量（服务 FE00，用于特征 FE01 写、FE02 通知）
private val SERVICE_UUID = UUID.fromString("0000FFFE-0000-1000-8000-00805F9B34FB")
private val CHARACTERISTIC_WRITE_UUID = UUID.fromString("0000FE01-0000-1000-8000-00805F9B34FB")
private val CHARACTERISTIC_NOTIFY_UUID = UUID.fromString("0000FE02-0000-1000-8000-00805F9B34FB")
private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")


const val CONNECTED =  "connected"//连接中
const val BOND_BONDED =  "bond_bonded"//配对成功
const val BOND_BONDING =  "bond_bonding"//配对中
const val BOND_ERROR =  "bond_error"//配对失败


// 多监听者单例
object BluetoothDeviceManager {
    private var impl: BluetoothDeviceManagerImpl? = null

    fun initialize(context: Context) {
        impl = BluetoothDeviceManagerImpl(context.applicationContext)
    }

    fun get(): BluetoothDeviceManagerImpl {
        return impl ?: throw IllegalStateException("BluetoothDeviceManager not initialized")
    }
}

class BluetoothDeviceManagerImpl(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private var gatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var notifyCharacteristic: BluetoothGattCharacteristic? = null
    private val discoveredDevices = LinkedHashMap<String, BluetoothDevice>()
    private val pairingAttempted = mutableSetOf<String>()
    private var bondReceiverRegistered = false
    private var pendingConnectAddress: String? = null

    private val bondReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == intent.action) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val newState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
                Log.d("BLE-Bond", "Bond state changed ${device?.address}: $prevState -> $newState")
                when (newState) {
                    BluetoothDevice.BOND_BONDED -> {
                        notifyObservers(state.copy(connectionStatus = BOND_BONDED))
                        if (device?.address == pendingConnectAddress) {
                            pendingConnectAddress = null
                            device?.let { connect(it) }
                        }
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        notifyObservers(state.copy(connectionStatus = BOND_BONDING))

                    }
                    BluetoothDevice.BOND_NONE -> {
                        if (prevState == BluetoothDevice.BOND_BONDING) {
                            notifyObservers(state.copy(error = "配对失败，可能为 Just Works 模式或设备限制"))
                            device?.let {
                                if (pairingAttempted.count { it == device.address } < 3) {
                                    Log.d("BLE-Bond", "重试配对 ${device.address}")
                                    disconnect()
                                    unpair(it)
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(2000)
                                        pair(it)
                                    }
                                } else {
                                    notifyObservers(state.copy(error = "配对失败，已达最大重试次数"))
                                    pairingAttempted.remove(device.address)
                                    pendingConnectAddress = null

                                }
                            }
                        }
                        if (device?.address == pendingConnectAddress) {
                            pendingConnectAddress = null
                        }
                    }
                }
            }
        }
    }

    // 允许多个观察者监听全局状态
    private val observers = mutableSetOf<(BluetoothState) -> Unit>()
    var state = BluetoothState(isBluetoothEnabled = bluetoothAdapter?.isEnabled == true)

    init {
        try {
            context.registerReceiver(bondReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            bondReceiverRegistered = true
        } catch (e: Exception) {
            Log.w("BLE-Bond", "register bond receiver failed: ${e.message}")
        }
    }

    fun addObserver(observer: (BluetoothState) -> Unit) {
        observers.add(observer)
        observer(state)
    }

    fun removeObserver(observer: (BluetoothState) -> Unit) {
        observers.remove(observer)
    }

    private fun notifyObservers(newState: BluetoothState) {
        state = newState
        observers.forEach { it(newState) }
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            val r = result ?: return
            val d = r.device ?: return
            if (!discoveredDevices.containsKey(d.address)) {
                discoveredDevices[d.address] = d
                val sr = r.scanRecord
                val name = d.name ?: sr?.deviceName
                val uuids = sr?.serviceUuids?.joinToString(prefix = "[", postfix = "]") { it.uuid.toString() }
                val mData = sr?.manufacturerSpecificData
                val mKeys = mutableListOf<Int>()
                if (mData != null) {
                    for (i in 0 until mData.size()) { mKeys.add(mData.keyAt(i)) }
                }
                val bytesHex = sr?.bytes?.joinToString(" ") { String.format("%02X", it) }
                Log.d( "BLE-Scan", "Found: name=${name} addr=${d.address} type=${d.type} bondState=${d.bondState} rssi=${r.rssi} tx=${r.txPower} serviceUuids=${uuids} mKeys=${mKeys} adv=${bytesHex}")
                Log.d( "BLE-Scan", "Found: name=${name} alias=${d.alias} addressType=${d.addressType} describeContents=${d.describeContents()}   ")

                Log.d( "BLE-Scan", "Found: name=${name} device=${JsonUtils.toJson(d)}  " )
                notifyObservers(state.copy(discoveredDevices = discoveredDevices.values.toList()))
            }
        }

        override fun onScanFailed(errorCode: Int) {
            notifyObservers(state.copy(error = "Scan failed: $errorCode", isScanning = false))
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d("BluetoothDeviceManager", "onConnectionStateChange status ${status}  newState $newState")
            Log.d("BluetoothDeviceManager", "onConnectionStateChange  gatt ${JsonUtils.toJson(gatt)} ")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        this@BluetoothDeviceManagerImpl.gatt = gatt
                        notifyObservers(state.copy(
                            connectedDevice = gatt?.device,
                            connectionStatus = "Connected (Bond: ${gatt?.device?.bondState})"
                        ))
                        Log.d("BluetoothDeviceManager", "Bond state: ${gatt?.device?.bondState}")
                        gatt?.device?.let {
                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1000) // 延迟1秒确保连接稳定
                                if (gatt?.device?.bondState != BluetoothDevice.BOND_BONDED) {
                                    attemptBondIfNeeded(it)
                                }
                            }
                        }
                        gatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        notifyObservers(state.copy(connectedDevice = null, connectionStatus = "Disconnected", error = null))
                        gatt?.close()
                        this@BluetoothDeviceManagerImpl.gatt = null
                        writeCharacteristic = null
                        notifyCharacteristic = null
                    }
                }
            } else {
                notifyObservers(state.copy(connectedDevice = null, connectionStatus = null, error = "Connection failed: $status"))
                gatt?.close()
                this@BluetoothDeviceManagerImpl.gatt = null
                writeCharacteristic = null
                notifyCharacteristic = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d("BluetoothDeviceManager", "onServicesDiscovered  gatt ${JsonUtils.toJson(gatt)}  status ${status} ")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                notifyObservers(state.copy(error = "Service discovery failed: $status"))
                return
            }
            val service: BluetoothGattService? = gatt?.getService(SERVICE_UUID)
            if (service == null) {
                notifyObservers(state.copy(error = "Service FE00 not found"))
                return
            }
            writeCharacteristic = service.getCharacteristic(CHARACTERISTIC_WRITE_UUID)
            notifyCharacteristic = service.getCharacteristic(CHARACTERISTIC_NOTIFY_UUID)

            if (notifyCharacteristic != null) {
                gatt.setCharacteristicNotification(notifyCharacteristic, true)
                val descriptor = notifyCharacteristic!!.getDescriptor(CCCD_UUID)
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                if (descriptor != null) gatt.writeDescriptor(descriptor)
            }
            // 服务发现后，主线程延时尝试配对，避免竞争
            val dev = gatt?.device
            if (dev != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    attemptBondIfNeeded(dev)
                }
            }
            notifyObservers(state.copy(connectionStatus = CONNECTED))
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            Log.d("BluetoothDeviceManager", "onCharacteristicChanged  gatt ${gatt}  ")
            Log.d("BluetoothDeviceManager", "onCharacteristicChanged  characteristic ${characteristic} ")
            val data = characteristic?.value ?: return
            val hex = data.joinToString(" ") { String.format("%02X", it) }
            Log.d("BluetoothDeviceManager", "${characteristic?.uuid} -> $hex")
            // 尝试解析协议数据并上报
            BluetoothProtocolUtils.parsePacket(data)?.let { result ->
                notifyObservers(state.copy(notificationData = hex, lastParsed = result))
            } ?: notifyObservers(state.copy(notificationData = hex))
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.d("BluetoothDeviceManager", "onCharacteristicWrite  gatt ${gatt} ")
            Log.d("BluetoothDeviceManager", "onCharacteristicWrite  characteristic ${characteristic} ")
            Log.d("BluetoothDeviceManager", "onCharacteristicWrite  status ${status} ")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                notifyObservers(state.copy(error = "Write failed: $status"))
            }
        }
    }

    fun startScan(timeoutMs: Long = 10000) {
        if (state.isScanning) {
            Log.d("BluetoothDeviceManager", "扫描已进行中，跳过")
            return
        }
        if (!isBluetoothEnabled() || scanner == null) {
            notifyObservers(state.copy(error = "Bluetooth not enabled or unsupported"))
            return
        }
        discoveredDevices.clear()
        notifyObservers(state.copy(isScanning = true, discoveredDevices = emptyList(), error = null))

        val filters = listOf(
            ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        )
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED) // Use BALANCED for BLE
            .setReportDelay(0)
            .setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
            .build()
        try {
            scanner.startScan(filters, settings, scanCallback)
            Log.d("BluetoothDeviceManager", "开始扫描，UUID=$SERVICE_UUID, 超时时间: $timeoutMs ms")
        } catch (e: Exception) {
            Log.e("BluetoothDeviceManager", "扫描失败: ${e.message}")
            notifyObservers(state.copy(error = "扫描失败: ${e.message}", isScanning = false))
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(timeoutMs)
            stopScan()
        }
    }

    fun stopScan() {
        try {
            scanner?.stopScan(scanCallback)
            notifyObservers(state.copy(isScanning = false))
            Log.d("BluetoothDeviceManager", "扫描已停止")
        } catch (e: Exception) {
            Log.e("BluetoothDeviceManager", "停止扫描失败: ${e.message}")
        }
    }

    fun connect(device: BluetoothDevice, retryCount: Int = 0) {
        Log.d("BluetoothDeviceManager", " connect  ")
        stopScan()
        gatt?.close()
        gatt = device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
        notifyObservers(state.copy(connectionStatus = "Connecting (Retry: $retryCount, Type: BLE)"))
        Log.d("BluetoothDeviceManager", "连接到 ${device.address}, 类型: BLE, 重试次数: $retryCount")

        if (retryCount < 3) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(20000) // Increase to 20 seconds for BLE
                if (gatt?.device?.bondState != BluetoothDevice.BOND_BONDED && state.connectedDevice == null) {
                    Log.d("BluetoothDeviceManager", "连接失败，重试 ${device.address}")
                    connect(device, retryCount + 1)
                }
            }
        }
    }

    // 先配对再连接：若未配对则等待配对成功后自动连接
    fun connectWithBond(device: BluetoothDevice) {
        if (device.bondState == BluetoothDevice.BOND_BONDED) {
            connect(device)
        } else {
            Log.d("BLE-Bond", "尝试清除旧配对记录: ${device.address}")
            unpair(device)
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000) // 增加延迟以确保设备端清除
                if (device.bondState == BluetoothDevice.BOND_NONE) {
                    pendingConnectAddress = device.address
                    pair(device)
                } else {
                    notifyObservers(state.copy(error = "无法清除旧配对记录"))
                }
            }
        }
    }

    fun disconnect() {
        Log.d("SearchBluetoothActivity", " disconnect ")
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        writeCharacteristic = null
        notifyCharacteristic = null
        notifyObservers(state.copy(connectedDevice = null, connectionStatus = null, error = null))
    }

    // 全局指令发送：FE01 写
    fun send(command: ByteArray) {
        val c = writeCharacteristic ?: run {
            notifyObservers(state.copy(error = "No write characteristic (FE01) available"))
            return
        }
        c.value = command
        c.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt?.writeCharacteristic(c)
    }

    // ============== 配对（Bond）相关 ==============
    fun pairCurrent(): Boolean {
        val dev = gatt?.device ?: return false
        return pair(dev)
    }

    fun pair(device: BluetoothDevice): Boolean {
        Log.d("BLE-Bond", "配对检查: address=${device.address}, bondState=${device.bondState}, type=BLE")
        return if (device.bondState == BluetoothDevice.BOND_BONDED) {
            Log.d("BLE-Bond", "设备已配对: ${device.address}")
            true
        } else if (!pairingAttempted.contains(device.address)) {
            pairingAttempted.add(device.address)
            gatt?.disconnect()
            val ok = try {
                device.createBond()
            } catch (e: Exception) {
                Log.e("BLE-Bond", "createBond exception: ${e.message}")
                false
            }
            Log.d("BLE-Bond", "createBond called for ${device.address}, result=$ok")
            if (ok) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(30000) // 延长到30秒
                    if (device.bondState != BluetoothDevice.BOND_BONDED) {
                        Log.d("BLE-Bond", "配对超时: ${device.address}")
                        notifyObservers(state.copy(error = "配对超时，可能为 Just Works 模式"))
                        pairingAttempted.remove(device.address)
                        pendingConnectAddress = null
                    }
                }
            }
            ok
        } else {
            Log.d("BLE-Bond", "已尝试配对: ${device.address}")
            false
        }
    }

    private fun attemptBondIfNeeded(device: BluetoothDevice) {
        if (device.bondState != BluetoothDevice.BOND_BONDED && !pairingAttempted.contains(device.address)) {
            pairingAttempted.add(device.address)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val ok = device.createBond()
                    Log.d("BLE-Bond", "createBond called for ${device.address}, result=$ok")
                } catch (e: Exception) {
                    Log.w("BLE-Bond", "createBond exception: ${e.message}")
                }
            }
        }
    }

    // 快捷命令
    fun requestDeviceInfo() = send(BluetoothProtocolUtils.buildGetDeviceInfoPacket())
    fun requestBattery() = send(BluetoothProtocolUtils.buildGetBatteryStatusPacket())
    fun requestRunningStatus() = send(BluetoothProtocolUtils.buildGetRunningStatusPacket())
    fun setManual(suction: Int, vibration: Int) = send(BluetoothProtocolUtils.buildManualSettingPacket(suction, vibration))
    fun setMode(mode: Int) = send(BluetoothProtocolUtils.buildSetRunningModePacket(mode))

    // 解除配对与释放
    fun unpair(device: BluetoothDevice): Boolean {
        return try {
            val m = device.javaClass.getMethod("removeBond")
            m.isAccessible = true
            m.invoke(device)
            pairingAttempted.remove(device.address)
            true
        } catch (e: Exception) {
            Log.w("BLE-Bond", "removeBond exception: ${e.message}")
            false
        }
    }

    fun unpairCurrent(): Boolean {
        val dev = gatt?.device ?: return false
        return unpair(dev)
    }

    fun cleanup() {
        stopScan()
        disconnect()
        if (bondReceiverRegistered) {
            try { context.unregisterReceiver(bondReceiver) } catch (_: Exception) {}
            bondReceiverRegistered = false
        }
    }
}

data class BluetoothState(
    val isBluetoothEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val discoveredDevices: List<BluetoothDevice> = emptyList(),
    val connectedDevice: BluetoothDevice? = null,
    val connectionStatus: String? = null,
    val notificationData: String? = null,
    val lastParsed: Any? = null,
    val error: String? = null
)
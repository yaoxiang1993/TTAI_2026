package com.example.ttai

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.ttai.event.ChatSlotChangedEvent
import com.example.ttai.event.CloseBLEEvent
import com.example.ttai.myenum.WorkMode
import com.example.ttai.utils.DebugUtils
import com.example.ttai.utils.ToastUtils
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.UUID

object MyBluetoothManager{

    private lateinit var context: Context
    private lateinit var callback: BluetoothCallback
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val GENERIC_ACCESS_SERVICE_UUID: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
    private val DEVICE_NAME_UUID: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    private val APPEARANCE_UUID: UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")
    private val CONNECTION_PARAMS_UUID: UUID = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb")
    private val DEVICE_INFO_SERVICE_UUID: UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
    private val MODEL_NUMBER_UUID: UUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb")
    private val BATTERY_SERVICE_UUID: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    private val BATTERY_LEVEL_UUID: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
    private val CUSTOM_SERVICE_UUID: UUID = UUID.fromString("0000fe00-0000-1000-8000-00805f9b34fb")

    private val CHARACTERISTIC_UUID_FE00: UUID = UUID.fromString("0000fe00-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID_FE01: UUID = UUID.fromString("0000fe01-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID_FE02: UUID = UUID.fromString("0000fe02-0000-1000-8000-00805f9b34fb")
    private val CLIENT_CHARACTERISTIC_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    private val MODE5_COMMAND = byteArrayOf(0x01, 0x12, 0x05, 0x00) // Motor1 mode5
    private val STOP_COMMAND = byteArrayOf(0x01, 0x12, 0x00, 0x00) // Motor1 stop
    private val BATTERY_QUERY_COMMAND = byteArrayOf(0x01, 0x01, 0x00, 0x00) // 电池状态查询

    public var connectedDevice: BluetoothDevice? = null

    private var currtentByteArray: ByteArray? = null

    public var workMode : WorkMode = WorkMode.SUCTION

    interface BluetoothCallback {
        fun onBluetoothStateChanged(enabled: Boolean)
        fun onDeviceFound(device: BluetoothDevice)
        fun onDiscoveryStarted()
        fun onDiscoveryFinished()
        fun onDeviceConnected(device: BluetoothDevice, isBLE: Boolean)
        fun onConnectionFailed(error: String)
        fun onDevicePairing(device: BluetoothDevice, bondState: Int)
        fun onPairingFailed(error: String)
        fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic, value: ByteArray)
        fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray)
        fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic, status: Int)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val device = gatt.device
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d("BluetoothManager", "BLE 设备连接成功: ${device.name}")
                        bluetoothGatt = gatt
                        connectedDevice = device
                        gatt.discoverServices()
                        callback.onDeviceConnected(device, true)
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.e("BluetoothManager", "BLE 设备断开连接: ${device.name}")
                        connectedDevice = null
                        callback.onConnectionFailed("BLE 设备断开连接")
                        gatt.close()
                        try {
                            EventBus.getDefault().post(CloseBLEEvent(success = true))
                        } catch (e: Exception) {
                        }
                    }
                }
            } else {
                Log.e("BluetoothManager", "BLE 连接失败: ${device.name}, 状态: $status")
                callback.onConnectionFailed("BLE 连接失败: 状态 $status")
                gatt.close()
                if (status == 133 ) {
                    connectBLEDevice(device, attempt = 2)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "BLE 服务发现成功: ${gatt.device.name}")
                gatt.services.forEach { service ->
                    Log.d("BluetoothManager", "发现服务: ${service.uuid}")
                    service.characteristics.forEach { characteristic ->
                        Log.d("BluetoothManager", "发现特征: ${characteristic.uuid}, 属性: ${characteristic.properties}  , writeType : ${characteristic.writeType}  ")
                    }
                }
                // 读取标准特征
                val genericAccessService = gatt.getService(GENERIC_ACCESS_SERVICE_UUID)
                if (genericAccessService != null) {
                    readCharacteristic(genericAccessService.getCharacteristic(DEVICE_NAME_UUID))
                    readCharacteristic(genericAccessService.getCharacteristic(APPEARANCE_UUID))
                    readCharacteristic(genericAccessService.getCharacteristic(CONNECTION_PARAMS_UUID))
                }
                val deviceInfoService = gatt.getService(DEVICE_INFO_SERVICE_UUID)
                if (deviceInfoService != null) {
                    readCharacteristic(deviceInfoService.getCharacteristic(MODEL_NUMBER_UUID))
                }
                // 检查标准电池服务
                val batteryService = gatt.getService(BATTERY_SERVICE_UUID)
                if (batteryService != null) {
                    val batteryLevelChar = batteryService.getCharacteristic(BATTERY_LEVEL_UUID)
                    if (batteryLevelChar != null && (batteryLevelChar.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        Log.d("BluetoothManager", "尝试读取标准电池电量: ${batteryLevelChar.uuid}")
                        readCharacteristic(batteryLevelChar)
                    }
                }
                // 启用通知并执行 mode5 和电池查询
                val customService = gatt.getService(CUSTOM_SERVICE_UUID)
                if (customService != null) {
                    val characteristic = customService.getCharacteristic(CHARACTERISTIC_UUID_FE02)
                    if (characteristic != null) {
                        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            Log.d("BluetoothManager", "启用通知: ${characteristic.uuid}")
                            gatt.setCharacteristicNotification(characteristic, true)
                            val descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG)
                            descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(descriptor)
                        }
                        // 执行 mode5 命令
                        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                            (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                            Log.d("BluetoothManager", "写入 mode5 命令: ${MODE5_COMMAND.joinToString { it.toString(16) }}")
                            characteristic.value = MODE5_COMMAND
                            gatt.writeCharacteristic(characteristic)
                            // 写入电池查询命令
                            Log.d("BluetoothManager", "写入电池状态查询命令: ${BATTERY_QUERY_COMMAND.joinToString { it.toString(16) }}")
                            characteristic.value = BATTERY_QUERY_COMMAND
                            gatt.writeCharacteristic(characteristic)
                        }
                    }
                    // 尝试读取电池状态
                    val batteryCharacteristic = customService.getCharacteristic(CHARACTERISTIC_UUID_FE01)
                    if (batteryCharacteristic != null && (batteryCharacteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        Log.d("BluetoothManager", "尝试读取电池状态: ${batteryCharacteristic.uuid}")
                        readCharacteristic(batteryCharacteristic)
                    }
                }
            } else {
                Log.e("BluetoothManager", "BLE 服务发现失败: 状态 $status")
                callback.onConnectionFailed("BLE 服务发现失败: 状态 $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "读取特征成功: ${characteristic.uuid}, 值: ${characteristic.value?.joinToString { it.toString(16) }}")
                callback.onCharacteristicRead(characteristic, characteristic.value ?: byteArrayOf())
            } else {
                Log.e("BluetoothManager", "读取特征失败: ${characteristic.uuid}, 状态: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            Log.d("BluetoothManager", "特征值变化: ${characteristic.uuid}, 值: ${characteristic.value?.joinToString { it.toString(16) }}")
            callback.onCharacteristicChanged(characteristic, characteristic.value ?: byteArrayOf())
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "写入特征成功: ${characteristic.uuid}")
                callback.onCharacteristicWrite(characteristic, status)
            } else {
                Log.e("BluetoothManager", "写入特征失败: ${characteristic.uuid}, 状态: $status")
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BluetoothManager", "写入描述符成功: ${descriptor.uuid}")
            } else {
                Log.e("BluetoothManager", "写入描述符失败: ${descriptor.uuid}, 状态: $status")
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null && (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)) {
                        Log.d("BluetoothManager", "发现设备: ${device.name} (${device.address}), 类型: ${device.type}")
                        callback.onDeviceFound(device)
                    } else {
                        Log.e("BluetoothManager", "缺少 BLUETOOTH_CONNECT 权限或设备为空")
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("BluetoothManager", "开始扫描")
                    callback.onDiscoveryStarted()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("BluetoothManager", "扫描完成")
                    callback.onDiscoveryFinished()
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    if (device != null) {
                        Log.d("BluetoothManager", "配对状态改变: ${device.name}, 状态: $bondState")
                        callback.onDevicePairing(device, bondState)
                        if (bondState == BluetoothDevice.BOND_BONDED && device.type != BluetoothDevice.DEVICE_TYPE_LE) {
                            connectToDevice(device)
                        }
                    }
                }
            }
        }
    }

    fun initialize(context: Context, callback: BluetoothCallback) {
        this.context = context
        this.callback = callback
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }
        context.registerReceiver(receiver, filter)
        Log.d("BluetoothManager", "广播接收器注册完成")
    }

    fun isBluetoothSupported(): Boolean {
        return bluetoothAdapter != null
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun enableBluetooth(requestCode: Int) {
        if (!isBluetoothEnabled()) {
            Log.d("BluetoothManager", "蓝牙未开启，请求开启")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            (context as? androidx.appcompat.app.AppCompatActivity)?.startActivityForResult(enableBtIntent, requestCode)
        } else {
            Log.d("BluetoothManager", "蓝牙已开启，调用回调")
            callback.onBluetoothStateChanged(true)
        }
    }

    fun startDiscovery() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            Log.d("BluetoothManager", "开始蓝牙扫描")
            bluetoothAdapter?.cancelDiscovery()
            bluetoothAdapter?.startDiscovery()
        } else {
            Log.e("BluetoothManager", "缺少 BLUETOOTH_SCAN 权限")
            callback.onConnectionFailed("缺少蓝牙扫描权限")
        }
    }

    fun pairDevice(device: BluetoothDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BluetoothManager", "缺少 BLUETOOTH_CONNECT 权限")
            callback.onPairingFailed("缺少蓝牙配对权限")
            return
        }

        Log.d("BluetoothManager", "开始配对设备: ${device.name} (${device.address})")
        try {
            bluetoothAdapter?.cancelDiscovery()
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                Log.d("BluetoothManager", "设备已配对: ${device.name}")
                callback.onDevicePairing(device, BluetoothDevice.BOND_BONDED)
            } else if (device.type != BluetoothDevice.DEVICE_TYPE_LE) {
                device.createBond()
            } else {
                Log.d("BluetoothManager", "BLE 设备跳过配对，直接尝试连接: ${device.name}")
                connectBLEDevice(device)
            }
        } catch (e: Exception) {
            Log.e("BluetoothManager", "配对失败: ${e.message}")
            callback.onPairingFailed("配对失败: ${e.message}")
        }
    }
    /**
     * 解除指定设备的蓝牙配对（移除配对关系）
     * @param device 要解除配对的设备
     * @return 是否成功发起解除配对请求
     */
    fun unpairDevice(device: BluetoothDevice): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("BluetoothManager", "缺少 BLUETOOTH_CONNECT 权限，无法解除配对")
            callback.onPairingFailed("缺少蓝牙连接权限，无法解除配对")
            return false
        }

        Log.d("BluetoothManager", "正在解除设备配对: ${device.name} (${device.address})")

        return try {
            val removeBondMethod = device.javaClass.getMethod("removeBond")
            val result = removeBondMethod.invoke(device) as Boolean
            if (result) {
                Log.d("BluetoothManager", "解除配对请求已发送，等待系统广播确认")
                // 可选：你可以在 BOND_STATE_CHANGED 广播中监听到 BOND_NONE 来确认最终成功
            } else {
                Log.e("BluetoothManager", "解除配对请求调用失败")
            }
            result
        } catch (e: Exception) {
            Log.e("BluetoothManager", "解除配对失败（反射调用 removeBond 异常）: ${e.message}")
            callback.onPairingFailed("解除配对失败: ${e.message}")
            false
        }
    }
    fun connectToDevice(device: BluetoothDevice, maxRetries: Int = 2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BluetoothManager", "缺少 BLUETOOTH_CONNECT 权限")
            callback.onConnectionFailed("缺少蓝牙连接权限")
            return
        }

        if (device.type == BluetoothDevice.DEVICE_TYPE_LE) {
            Log.d("BluetoothManager", "尝试连接 BLE 设备: ${device.name}")
            connectBLEDevice(device)
        } else {
            Log.d("BluetoothManager", "尝试连接经典蓝牙设备: ${device.name}")
            connectClassicDevice(device, maxRetries)
        }
    }

    private fun connectBLEDevice(device: BluetoothDevice, attempt: Int = 1, maxAttempts: Int = 3) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BluetoothManager", "缺少 BLUETOOTH_CONNECT 权限")
            callback.onConnectionFailed("缺少蓝牙连接权限")
            return
        }

        if (attempt > maxAttempts) {
            Log.e("BluetoothManager", "BLE 连接失败: ${device.name}，超过最大重试次数 $maxAttempts")
            callback.onConnectionFailed("BLE 连接失败: 超过最大重试次数")
            return
        }

        Log.d("BluetoothManager", "连接 BLE 设备: ${device.name} (${device.address}), 尝试 $attempt/$maxAttempts")
        bluetoothGatt?.close()
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private fun connectClassicDevice(device: BluetoothDevice, maxRetries: Int) {
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            Log.d("BluetoothManager", "设备未配对，尝试配对: ${device.name}")
            pairDevice(device)
            return
        }

        Log.d("BluetoothManager", "尝试连接经典蓝牙设备: ${device.name} (${device.address})")
        bluetoothAdapter?.cancelDiscovery()

        Thread {
            var attempt = 0
            var connected = false
            var lastError: String? = null
            val uuids = listOf(CUSTOM_SERVICE_UUID )

            for (uuid in uuids) {
                attempt = 0
                while (attempt < maxRetries && !connected) {
                    attempt++
                    Log.d("BluetoothManager", "连接尝试 $attempt/$maxRetries，UUID: $uuid")
                    try {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                        bluetoothSocket?.connect()
                        Log.d("BluetoothManager", "设备连接成功: ${device.name}，UUID: $uuid")
                        connected = true
                        callback.onDeviceConnected(device, false)
                    } catch (e: IOException) {
                        Log.e("BluetoothManager", "连接失败 (尝试 $attempt, UUID: $uuid): ${e.message}")
                        lastError = e.message
                        try {
                            bluetoothSocket?.close()
                        } catch (e: IOException) {
                            Log.e("BluetoothManager", "关闭Socket失败: ${e.message}")
                        }
                    }
                }
                if (connected) break
            }

            if (!connected) {
                callback.onConnectionFailed("连接失败: ${lastError ?: "未知错误"}")
            }
        }.start()
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic?) {
        if (characteristic == null) {
            Log.e("BluetoothManager", "特征为空，无法读取")
            return
        }
        if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
            Log.d("BluetoothManager", "读取特征: ${characteristic.uuid}")
            bluetoothGatt?.readCharacteristic(characteristic)
        } else {
            Log.e("BluetoothManager", "特征 ${characteristic.uuid} 不支持读取")
        }
    }

    fun writeCharacteristic(context: Context,value: ByteArray) {
        kotlin.runCatching {
            val service =  getGattService( CHARACTERISTIC_UUID_FE00)
            if (service == null) {
                Log.e("MyBluetoothManager", " 请先绑定设备 service == null ")
                ToastUtils.showShort(context,"请先绑定设备")
                return
            }
            val characteristic = service?.characteristics?.get(1)
            if (characteristic == null) {
                Log.e("MyBluetoothManager", " 请先绑定设备  characteristic == null")
                ToastUtils.showShort(context,"请先绑定设备")
                return
            }
            if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                Log.d("BluetoothManager", "写入特征: ${characteristic.uuid}, 值: ${value.joinToString { it.toString(16) }}")
                characteristic.value = value
                currtentByteArray = value
                bluetoothGatt?.writeCharacteristic(characteristic)
            } else {
                Log.e("BluetoothManager", "特征 ${characteristic.uuid} 不支持写入")
            }
        }
    }
    fun writeCharacteristicUp( vibrationTemp: Int?,suckingTemp: Int?) {
        var vibration = vibrationTemp
        var sucking = suckingTemp
        kotlin.runCatching {
            when(workMode){
                WorkMode.SUCTION -> {
                    vibration = 0
                }
                WorkMode.VIBRATION -> {
                    sucking = 0
                }
                WorkMode.BOTH -> TODO()
            }
            val service =  getGattService( CHARACTERISTIC_UUID_FE00)
            if (service == null) {
                Log.e("MyBluetoothManager", " service == null")
                return
            }
            val characteristic = service?.characteristics?.get(1)
            if (characteristic == null) {
                Log.e("MyBluetoothManager", " 请先绑定设备  characteristic == null")
                return
            }
            if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
                (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
                if (currtentByteArray == null){
                    currtentByteArray = byteArr ayOf(0x02,0x01,vibration?.toByte()?:0,sucking?.toByte()?:0)
                    characteristic.value = currtentByteArray
                    Log.d("BluetoothManager", "写入特征: ${characteristic.uuid}, 值: ${currtentByteArray!!.joinToString { it.toString(16) }}")
                    bluetoothGatt?.writeCharacteristic(characteristic)
                }else{
                    Log.d("BluetoothManager", "BluetoothManager ${currtentByteArray!![2]} ${currtentByteArray!![3]}")
                    currtentByteArray!![2]= vibration?.toByte()?:0
                    currtentByteArray!![3]= sucking?.toByte()?:0
                    characteristic.value = currtentByteArray
                    Log.d("BluetoothManager", "写入特征: ${characteristic.uuid}, 值: ${currtentByteArray!!.joinToString { it.toString(16) }}")
                    bluetoothGatt?.writeCharacteristic(characteristic)
                }
            } else {
                Log.e("BluetoothManager", "特征 ${characteristic.uuid} 不支持写入")
            }
        }
    }
    fun getGattService(  serviceUuid: UUID): BluetoothGattService? {
        return bluetoothGatt?.getService(serviceUuid)
    }

    fun checkPermissions(permissions: Array<String>): Boolean {
        val result = permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        Log.d("BluetoothManager", "权限检查结果: $result, 权限列表: ${permissions.joinToString()}")
        return result
    }

    fun cleanup() {
        try {
            bluetoothSocket?.close()
            Log.d("BluetoothManager", "Socket 关闭")
        } catch (e: IOException) {
            Log.e("BluetoothManager", "关闭Socket失败: ${e.message}")
        }
        connectedDevice = null
        bluetoothGatt?.close()
        Log.d("BluetoothManager", "GATT 关闭")
        context.unregisterReceiver(receiver)
        Log.d("BluetoothManager", "广播接收器已注销")
    }
    /**
     * 获取当前设备的震动值
     * */
    fun getVibrationIntensity(): Int{
        currtentByteArray?.let {
            return currtentByteArray!![2].toInt()
        }
        return 0
    }
    /**
     * 获取当前设备的吮吸值
     * */
    fun getSuckingIntensity(): Int{
        currtentByteArray?.let {
            return currtentByteArray!![3].toInt()
        }
        return 0
    }
}
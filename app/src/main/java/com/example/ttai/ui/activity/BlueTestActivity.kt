package com.example.ttai.ui.activity

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.ttai.R
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ttai.MyBluetoothManager
import com.example.ttai.adapter.BluetoothDeviceAdapter
import com.example.ttai.utils.ToastUtils
import java.util.UUID

/**
 *
 * 发现服务: 00001800-0000-1000-8000-00805f9b34fb
 * 发现特征: 00002a01-0000-1000-8000-00805f9b34fb, 属性: 2  mDescriptors  []
 * 发现特征: 00002a00-0000-1000-8000-00805f9b34fb, 属性: 2  mDescriptors  []
 * 发现特征: 00002a04-0000-1000-8000-00805f9b34fb, 属性: 2  mDescriptors  []
 * 发现服务: 00001801-0000-1000-8000-00805f9b34fb
 * 发现特征: 00002a05-0000-1000-8000-00805f9b34fb, 属性: 32  mDescriptors  [android.bluetooth.BluetoothGattDescriptor@1e4936b]
 * 发现服务: 0000fe00-0000-1000-8000-00805f9b34fb
 * 发现特征: 0000fe02-0000-1000-8000-00805f9b34fb, 属性: 16  mDescriptors  [android.bluetooth.BluetoothGattDescriptor@2ff8ec8]
 * 发现特征: 0000fe01-0000-1000-8000-00805f9b34fb, 属性: 4  mDescriptors  []
 * 读取特征: 00002a00-0000-1000-8000-00805f9b34fb
 * 读取特征: 00002a01-0000-1000-8000-00805f9b34fb
 * 读取特征: 00002a04-0000-1000-8000-00805f9b34fb
 *
 * */
class BlueTestActivity : AppCompatActivity(), MyBluetoothManager.BluetoothCallback {
    private var devices: ArrayList<BluetoothDevice> = ArrayList()
    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_BLUETOOTH_PERMISSIONS = 2
    private val DEVICE_NAME_UUID: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    private val APPEARANCE_UUID: UUID = UUID.fromString("00002a01-0000-1000-8000-00805f9b34fb")
    private val CONNECTION_PARAMS_UUID: UUID = UUID.fromString("00002a00-0000-1000-8000-00805f9b34fb")
    private val MODEL_NUMBER_UUID: UUID = UUID.fromString("00002a04-0000-1000-8000-00805f9b34fb")
    private val BATTERY_LEVEL_UUID: UUID = UUID.fromString("00002a05-0000-1000-8000-00805f9b34fb")

    private val CHARACTERISTIC_UUID_FE02: UUID = UUID.fromString("0000fe02-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID_FE01: UUID = UUID.fromString("0000fe01-0000-1000-8000-00805f9b34fb")
    private var deviceInfo = mutableMapOf<String, String>()

    private var tvSearch : TextView ? =null
    private var ivLoad : ImageView ? =null

    private val deviceAdapter = BluetoothDeviceAdapter(
        onDeviceClick = { selectedDevice ->
            /*    val resultIntent = Intent().apply {
                putExtra("selected_device", selectedDevice)
            }
            MyBluetoothManager.connectToDevice(selectedDevice)
            setResult(RESULT_OK, resultIntent)
            finish()
         */
        },
        onToggleClick = { device,connectedAddresses ->
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_bluetooth)
        MyBluetoothManager.initialize(this,  this)
        tvSearch = findViewById<TextView>(R.id.tvSearch)
        ivLoad = findViewById<ImageView>(R.id.ivLoad)
        devices = ArrayList()
         val mRecyclerView = findViewById<RecyclerView>(R.id.mRecyclerView)
        if (MyBluetoothManager.connectedDevice != null) {
            ivLoad?.isVisible=false
            tvSearch?.text="设备已连接"
            deviceAdapter.updateConnectedAddresses( MyBluetoothManager.connectedDevice!!.address)
            devices.add(MyBluetoothManager.connectedDevice!!)
        }else{
            ivLoad?.let {
                Glide.with(this)
                    .load(R.mipmap.gif_loading)  // 替换为实际 GIF URL
                    .into(it)
            }
        }
        deviceAdapter.submitList(devices)
        mRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BlueTestActivity)
            adapter = deviceAdapter
        }
        // 检查蓝牙支持
        if (!MyBluetoothManager.isBluetoothSupported()) {
            Log.e("BlueTestActivity", "设备不支持蓝牙")
            ToastUtils.showShort(this,"设备不支持蓝牙")
            finish()
            return
        }

        // 检查权限
        checkBluetoothPermissions()
        MyBluetoothManager.enableBluetooth(REQUEST_ENABLE_BT)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {   finish() }
    }

    private fun checkBluetoothPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            Log.d("BlueTestActivity", "请求权限: ${permissionsToRequest.joinToString()}")
            permissionsToRequest.forEach {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, it)) {
                    ToastUtils.showShort(this, "需要 $it 权限以使用蓝牙功能")
                }
            }
            ActivityCompat.requestPermissions(this, permissionsToRequest, REQUEST_BLUETOOTH_PERMISSIONS)
        } else {
            Log.d("BlueTestActivity", "所有权限已授予")
            if (MyBluetoothManager.connectedDevice==null){
                MyBluetoothManager.startDiscovery()
            }
        }
    }

    override fun onBluetoothStateChanged(enabled: Boolean) {
        Log.d("BlueTestActivity", "蓝牙状态改变: enabled=$enabled")
        if (!enabled){
            ToastUtils.showShort(this,  "蓝牙未开启")
        }
    }

    override fun onDeviceFound(device: BluetoothDevice) {
        Log.d("BlueTestActivity", "发现设备: ${device.name} (${device.address}), 配对状态: ${device.bondState}, 类型: ${device.type}")
        if (!devices.contains(device) && "HuiXiang"==device.name){
            devices.add(device)
        }
        deviceAdapter.notifyDataSetChanged()
    }

    override fun onDiscoveryStarted() {
        Log.d("BlueTestActivity", "开始扫描")
        ivLoad?.isVisible = true
        devices.clear()
        deviceAdapter.notifyDataSetChanged()
        deviceInfo.clear()
    }

    override fun onDiscoveryFinished() {
        Log.d("BlueTestActivity", "扫描完成")
        tvSearch?.text ="设备搜索完成"
        ivLoad?.isVisible = false
        ToastUtils.showShort(this, "扫描完成")
    }

    override fun onDeviceConnected(device: BluetoothDevice, isBLE: Boolean) {
        runOnUiThread {
            Log.d("BlueTestActivity", "连接成功: ${device.name}, 是 BLE: $isBLE")
            ToastUtils.showShort(this,"连接成功")
            deviceInfo.clear()
            deviceAdapter.updateConnectedAddresses( device.address)
            deviceAdapter.notifyDataSetChanged()
        }
    }

    override fun onConnectionFailed(error: String) {
        runOnUiThread {
            Log.e("BlueTestActivity", "连接失败: $error")
            ToastUtils.showShort(this, error,)
            deviceInfo.clear()
        }
    }

    override fun onDevicePairing(device: BluetoothDevice, bondState: Int) {
        runOnUiThread {
            when (bondState) {
                BluetoothDevice.BOND_BONDED -> {
                    Log.d("BlueTestActivity", "设备配对成功: ${device.name}")
                    ToastUtils.showShort(this, "配对成功: ${device.name}")
                    if (device.type == BluetoothDevice.DEVICE_TYPE_LE) {
                        MyBluetoothManager.connectToDevice(device)
                    }
                }
                BluetoothDevice.BOND_BONDING -> {
                    Log.d("BlueTestActivity", "设备配对中: ${device.name}")

                    ToastUtils.showShort(this, "配对中: ${device.name})")
                }
                BluetoothDevice.BOND_NONE -> {
                    Log.d("BlueTestActivity", "设备未配对: ${device.name}")
                    ToastUtils.showShort(this, "设备未配对: ${device.name} ")
                }
            }
        }
    }

    override fun onPairingFailed(error: String) {
        runOnUiThread {
            Log.e("BlueTestActivity", "配对失败: $error")
            ToastUtils.showShort(this, error)
        }
    }

    override fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        runOnUiThread {
            Log.d("BlueTestActivity", "特征读取: ${characteristic.uuid}, 值: ${value.joinToString { it.toString(16) }}")
            when (characteristic.uuid) {
                DEVICE_NAME_UUID -> deviceInfo["设备名称"] = value.toString(Charsets.UTF_8)
                APPEARANCE_UUID -> deviceInfo["外观"] = value.joinToString { it.toString(16) }
                CONNECTION_PARAMS_UUID -> deviceInfo["首选连接参数"] = value.joinToString { it.toString(16) }
                MODEL_NUMBER_UUID -> deviceInfo["型号"] = value.toString(Charsets.UTF_8)
                BATTERY_LEVEL_UUID, CHARACTERISTIC_UUID_FE01 -> {
                    val batteryLevel = if (value.isNotEmpty()) value[0].toInt() and 0xFF else 0
                    deviceInfo["电池状态"] = "${value.joinToString { it.toString(16) }} ($batteryLevel%)"
                }
            }
            updateDeviceInfoDisplay()
        }
    }

    override fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        runOnUiThread {
            Log.d("BlueTestActivity", "特征变化: ${characteristic.uuid}, 值: ${value.joinToString { it.toString(16) }}")
            if (characteristic.uuid == CHARACTERISTIC_UUID_FE02) {
                val batteryLevel = if (value.isNotEmpty()) value[0].toInt() and 0xFF else 0
                deviceInfo["电池状态"] = "${value.joinToString { it.toString(16) }} ($batteryLevel%)"
                updateDeviceInfoDisplay()
            }
        }
    }

    override fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic, status: Int) {
        runOnUiThread {
            Log.d("BlueTestActivity", "写入特征结果: ${characteristic.uuid}, 状态: $status")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            Log.d("BlueTestActivity", "蓝牙开启结果: resultCode=$resultCode")
            onBluetoothStateChanged(resultCode == RESULT_OK)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d("BlueTestActivity", "所有权限授予成功")
                ToastUtils.showShort(this, "权限授予成功")
                MyBluetoothManager.startDiscovery()
            } else {
                Log.e("BlueTestActivity", "权限被拒绝: ${permissions.joinToString()}")
                ToastUtils.showLong(this, "需要蓝牙和位置权限才能正常工作，请在设置中启用")
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun bondStateToString(bondState: Int): String {
        return when (bondState) {
            BluetoothDevice.BOND_BONDED -> "已配对"
            BluetoothDevice.BOND_BONDING -> "配对中"
            BluetoothDevice.BOND_NONE -> "未配对"
            else -> "未知"
        }
    }
    private fun updateDeviceInfoDisplay() {
        val infoText = buildString {
            append("设备信息:\n")
            deviceInfo.forEach { (key, value) ->
                append("$key: $value\n")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
       // MyBluetoothManager.cleanup()
    }
}
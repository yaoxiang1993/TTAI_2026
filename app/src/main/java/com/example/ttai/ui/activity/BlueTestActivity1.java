package com.example.ttai.ui.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.ttai.R;
import com.example.ttai.utils.JsonUtils;

import java.util.UUID;

public class BlueTestActivity1 extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private Context context;
    private static final UUID SERVICE_UUID = UUID.fromString("0000fe00-0000-1000-8000-00805f9b34fb");
    private static final UUID WRITE_CHAR_UUID = UUID.fromString("0000fe01-0000-1000-8000-00805f9b34fb");
    private static final UUID NOTIFY_CHAR_UUID = UUID.fromString("0000fe02-0000-1000-8000-00805f9b34fb");
    private static final UUID CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final byte[] BATTERY_QUERY_COMMAND = {0x01,  0x011};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_test1);
        context = this;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "请开启蓝牙", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startScan();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void startScan() {
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                // 过滤设备名称为 HuiXiang
                if (device.getName() != null && device.getName().equals("HuiXiang")) {
                    scanner.stopScan(this);
                    connectToDevice(device);
                }
            }
        });
    }

    private void connectToDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        bluetoothGatt = device.connectGatt(this, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d("BLE", "onConnectionStateChange status "+status+" newState "+newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BLE", "连接成功，启动服务发现");
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BLE", "断开连接");
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.d("BLE", "onServicesDiscovered status "+status+" Device "+ gatt.getDevice().getName() );
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(SERVICE_UUID);
                    Log.d("BLE", "服务发现成功  ");
                    if (service != null) {
                        for(BluetoothGattCharacteristic characteristic:service.getCharacteristics()){
                            Log.d("BLE", " 连接成功获取详情信息 characteristic  Uuid "+characteristic.getUuid() );
                        }
                        // 启用 FE02 的通知
                        BluetoothGattCharacteristic notifyChar = service.getCharacteristic(NOTIFY_CHAR_UUID);
                        Log.d("BLE", "服务发现成功  notifyChar "+notifyChar.getUuid());
                        if (notifyChar != null) {
                            gatt.setCharacteristicNotification(notifyChar, true);
                            sendBatteryRequest(gatt);
                        }
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.d("BLE", "onDescriptorWrite status "+status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "通知启用成功，发送获取电量命令");
                    // 通知启用后，发送获取电池状态命令
                    sendBatteryRequest(gatt);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.d("BLE", "onCharacteristicChanged characteristic "+  characteristic.getDescriptors());
                if (characteristic.getUuid().equals(NOTIFY_CHAR_UUID)) {
                    byte[] data = characteristic.getValue();
                    parseBatteryData(data);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d("BLE", "onCharacteristicChanged status "+status+"  characteristic Uuid "+  characteristic.getUuid() +" Value "+  characteristic.getValue().toString() );
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("BLE", "写入命令成功，等待通知");
                } else {
                    Log.e("BLE", "写入命令失败，状态: " + status);
                }
            }

            @Override
            public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
                super.onCharacteristicRead(gatt, characteristic, value, status);
                Log.d("BLE", "onCharacteristicRead status "+status+"  value "+  value +" characteristic "+characteristic.getUuid()+"  "+characteristic.getDescriptors());
                for(BluetoothGattDescriptor   descriptor: characteristic.getDescriptors()){
                    Log.d("BLE", "onCharacteristicRead characteristic  Uuid "+descriptor.getUuid()+"  "+descriptor.getUuid()+"  "+descriptor.getValue());
                }

            }
        });
    }

    private void sendBatteryRequest(BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(SERVICE_UUID);
        if (service != null) {
            BluetoothGattCharacteristic writeChar = service.getCharacteristic(WRITE_CHAR_UUID);
            if (writeChar != null) {
                // 构造获取电池状态命令 (command_id=0x01, key=0x11)
                byte[] command = new byte[20];
                command[0] = 0x01; // command_id
                command[1] = 0x11; // key
                for (int i = 2; i < 18; i++) {
                    command[i] = 0x00; // 填充 0
                }
                writeChar.setValue(command);
                gatt.writeCharacteristic(writeChar);
                Log.d("BLE", "sendBatteryRequest "+writeChar.getValue());
            }
        }
    }

    private void parseBatteryData(byte[] data) {
        if (data.length < 20) {
            Log.e("BLE", "数据长度不足: " + data.length);
            return;
        }
        byte commandId = data[0];
        byte key = data[1];
        if (commandId == 0x01 && key == 0x11) {
            // 解析电池状态
            int adcValue = ((data[2] & 0xFF) << 24) | ((data[3] & 0xFF) << 16) |
                    ((data[4] & 0xFF) << 8) | (data[5] & 0xFF);
            byte batStatus = data[6];
            byte batLevel = data[7];
            byte batPercent = data[8];

            String statusText;
            switch (batStatus) {
                case 0: statusText = "正常"; break;
                case 1: statusText = "充电"; break;
                case 2: statusText = "充满"; break;
                case 3: statusText = "低电"; break;
                case 4: statusText = "异常"; break;
                default: statusText = "未知"; break;
            }

            String message = String.format("电池状态: %s\n电池格数: %d\n电量百分比: %d%%\nADC值: %d",
                    statusText, batLevel, batPercent, adcValue);
            Log.d("Battery", message);
            runOnUiThread(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }
}
package com.example.ttai.utils

import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 回响蓝牙设备通信协议工具类
 * 基于 usertext.txt 协议规范实现
 */
object BluetoothProtocolUtils {
    
    private const val TAG = "BluetoothProtocolUtils"
    
    // ==================== 设备信息常量 ====================
    const val SERVICE_UUID_180A = "180A"
    
    // 特征UUID
    const val CHARACTERISTIC_MANUFACTURER = "2A29"
    const val CHARACTERISTIC_HARDWARE_REVISION = "2A27"
    const val CHARACTERISTIC_PNP_ID = "2A50"
    const val CHARACTERISTIC_WRITE = "FE01"
    const val CHARACTERISTIC_NOTIFY = "FE02"
    
    // ==================== 命令定义 ====================
    object Commands {
        const val GET_COMMAND = 0x01
        const val SET_COMMAND = 0x02
        const val NOTIFY_COMMAND = 0x03
    }
    
    // ==================== 获取命令的Key定义 ====================
    object GetKeys {
        const val DEVICE_INFO = 0x01
        const val BATTERY_STATUS = 0x11
        const val RUNNING_STATUS = 0x12
    }
    
    // ==================== 设置命令的Key定义 ====================
    object SetKeys {
        const val MANUAL_SETTING = 0x01
        const val RUNNING_MODE = 0x02
    }
    
    // ==================== 通知命令的Key定义 ====================
    object NotifyKeys {
        const val DEVICE_STATUS = 0x01
        const val BATTERY_STATUS = 0x11
        const val RUNNING_STATUS = 0x12
    }
    
    // ==================== 电池状态定义 ====================
    object BatteryStatus {
        const val NORMAL = 0
        const val CHARGING = 1
        const val FULL = 2
        const val LOW = 3
        const val ABNORMAL = 4
    }
    
    // ==================== 运行状态定义 ====================
    object RunningStatus {
        const val MANUAL_MODE = 0x01
        const val PATTERN_MODE = 0x02
    }
    
    // ==================== 数据模型 ====================
    
    /**
     * 设备基本信息
     */
    data class DeviceInfo(
        val deviceId: Int,
        val firmwareVersion: String,
        val buildDate: String,
        val manufacturerId: Int,
        val handshakeStatus: Int,
        val deviceAddress: String
    )
    
    /**
     * 电池状态信息
     */
    data class BatteryInfo(
        val adcValue: Int,
        val batteryStatus: Int,
        val batteryLevel: Int,
        val batteryPercent: Int
    )
    
    /**
     * 运行状态信息
     */
    data class RunningInfo(
        val runningStatus: Int,
        val runningMode: Int,
        val suctionIntensity: Int,
        val vibrationIntensity: Int
    )
    
    /**
     * 设置结果
     */
    data class SettingResult(
        val success: Boolean,
        val commandId: Int,
        val key: Int
    )
    
    // ==================== 数据包构建方法 ====================
    
    /**
     * 构建数据包
     * @param commandId 命令ID
     * @param key 键值
     * @param value 数据值（18字节）
     * @return 完整的数据包字节数组
     */
    fun buildPacket(commandId: Int, key: Int, value: ByteArray = ByteArray(18)): ByteArray {
        require(value.size == 18) { "Value must be exactly 18 bytes" }
        
        val packet = ByteArray(20)
        packet[0] = commandId.toByte()
        packet[1] = key.toByte()
        System.arraycopy(value, 0, packet, 2, 18)
        
        Log.d(TAG, "Built packet: ${packet.toHexString()}")
        return packet
    }
    
    /**
     * 构建获取设备基本信息的数据包
     */
    fun buildGetDeviceInfoPacket(): ByteArray {
        return buildPacket(Commands.GET_COMMAND, GetKeys.DEVICE_INFO)
    }
    
    /**
     * 构建获取电池状态的数据包
     */
    fun buildGetBatteryStatusPacket(): ByteArray {
        return buildPacket(Commands.GET_COMMAND, GetKeys.BATTERY_STATUS)
    }
    
    /**
     * 构建获取运行状态的数据包
     */
    fun buildGetRunningStatusPacket(): ByteArray {
        return buildPacket(Commands.GET_COMMAND, GetKeys.RUNNING_STATUS)
    }
    
    /**
     * 构建手动设置的数据包
     * @param suctionIntensity 吮吸强度 (0-100)
     * @param vibrationIntensity 震动强度 (0-100)
     */
    fun buildManualSettingPacket(suctionIntensity: Int, vibrationIntensity: Int): ByteArray {
        require(suctionIntensity in 0..100) { "Suction intensity must be 0-100" }
        require(vibrationIntensity in 0..100) { "Vibration intensity must be 0-100" }
        
        val value = ByteArray(18)
        value[0] = suctionIntensity.toByte()
        value[1] = vibrationIntensity.toByte()
        // 其余16字节保持为0
        
        return buildPacket(Commands.SET_COMMAND, SetKeys.MANUAL_SETTING, value)
    }
    
    /**
     * 构建设置运行模式的数据包
     * @param mode 震动模式 (0-20, 0为待机)
     */
    fun buildSetRunningModePacket(mode: Int): ByteArray {
        require(mode in 0..20) { "Mode must be 0-20" }
        
        val value = ByteArray(18)
        value[0] = mode.toByte()
        // 其余17字节保持为0
        
        return buildPacket(Commands.SET_COMMAND, SetKeys.RUNNING_MODE, value)
    }
    
    // ==================== 数据包解析方法 ====================
    
    /**
     * 解析数据包
     * @param data 原始数据
     * @return 解析结果
     */
    fun parsePacket(data: ByteArray): PacketParseResult? {
        if (data.size < 20) {
            Log.e(TAG, "Invalid packet size: ${data.size}")
            return null
        }
        
        val commandId = data[0].toInt() and 0xFF
        val key = data[1].toInt() and 0xFF
        val value = data.copyOfRange(2, 20)
        
        Log.d(TAG, "Parsing packet: commandId=0x${commandId.toString(16)}, key=0x${key.toString(16)}")
        
        return when (commandId) {
            Commands.GET_COMMAND -> parseGetCommand(key, value)
            Commands.SET_COMMAND -> parseSetCommand(key, value)
            Commands.NOTIFY_COMMAND -> parseNotifyCommand(key, value)
            else -> {
                Log.w(TAG, "Unknown command ID: 0x${commandId.toString(16)}")
                null
            }
        }
    }
    
    /**
     * 解析获取命令响应
     */
    private fun parseGetCommand(key: Int, value: ByteArray): PacketParseResult {
        return when (key) {
            GetKeys.DEVICE_INFO -> {
                val deviceInfo = parseDeviceInfo(value)
                PacketParseResult.DeviceInfo(deviceInfo)
            }
            GetKeys.BATTERY_STATUS -> {
                val batteryInfo = parseBatteryInfo(value)
                PacketParseResult.BatteryInfo(batteryInfo)
            }
            GetKeys.RUNNING_STATUS -> {
                val runningInfo = parseRunningInfo(value)
                PacketParseResult.RunningInfo(runningInfo)
            }
            else -> {
                Log.w(TAG, "Unknown get key: 0x${key.toString(16)}")
                PacketParseResult.Unknown
            }
        }
    }
    
    /**
     * 解析设置命令响应
     */
    private fun parseSetCommand(key: Int, value: ByteArray): PacketParseResult {
        val success = value[0] != 0.toByte()
        val settingResult = SettingResult(success, Commands.SET_COMMAND, key)
        return PacketParseResult.SettingResult(settingResult)
    }
    
    /**
     * 解析通知命令
     */
    private fun parseNotifyCommand(key: Int, value: ByteArray): PacketParseResult {
        return when (key) {
            NotifyKeys.DEVICE_STATUS -> {
                val deviceInfo = parseDeviceInfo(value)
                PacketParseResult.DeviceInfo(deviceInfo)
            }
            NotifyKeys.BATTERY_STATUS -> {
                val batteryInfo = parseBatteryInfo(value)
                PacketParseResult.BatteryInfo(batteryInfo)
            }
            NotifyKeys.RUNNING_STATUS -> {
                val runningInfo = parseRunningInfo(value)
                PacketParseResult.RunningInfo(runningInfo)
            }
            else -> {
                Log.w(TAG, "Unknown notify key: 0x${key.toString(16)}")
                PacketParseResult.Unknown
            }
        }
    }
    
    /**
     * 解析设备信息
     */
    private fun parseDeviceInfo(value: ByteArray): DeviceInfo {
        val buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
        
        val deviceId = buffer.int
        val firmwareVersion = parseFirmwareVersion(value, 4)
        val buildDate = parseBuildDate(value, 8)
        val manufacturerId = buffer.get(10).toInt() and 0xFF
        val handshakeStatus = buffer.get(11).toInt() and 0xFF
        val deviceAddress = parseMacAddress(value, 12)
        
        return DeviceInfo(deviceId, firmwareVersion, buildDate, manufacturerId, handshakeStatus, deviceAddress)
    }
    
    /**
     * 解析电池信息
     */
    private fun parseBatteryInfo(value: ByteArray): BatteryInfo {
        val buffer = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN)
        
        val adcValue = buffer.int
        val batteryStatus = buffer.get(4).toInt() and 0xFF
        val batteryLevel = buffer.get(5).toInt() and 0xFF
        val batteryPercent = buffer.get(6).toInt() and 0xFF
        
        return BatteryInfo(adcValue, batteryStatus, batteryLevel, batteryPercent)
    }
    
    /**
     * 解析运行信息
     */
    private fun parseRunningInfo(value: ByteArray): RunningInfo {
        val runningStatus = value[0].toInt() and 0xFF
        val runningMode = value[1].toInt() and 0xFF
        val suctionIntensity = value[2].toInt() and 0xFF
        val vibrationIntensity = value[3].toInt() and 0xFF
        
        return RunningInfo(runningStatus, runningMode, suctionIntensity, vibrationIntensity)
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 解析固件版本
     */
    private fun parseFirmwareVersion(value: ByteArray, offset: Int): String {
        val versionBytes = value.copyOfRange(offset, offset + 4)
        return "V${versionBytes[0]}.${versionBytes[1]}.${versionBytes[2]}"
    }
    
    /**
     * 解析编译日期
     */
    private fun parseBuildDate(value: ByteArray, offset: Int): String {
        val dateBytes = value.copyOfRange(offset, offset + 2)
        return "${dateBytes[0]}-${dateBytes[1]}"
    }
    
    /**
     * 解析MAC地址
     */
    private fun parseMacAddress(value: ByteArray, offset: Int): String {
        val macBytes = value.copyOfRange(offset, offset + 6)
        return macBytes.joinToString(":") { "%02X".format(it) }
    }
    
    /**
     * 获取电池状态描述
     */
    fun getBatteryStatusDescription(status: Int): String {
        return when (status) {
            BatteryStatus.NORMAL -> "正常"
            BatteryStatus.CHARGING -> "充电中"
            BatteryStatus.FULL -> "已充满"
            BatteryStatus.LOW -> "电量低"
            BatteryStatus.ABNORMAL -> "异常"
            else -> "未知状态"
        }
    }
    
    /**
     * 获取运行状态描述
     */
    fun getRunningStatusDescription(status: Int): String {
        return when (status) {
            RunningStatus.MANUAL_MODE -> "手动设置"
            RunningStatus.PATTERN_MODE -> "模式运行"
            else -> "未知状态"
        }
    }
    

    
    /**
     * 字节数组转十六进制字符串
     */
    private fun ByteArray.toHexString(): String {
        return joinToString(" ") { "%02X".format(it) }
    }
    
    // ==================== 解析结果密封类 ====================
    sealed class PacketParseResult {
        data class DeviceInfo(val info: BluetoothProtocolUtils.DeviceInfo) : PacketParseResult()
        data class BatteryInfo(val info: BluetoothProtocolUtils.BatteryInfo) : PacketParseResult()
        data class RunningInfo(val info: BluetoothProtocolUtils.RunningInfo) : PacketParseResult()
        data class SettingResult(val result: BluetoothProtocolUtils.SettingResult) : PacketParseResult()
        object Unknown : PacketParseResult()
    }
}

package com.example.ttai.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID


object CommontUtils {

    /**
     * 电话号码脱敏处理
     * 将中间4位用*代替 0ec9c13d-29ce-3ef8-a0ac-e083a1b7846d
     * @param phone 原始电话号码
     * @return 脱敏后的电话号码
     */
    fun maskPhoneNumber(phone: String): String {
        if (phone.isBlank() || phone.length < 7) {
            return phone
        }

        // 中国手机号通常是11位，格式为：1xx xxxx xxxx
        // 我们将中间4位用*代替
        return try {
            val prefix = phone.substring(0, 3) // 前3位
            val suffix = phone.substring(7) // 后4位
            "$prefix****$suffix"
        } catch (e: Exception) {
            phone // 如果处理失败，返回原始号码
        }
    }

    /**
     * 校验中国手机号码格式
     * @param phone 手机号码
     * @return 是否为有效的手机号码
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        if (phone.isBlank()) {
            return false
        }
        
        // 移除所有空格和特殊字符
        val cleanPhone = phone.replace(Regex("[\\s\\-\\(\\)]"), "")
        
        // 中国手机号规则：
        // 1. 必须是11位数字
        // 2. 必须以1开头
        // 3. 第二位必须是3、4、5、6、7、8、9中的一个
        val phoneRegex = Regex("^1[3-9]\\d{9}$")
        
        return phoneRegex.matches(cleanPhone)
    }

    /**
     * 获取真实的设备ID
     * 优先使用ANDROID_ID，如果不可用则使用其他标识符
     * @param context 上下文
     * @return 设备唯一标识符
     */
    fun getRealDeviceId(context: Context): String {
        return try {
            // 优先使用ANDROID_ID
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            
            if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
                // 9774d56d682e549c 是某些设备的默认ANDROID_ID，需要避免使用
                return androidId
            }
            
            // 如果ANDROID_ID不可用，使用设备信息组合生成
            val deviceInfo = StringBuilder().apply {
                append(Build.MANUFACTURER)
                append(Build.MODEL)
                append(Build.PRODUCT)
                append(Build.DEVICE)
                append(Build.BOARD)
                append(Build.BRAND)
                append(Build.HARDWARE)
                append(Build.SERIAL)
            }.toString()
            
            // 使用SHA-256生成唯一标识符
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val hash = messageDigest.digest(deviceInfo.toByteArray())
            val hexString = StringBuilder()
            
            for (byte in hash) {
                val hex = Integer.toHexString(0xff and byte.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            
            hexString.toString()
        } catch (e: Exception) {
            // 如果所有方法都失败，使用UUID作为后备方案
            UUID.randomUUID().toString()
        }
    }

    fun getExpiryTime(time: Long?): String {
        return time?.let { expiry ->
            try {
                val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                "到期时间:${outputFormat.format(expiry.times(1000))}"
            } catch (e: Exception) {
                "到期时间:未知"
            }
        } ?: "到期时间:未知"
    }

    /**
     * 格式化时间戳为距离当前时间的描述
     * @param time 以秒为单位的时间戳
     * @return 距离当前时间的描述
     */
    fun formatTime(time: Long?): String {
        return time?.let { timestamp ->
            try {
                // 将秒转换为毫秒
                val timestampMillis = timestamp * 1000
                val now = System.currentTimeMillis()
                val diffInMillis = now - timestampMillis

                when {
                    diffInMillis < 0 -> "刚刚" // 如果时间戳在未来，显示"刚刚"
                    diffInMillis < 60 * 1000 -> "刚刚" // 1分钟内
                    diffInMillis < 60 * 60 * 1000 -> "${diffInMillis / (60 * 1000)}分钟前"
                    diffInMillis < 24 * 60 * 60 * 1000 -> "${diffInMillis / (60 * 60 * 1000)}小时前"
                    diffInMillis < 30 * 24 * 60 * 60 * 1000L -> "${diffInMillis / (24 * 60 * 60 * 1000)}天前"
                    diffInMillis < 365 * 24 * 60 * 60 * 1000L -> "${diffInMillis / (30 * 24 * 60 * 60 * 1000)}个月前"
                    else -> "${diffInMillis / (365 * 24 * 60 * 60 * 1000)}年前"
                }
            } catch (e: Exception) {
                "未知时间"
            }
        } ?: "未知时间"
    }

    /**
     * 新增方法: 十六进制字符串列表转换为 byte[]（支持 "0x" 前缀）
     * 示例: ["0x02", "0x02", "0x02", "0x00"] -> [0x02, 0x02, 0x02, 0x00]
     * @param hexList 十六进制字符串列表
     * @return byte[] 字节数组
     */
    fun convertHexToBytes(hexList: List<String>?): ByteArray {
        if (hexList == null || hexList.isEmpty()) {
            return byteArrayOf(0x02, 0x02, 0x00, 0x00)
        }
        val bytes = ByteArray(hexList.size)
        for (i in hexList.indices) {
            var hexStr = hexList.get(i)
            if (hexStr.startsWith("0x") || hexStr.startsWith("0X")) {
                hexStr = hexStr.substring(2) // 移除 "0x" 前缀
            }
            val value = hexStr.toInt(16)
            bytes[i] = value.toByte() // 转换为 byte（自动符号扩展）
        }
        return bytes
    }
}
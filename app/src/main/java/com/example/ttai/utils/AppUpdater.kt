package com.example.ttai.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.app.NotificationChannel
import android.app.NotificationManager

object AppUpdater {

    private var downloadId: Long = -1

    /**
     * 发起下载请求
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startDownload(context: Context, url: String) {
        // 使用时间戳防止文件名重复冲突
        val fileName = "update_${System.currentTimeMillis()}.apk"

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("正在更新 TTAI")
            setDescription("下载中...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // 使用公共目录，不要用私有目录
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

            setMimeType("application/vnd.android.package-archive")
        }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        try {
            downloadId = manager.enqueue(request)
            // --- 关键：在这里注册广播，监听下载完成 ---
            val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        // 下载完成，立即检查状态并安装
                        checkDownloadStatus(ctx, id)
                        // 安装指令发出后，注销本广播，防止重复触发
                        ctx.unregisterReceiver(this)
                    }
                }
            }

            // 适配 Android 14+ 的导出标识
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
            } else {
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    intentFilter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            }

        } catch (e: Exception) {
            // 兜底：如果 OSS 报错或 Manager 挂了，直接走浏览器
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    @SuppressLint("Range")
    private fun checkDownloadStatus(context: Context, id: Long) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(id)
        val cursor = manager.query(query)

        if (cursor != null && cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                val fileUri = manager.getUriForDownloadedFile(id)

                // 重点：判断 App 是否在后台
                if (isAppInForeground(context)) {
                    // 在前台，直接弹安装
                    installApk(context, fileUri)
                } else {
                    // 在后台，系统会拦截弹窗。
                    // 此时建议：弹出一个全屏通知或者只是静静等待用户点击通知栏。
                    // 也可以强制发一个我们自己的通知
                    showInstallNotification(context, fileUri)
                }
            }
            cursor.close()
        }
    }
    /**
     * 发送一个安装通知
     */
    fun showInstallNotification(context: Context, fileUri: Uri) {
        val channelId = "app_update_channel"
        val notificationId = 1001
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. 创建通知渠道 (适配 Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "应用更新",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "点击安装下载完成的新版本"
            }
            manager.createNotificationChannel(channel)
        }

        // 2. 创建安装 Intent
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // 3. 将 Intent 包装成 PendingIntent
        // 适配 Android 12+ 必须指定 FLAG_IMMUTABLE 或 FLAG_MUTABLE
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. 构建通知
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // 替换为你自己的 App 图标
            .setContentTitle("下载完成")
            .setContentText("最新版本已下载，点击立即安装")
            .setAutoCancel(true)        // 点击后通知自动消失
            .setOngoing(false)          // 允许滑动删除
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // 设置点击事件
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // 5. 发送通知
        manager.notify(notificationId, builder.build())
    }
    // 判断应用是否在前台
    private fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        return appProcesses.any { it.processName == context.packageName && it.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
    }
    /**
     * 安装 APK
     */
    fun installApk(context: Context, uri: Uri?) {
        if (uri == null) return

        // 1. 处理 Android 8.0 未知来源安装权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        // 2. 发起安装 Intent
        val intent = Intent(Intent.ACTION_VIEW).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 非常重要
            setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(intent)
    }
}
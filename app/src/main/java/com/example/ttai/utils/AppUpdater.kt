package com.example.ttai.utils

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.ttai.utils.DebugUtils

object AppUpdater {

    private const val PREFS_NAME = "app_updater"
    private const val KEY_PENDING_DOWNLOAD_ID = "pending_download_id"
    private const val INSTALL_CHANNEL_ID = "app_update_channel"
    private const val INSTALL_NOTIFICATION_ID = 1001

    /**
     * 发起下载请求
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startDownload(context: Context, url: String) {
        val appContext = context.applicationContext
        val fileName = "update_${System.currentTimeMillis()}.apk"

        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle("正在更新 TTAI")
            setDescription("下载中...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
        }

        val manager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        try {
            val downloadId = manager.enqueue(request)
            savePendingDownloadId(appContext, downloadId)
            DebugUtils.logInfo("AppUpdater", "开始下载 APK, id=$downloadId")
        } catch (e: Exception) {
            DebugUtils.logError("AppUpdater", "DownloadManager 下载失败，跳转浏览器", e)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            appContext.startActivity(intent)
        }
    }

    fun getPendingDownloadId(context: Context): Long {
        return context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_PENDING_DOWNLOAD_ID, -1L)
    }

    fun clearPendingDownloadId(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_PENDING_DOWNLOAD_ID)
            .apply()
    }

    private fun savePendingDownloadId(context: Context, downloadId: Long) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_PENDING_DOWNLOAD_ID, downloadId)
            .apply()
    }

    fun onDownloadComplete(context: Context, downloadId: Long) {
        checkDownloadStatus(context.applicationContext, downloadId)
    }

    @SuppressLint("Range")
    private fun checkDownloadStatus(context: Context, downloadId: Long) {
        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        manager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) {
                DebugUtils.logWarning("AppUpdater", "未找到下载记录: $downloadId")
                return
            }
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex < 0) return
            val status = cursor.getInt(statusIndex)
            if (status != DownloadManager.STATUS_SUCCESSFUL) {
                DebugUtils.logWarning("AppUpdater", "下载未成功, status=$status")
                return
            }
        }

        val apkUri = manager.getUriForDownloadedFile(downloadId)
        if (apkUri == null) {
            DebugUtils.logWarning("AppUpdater", "无法获取已下载 APK 的 Uri")
            return
        }

        handleDownloadedApk(context, apkUri)
    }

    private fun handleDownloadedApk(context: Context, apkUri: Uri) {
        if (!tryInstallApk(context, apkUri)) {
            showInstallNotification(context, apkUri)
        }
    }

    /**
     * 尝试拉起系统安装界面。后台时部分机型会拦截 Activity 启动，返回 false 后走通知兜底。
     */
    private fun tryInstallApk(context: Context, uri: Uri): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                DebugUtils.logWarning("AppUpdater", "缺少未知来源安装权限")
                return false
            }
        }

        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            DebugUtils.logError("AppUpdater", "后台拉起安装页失败，将显示通知", e)
            false
        }
    }

    fun showInstallNotification(context: Context, fileUri: Uri) {
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                appContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                DebugUtils.logWarning("AppUpdater", "无通知权限，无法展示安装通知")
                return
            }
        }

        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                INSTALL_CHANNEL_ID,
                "应用更新",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "新版本下载完成，点击安装"
            }
            manager.createNotificationChannel(channel)
        }

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            downloadIdToRequestCode(fileUri),
            installIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(appContext, INSTALL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("下载完成")
            .setContentText("新版本已就绪，点击安装")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        manager.notify(INSTALL_NOTIFICATION_ID, notification)
    }

    private fun downloadIdToRequestCode(uri: Uri): Int = uri.hashCode()

    /**
     * 安装 APK（供外部直接调用）
     */
    fun installApk(context: Context, uri: Uri?) {
        if (uri == null) return
        if (!tryInstallApk(context.applicationContext, uri)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !context.packageManager.canRequestPackageInstalls()
            ) {
                val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
            } else {
                showInstallNotification(context, uri)
            }
        }
    }
}

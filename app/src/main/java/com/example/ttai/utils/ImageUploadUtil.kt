package com.example.ttai.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 图片上传工具类
 */
object ImageUploadUtil {
    
    /**
     * 从URI创建MultipartBody.Part
     * @param context 上下文
     * @param uri 图片URI
     * @param partName 参数名称，默认为"file"
     * @return MultipartBody.Part
     */
    fun createImagePart(context: Context, uri: Uri, partName: String = "file"): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                android.util.Log.e("ImageUploadUtil", "无法打开输入流")
                return null
            }

            // 创建临时文件
            val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadUtil", "创建图片部分失败", e)
            null
        }
    }
    
    /**
     * 从Bitmap创建MultipartBody.Part
     * @param context 上下文
     * @param bitmap 图片Bitmap
     * @param partName 参数名称，默认为"file"
     * @param quality 压缩质量，默认80
     * @return MultipartBody.Part
     */
    fun createImagePart(
        context: Context, 
        bitmap: Bitmap, 
        partName: String = "file",
        quality: Int = 80
    ): MultipartBody.Part? {
        return try {
            // 创建临时文件
            val tempFile = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            
            // 压缩Bitmap到文件
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.close()

            val requestBody = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, tempFile.name, requestBody)
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadUtil", "从Bitmap创建图片部分失败", e)
            null
        }
    }
    
    /**
     * 检查图片文件大小
     * @param context 上下文
     * @param uri 图片URI
     * @param maxSizeInMB 最大大小（MB），默认10MB
     * @return 是否符合大小要求
     */
    fun checkImageSize(context: Context, uri: Uri, maxSizeInMB: Int = 10): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val size = inputStream?.available() ?: 0
            inputStream?.close()
            
            val sizeInMB = size / (1024 * 1024)
            sizeInMB <= maxSizeInMB
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadUtil", "检查图片大小失败", e)
            false
        }
    }
    
    /**
     * 检查图片格式
     * @param context 上下文
     * @param uri 图片URI
     * @return 是否是支持的格式
     */
    fun checkImageFormat(context: Context, uri: Uri): Boolean {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            mimeType in listOf(
                "image/png",
                "image/jpg", 
                "image/jpeg",
                "image/gif",
                "image/webp",
                "image/bmp"
            )
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadUtil", "检查图片格式失败", e)
            false
        }
    }
    
    /**
     * 清理临时文件
     * @param context 上下文
     */
    fun cleanTempFiles(context: Context) {
        try {
            val cacheDir = context.cacheDir
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("temp_upload_")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ImageUploadUtil", "清理临时文件失败", e)
        }
    }
}

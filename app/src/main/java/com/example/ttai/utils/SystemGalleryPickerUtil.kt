package com.example.ttai.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * 系统相册选择工具类
 * 直接调用系统相册界面
 */
class SystemGalleryPickerUtil(
    private val context: Context,
    private val onImageSelected: (Bitmap) -> Unit
) {
    
    // 系统相册选择结果处理
    private var galleryLauncher: ActivityResultLauncher<Intent>? = null
    
    /**
     * 初始化 Activity 版本的启动器
     */
    fun initForActivity(activity: FragmentActivity) {
        galleryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data = result.data
                val selectedImageUri = data?.data
                handleGalleryResult(selectedImageUri)
            } else {
                android.util.Log.d("SystemGalleryPickerUtil", "用户取消了图片选择")
            }
        }
    }
    
    /**
     * 初始化 Fragment 版本的启动器
     */
    fun initForFragment(fragment: Fragment) {
        galleryLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                val data = result.data
                val selectedImageUri = data?.data
                handleGalleryResult(selectedImageUri)
            } else {
                android.util.Log.d("SystemGalleryPickerUtil", "用户取消了图片选择")
            }
        }
    }
    
    /**
     * 处理相册选择结果
     */
    private fun handleGalleryResult(uri: android.net.Uri?) {
        if (uri == null) {
            ToastUtils.showShort(context, "未选择图片")
            return
        }
        
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                ToastUtils.showShort(context, "无法读取图片文件")
                return
            }
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap != null) {
                android.util.Log.d("SystemGalleryPickerUtil", "成功加载图片: ${bitmap.width}x${bitmap.height}")
                onImageSelected(bitmap)
            } else {
                ToastUtils.showShort(context, "无法加载图片，请选择其他图片")
            }
        } catch (e: OutOfMemoryError) {
            android.util.Log.e("SystemGalleryPickerUtil", "内存不足，图片太大", e)
            ToastUtils.showShort(context, "图片太大，请选择较小的图片")
        } catch (e: Exception) {
            android.util.Log.e("SystemGalleryPickerUtil", "图片加载失败", e)
            ToastUtils.showShort(context, "图片加载失败: ${e.message}")
        }
    }
    
    /**
     * 打开系统相册
     */
    fun openSystemGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            galleryLauncher?.launch(intent)
            android.util.Log.d("SystemGalleryPickerUtil", "打开系统相册")
        } catch (e: Exception) {
            android.util.Log.e("SystemGalleryPickerUtil", "打开系统相册失败", e)
            ToastUtils.showShort(context, "打开相册失败，请重试")
        }
    }
    
    companion object {
        /**
         * 为 Activity 创建 SystemGalleryPickerUtil 实例
         */
        fun createForActivity(
            activity: FragmentActivity,
            onImageSelected: (Bitmap) -> Unit
        ): SystemGalleryPickerUtil {
            val picker = SystemGalleryPickerUtil(activity, onImageSelected)
            picker.initForActivity(activity)
            return picker
        }
        
        /**
         * 为 Fragment 创建 SystemGalleryPickerUtil 实例
         */
        fun createForFragment(
            fragment: Fragment,
            onImageSelected: (Bitmap) -> Unit
        ): SystemGalleryPickerUtil {
            val picker = SystemGalleryPickerUtil(fragment.requireContext(), onImageSelected)
            picker.initForFragment(fragment)
            return picker
        }
    }
}

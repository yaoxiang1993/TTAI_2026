package com.example.ttai.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.io.IOException

/**
 * 专门用于从相册选择图片的工具类
 * 只支持相册选择，不支持拍照
 */
class GalleryPickerUtil(
    private val context: Context,
    private val onImageSelected: (Bitmap) -> Unit
) {
    
    // 相册选择结果处理
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    
    /**
     * 初始化 Activity 版本的启动器
     */
    fun initForActivity(activity: FragmentActivity) {
        // 相册结果处理 - 使用 GetContent 来选择图片
        galleryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            handleGalleryResult(uri)
        }
    }
    
    /**
     * 初始化 Fragment 版本的启动器
     */
    fun initForFragment(fragment: Fragment) {
        // 相册结果处理 - 使用 GetContent 来选择图片
        galleryLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            handleGalleryResult(uri)
        }
    }
    
    /**
     * 处理相册选择结果
     */
    private fun handleGalleryResult(uri: android.net.Uri?) {
        if (uri == null) {
            // 用户取消了选择
            android.util.Log.d("GalleryPickerUtil", "用户取消了图片选择")
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
                android.util.Log.d("GalleryPickerUtil", "成功加载图片: ${bitmap.width}x${bitmap.height}")
                onImageSelected(bitmap)
            } else {
                ToastUtils.showShort(context, "无法加载图片，请选择其他图片")
            }
        } catch (e: OutOfMemoryError) {
            android.util.Log.e("GalleryPickerUtil", "内存不足，图片太大", e)
            ToastUtils.showShort(context, "图片太大，请选择较小的图片")
        } catch (e: Exception) {
            android.util.Log.e("GalleryPickerUtil", "图片加载失败", e)
            ToastUtils.showShort(context, "图片加载失败: ${e.message}")
        }
    }
    
    /**
     * 打开相册选择
     */
    fun openGallery() {
        try {
            galleryLauncher?.launch("image/*")
            android.util.Log.d("GalleryPickerUtil", "打开相册选择")
        } catch (e: Exception) {
            android.util.Log.e("GalleryPickerUtil", "打开相册失败", e)
            ToastUtils.showShort(context, "打开相册失败，请重试")
        }
    }
    
    companion object {
        /**
         * 为 Activity 创建 GalleryPickerUtil 实例
         */
        fun createForActivity(
            activity: FragmentActivity,
            onImageSelected: (Bitmap) -> Unit
        ): GalleryPickerUtil {
            val galleryPicker = GalleryPickerUtil(activity, onImageSelected)
            galleryPicker.initForActivity(activity)
            return galleryPicker
        }
        
        /**
         * 为 Fragment 创建 GalleryPickerUtil 实例
         */
        fun createForFragment(
            fragment: Fragment,
            onImageSelected: (Bitmap) -> Unit
        ): GalleryPickerUtil {
            val galleryPicker = GalleryPickerUtil(fragment.requireContext(), onImageSelected)
            galleryPicker.initForFragment(fragment)
            return galleryPicker
        }
    }
}

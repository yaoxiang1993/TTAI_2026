package com.example.ttai.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.IOException

/**
 * 统一的图片选择工具类
 * 支持 Activity 和 Fragment 调用
 * 封装相机拍照和相册选择功能
 */
class ImagePickerUtil(
    private val context: Context,
    private val onImageSelected: (Bitmap) -> Unit
) {
    
    private var currentPhotoPath: String? = null
    
    // 相机权限请求
    private var cameraPermissionLauncher: ActivityResultLauncher<String>? = null
    
    // 相册权限请求
    private var galleryPermissionLauncher: ActivityResultLauncher<String>? = null
    
    // 相机结果处理
    private var cameraLauncher: ActivityResultLauncher<android.net.Uri>? = null
    
    // 相册结果处理
    private var galleryLauncher: ActivityResultLauncher<Array<String>>? = null
    
    /**
     * 初始化 Activity 版本的启动器
     */
    fun initForActivity(activity: FragmentActivity) {
        // 相机权限请求
        cameraPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                ToastUtils.showShort(context, "需要相机权限才能拍照")
            }
        }
        
        // 相册权限请求
        galleryPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                ToastUtils.showShort(context, "需要存储权限才能选择照片")
            }
        }
        
        // 相机结果处理
        cameraLauncher = activity.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                currentPhotoPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    onImageSelected(bitmap)
                }
            }
        }
        
        // 相册结果处理 - 使用 OpenDocument 以获得持久权限
        galleryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            handleGalleryResult(uri)
        }
    }
    
    /**
     * 初始化 Fragment 版本的启动器
     */
    fun initForFragment(fragment: Fragment) {
        // 相机权限请求
        cameraPermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                ToastUtils.showShort(context, "需要相机权限才能拍照")
            }
        }
        
        // 相册权限请求
        galleryPermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                ToastUtils.showShort(context, "需要存储权限才能选择照片")
            }
        }
        
        // 相机结果处理
        cameraLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                currentPhotoPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    onImageSelected(bitmap)
                }
            }
        }
        
        // 相册结果处理 - 使用 OpenDocument 以获得持久权限
        galleryLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            handleGalleryResult(uri)
        }
    }
    
    /**
     * 处理相册选择结果
     */
    private fun handleGalleryResult(uri: android.net.Uri?) {
        uri?.let {
            try {
                // 获取持久权限
                context.contentResolver.takePersistableUriPermission(
                    uri, 
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                onImageSelected(bitmap)
                inputStream?.close()
            } catch (e: SecurityException) {
                android.util.Log.w("ImagePickerUtil", "无法获取持久权限，尝试直接读取", e)
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    onImageSelected(bitmap)
                    inputStream?.close()
                } catch (e2: Exception) {
                    android.util.Log.e("ImagePickerUtil", "图片加载失败", e2)
                    ToastUtils.showShort(context, "图片加载失败: ${e2.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("ImagePickerUtil", "图片加载失败", e)
                ToastUtils.showShort(context, "图片加载失败: ${e.message}")
            }
        }
    }
    
    /**
     * 显示图片选择对话框
     */
    fun showImageSelectionDialog() {



//        checkGalleryPermission()
        val options = arrayOf(
            "拍照",
            "从相册选择")
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("选择图片来源")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> checkGalleryPermission()
                }
            }
            .show()
    }
    
    /**
     * 检查相机权限
     */
    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher?.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    /**
     * 检查相册权限
     */
    private fun checkGalleryPermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                galleryPermissionLauncher?.launch(permission)
            }
        }
    }
    
    /**
     * 打开相机
     */
    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            currentPhotoPath = photoFile.absolutePath
            
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            
            cameraLauncher?.launch(photoUri)
        } catch (e: IOException) {
            ToastUtils.showShort(context, "创建图片文件失败")
        }
    }
    
    /**
     * 打开相册
     */
    private fun openGallery() {

        galleryLauncher?.launch(arrayOf("image/*"))
    }
    
    /**
     * 创建图片文件
     */
    private fun createImageFile(): File {
        val timeStamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = context.getExternalFilesDir(null)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    companion object {
        /**
         * 检查是否有相机权限
         */
        fun hasCameraPermission(context: Context): Boolean {
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
        
        /**
         * 检查是否有相册权限
         */
        fun hasGalleryPermission(context: Context): Boolean {
            val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            return ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
        
        /**
         * 为 Activity 创建 ImagePickerUtil 实例
         */
        fun createForActivity(
            activity: FragmentActivity,
            onImageSelected: (Bitmap) -> Unit
        ): ImagePickerUtil {
            val imagePicker = ImagePickerUtil(activity, onImageSelected)
            imagePicker.initForActivity(activity)
            return imagePicker
        }
        
        /**
         * 为 Fragment 创建 ImagePickerUtil 实例
         */
        fun createForFragment(
            fragment: Fragment,
            onImageSelected: (Bitmap) -> Unit
        ): ImagePickerUtil {
            val imagePicker = ImagePickerUtil(fragment.requireContext(), onImageSelected)
            imagePicker.initForFragment(fragment)
            return imagePicker
        }
    }
} 
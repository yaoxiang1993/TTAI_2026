package com.example.ttai.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.ttai.R

/**
 * 安全的Glide图片加载工具类
 * 用于避免MediaDocumentsProvider权限问题
 */
object SafeGlideUtils {
    
    /**
     * 安全地加载图片，避免MediaDocumentsProvider权限问题
     * @param context 上下文
     * @param imageView 目标ImageView
     * @param imageUrl 图片URL
     * @param placeholder 占位图资源ID
     * @param error 错误图资源ID
     * @param applyCircleCrop 是否应用圆形裁剪
     */
    fun loadImageSafely(
        context: Context,
        imageView: ImageView,
        imageUrl: String?,
        applyCircleCrop: Boolean = false
    ) {

        
        try {
            val glideRequest = Glide.with(context)
                .load(imageUrl)
                .transform(
                    CenterCrop() // 铺满并裁剪
                )

            // 应用变换
            if (applyCircleCrop) {
                glideRequest.transform(CenterCrop(), CircleCrop())
            }
            
            glideRequest.into(imageView)
        } catch (e: Exception) {
            android.util.Log.e("SafeGlideUtils", "图片加载失败: $imageUrl", e)
        }
    }

    /**
     * 检查URL是否是安全的（非MediaDocumentsProvider URI）
     */
    fun isUrlSafe(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        return !url.contains("com.android.providers.media.documents") &&
               !url.startsWith("content://com.android.providers.media.documents")
    }
}

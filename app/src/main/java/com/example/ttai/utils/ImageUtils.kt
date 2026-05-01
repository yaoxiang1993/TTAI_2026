package com.example.ttai.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.ttai.R


object ImageUtils {
    fun loadImageToBG(
        context: Context, imageUrl: String?, imageView: ImageView,
        placeholder: Int = R.mipmap.icon_defult_loading_big,
        error: Int = R.mipmap.icon_defult_loading_big
    ) {
        Glide.with(context)
            .load(imageUrl)
            .fitCenter() // 按比例缩放，保持宽高比，居中显示
            .placeholder(placeholder)
            .error(error)
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }
}
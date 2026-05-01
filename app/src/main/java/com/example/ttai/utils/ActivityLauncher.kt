package com.example.ttai.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair

/**
 * Activity启动工具类
 * 封装常见的Activity启动场景，避免重复代码
 */
object ActivityLauncher {

    // ============== 基础启动方法 ==============

    /**
     * 启动普通Activity
     * @param context 上下文，建议使用Application Context
     * @param clazz 目标Activity类
     */
    fun launchActivity(context: Context, clazz: Class<out Activity>) {
        launchActivity(context, clazz, null, null)
    }

    /**
     * 启动Activity并传递Bundle数据
     */
    fun launchActivity(
        context: Context,
        clazz: Class<out Activity>,
        bundle: Bundle? = null,
        options: ActivityOptionsCompat? = null
    ) {
        val intent = Intent(context, clazz)
        bundle?.let { intent.putExtras(it) }
        context.startActivity(intent, options?.toBundle())
    }

    /**
     * 带返回值启动Activity（用于Fragment）
     */
    fun launchForResult(
        fragment: androidx.fragment.app.Fragment,
        clazz: Class<out Activity>,
        requestCode: Int,
        bundle: Bundle? = null
    ) {
        val intent = Intent(fragment.requireContext(), clazz)
        bundle?.let { intent.putExtras(it) }
        fragment.startActivityForResult(intent, requestCode)
    }

    /**
     * 带返回值启动Activity（用于Activity）
     */
    fun launchForResult(
        activity: Activity,
        clazz: Class<out Activity>,
        requestCode: Int,
        bundle: Bundle? = null
    ) {
        val intent = Intent(activity, clazz)
        bundle?.let { intent.putExtras(it) }
        activity.startActivityForResult(intent, requestCode)
    }

    // ============== 带动画启动方法 ==============

    /**
     * 带共享元素动画启动Activity（Material Design风格）
     */
    fun launchWithTransition(
        activity: Activity,
        clazz: Class<out Activity>,
        vararg sharedElements: Pair<View, String>
    ) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            *sharedElements
        )
        launchActivity(activity, clazz, null, options)
    }

    /**
     * 自定义Activity进入/退出动画
     */
    fun launchWithAnimation(
        activity: Activity,
        clazz: Class<out Activity>,
        enterAnim: Int,
        exitAnim: Int
    ) {
        val intent = Intent(activity, clazz)
        activity.startActivity(intent)
        activity.overridePendingTransition(enterAnim, exitAnim)
    }

    // ============== 特定场景启动方法 ==============

    /**
     * 启动登录Activity（示例场景）
     */
//    fun launchLoginActivity(context: Context, needResult: Boolean = false) {
//        if (needResult && context is Activity) {
//            launchForResult(context, LoginActivity::class.java, Constants.REQUEST_LOGIN)
//        } else {
//            launchActivity(context, LoginActivity::class.java)
//        }
//    }

    /**
     * 启动详情页Activity并传递ID
     */
//    fun launchDetailActivity(
//        context: Context,
//        itemId: String,
//        isFromFavorite: Boolean = false
//    ) {
//        val bundle = Bundle().apply {
//            putString(Constants.KEY_ITEM_ID, itemId)
//            putBoolean(Constants.KEY_FROM_FAVORITE, isFromFavorite)
//        }
//        launchActivity(context, DetailActivity::class.java, bundle)
//    }

    // ============== 安全启动方法（避免Context问题） ==============

    /**
     * 安全启动Activity（自动处理Context类型）
     * 当context为Application Context时，自动添加FLAG_ACTIVITY_NEW_TASK
     */
    fun safeLaunchActivity(context: Context, clazz: Class<out Activity>, bundle: Bundle? = null) {
        val intent = Intent(context, clazz).apply {
            bundle?.let { putExtras(it) }
            // 若context为Application Context，必须添加NEW_TASK标志
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }
}
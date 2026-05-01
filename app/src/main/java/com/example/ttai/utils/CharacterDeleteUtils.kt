package com.example.ttai.utils

import android.content.Context
import com.example.ttai.bean.DeleteCharacterData
import com.example.ttai.network.ApiHelper
import com.example.ttai.network.NetworkModule

/**
 * 角色删除工具类
 * 提供角色删除相关的功能
 */
object CharacterDeleteUtils {
    
    /**
     * 删除角色
     * @param context 上下文
     * @param characterId 要删除的角色ID
     * @return 删除结果数据
     */
    suspend fun deleteCharacter(
        context: Context,
        characterId: String
    ): DeleteCharacterData {
        return ApiHelper.executeApiRequest(
            context = context,
            requestName = "删除角色",
            apiCall = {
                NetworkModule.provideApiService().deleteCharacter(characterId)
            }
        )
    }

    /**
     * 处理删除角色的错误响应
     * @param context 上下文
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     */
    fun handleDeleteCharacterError(
        context: Context,
        errorCode: Int,
        errorMessage: String
    ) {
        when (errorCode) {
            400 -> {
                when {
                    errorMessage.contains("无效的角色ID格式") -> {
                        ToastUtils.showShort(context, "角色ID格式无效")
                    }
                    errorMessage.contains("正在被使用中") -> {
                        ToastUtils.showShort(context, "该角色正在被使用中，无法删除。请先结束相关聊天会话。")
                    }
                    else -> {
                        ToastUtils.showShort(context, errorMessage)
                    }
                }
            }
            401 -> {
                ToastUtils.showShort(context, "未提供有效的认证token")
            }
            403 -> {
                ToastUtils.showShort(context, "您只能删除自己创建的角色")
            }
            404 -> {
                ToastUtils.showShort(context, "角色不存在")
            }
            500 -> {
                ToastUtils.showShort(context, "服务器错误，请稍后重试")
            }
            else -> {
                ToastUtils.showShort(context, errorMessage)
            }
        }
    }
    
    /**
     * 显示删除确认对话框
     * @param context 上下文
     * @param characterName 角色名称
     * @param onConfirm 确认删除的回调
     */
//    fun showDeleteConfirmationDialog(
//        context: Context,
//        characterName: String,
//        onConfirm: () -> Unit
//    ) {
//        val dialog = com.example.ttai.ui.dialog.TwoButtonDialogFragment.newInstance(
//            message = "确定要删除角色「$characterName」吗？\n\n删除操作不可逆，请谨慎操作。",
//            positiveText = "确认删除",
//            negativeText = "取消"
//        ).setOnButtonClickListener(object : com.example.ttai.ui.dialog.TwoButtonDialogFragment.OnButtonClickListener {
//            override fun onPositiveClick() {
//                onConfirm()
//            }
//
//            override fun onNegativeClick() {
//                ToastUtils.showShort(context, "已取消删除")
//            }
//        })
//
//        dialog.show((context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager, "DeleteCharacterDialog")
//    }
}

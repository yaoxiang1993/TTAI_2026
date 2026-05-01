package com.example.ttai.utils

import android.content.Context
import com.example.ttai.R

/**
 * 审核状态工具类
 * 用于处理角色审核状态的显示和转换
 */
object ReviewStatusUtils {

    /**
     * 审核状态枚举
     */
    enum class ReviewStatus(val value: String) {
        PENDING("pending"),
        APPROVED("approved"),
        REJECTED("rejected");

        companion object {
            fun fromString(value: String?): ReviewStatus {
                return when (value) {
                    "pending" -> PENDING
                    "approved" -> APPROVED
                    "rejected" -> REJECTED
                    else -> PENDING
                }
            }
        }
    }

    /**
     * 获取审核状态显示文本
     */
    fun getReviewStatusText(context: Context, status: String?): String {
        return when (ReviewStatus.fromString(status)) {
            ReviewStatus.PENDING -> context.getString(R.string.review_status_pending)
            ReviewStatus.APPROVED -> context.getString(R.string.review_status_approved)
            ReviewStatus.REJECTED -> context.getString(R.string.review_status_rejected)
        }
    }

    /**
     * 获取审核状态颜色资源ID
     */
    fun getReviewStatusColor(context: Context, status: String?): Int {
        return when (ReviewStatus.fromString(status)) {
            ReviewStatus.PENDING -> context.getColor(R.color.review_status_pending)
            ReviewStatus.APPROVED -> context.getColor(R.color.review_status_approved)
            ReviewStatus.REJECTED -> context.getColor(R.color.review_status_rejected)
        }
    }

    /**
     * 获取审核状态图标资源ID
     */
    fun getReviewStatusIcon(status: String?): Int {
        return when (ReviewStatus.fromString(status)) {
            ReviewStatus.PENDING -> R.drawable.ic_review_pending
            ReviewStatus.APPROVED -> R.drawable.ic_review_approved
            ReviewStatus.REJECTED -> R.drawable.ic_review_rejected
        }
    }

    /**
     * 检查角色是否已审核通过
     */
    fun isApproved(status: String?): Boolean {
        return ReviewStatus.fromString(status) == ReviewStatus.APPROVED
    }

    /**
     * 检查角色是否正在审核中
     */
    fun isPending(status: String?,isPublic :Boolean?): Boolean {
        return ReviewStatus.fromString(status) == ReviewStatus.PENDING && isPublic == true
    }

    /**
     * 检查角色是否被拒绝
     */
    fun isRejected(status: String?,isPublic :Boolean?): Boolean {
        return ReviewStatus.fromString(status) == ReviewStatus.REJECTED && isPublic == true
    }

    /**
     * 获取审核状态描述
     */
    fun getReviewStatusDescription(context: Context, status: String?, message: String?): String {
        return when (ReviewStatus.fromString(status)) {
            ReviewStatus.PENDING -> context.getString(R.string.review_status_pending_desc)
            ReviewStatus.APPROVED -> context.getString(R.string.review_status_approved_desc)
            ReviewStatus.REJECTED -> {
                if (!message.isNullOrEmpty()) {
                    context.getString(R.string.review_status_rejected_with_reason, message)
                } else {
                    context.getString(R.string.review_status_rejected_desc)
                }
            }
        }
    }
} 
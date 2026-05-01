package com.example.ttai.ui.dialog

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.ttai.R
import com.example.ttai.databinding.DialogAddAiSizeBinding
import com.example.ttai.databinding.DialogChargeMoneyBinding

/**
 * 充值
 * 使用 dialog_add_ai_size 布局样式
 */
class ChargeMoneyDialogFragment : DialogFragment() {

    private var _binding: DialogChargeMoneyBinding? = null
    private val binding get() = _binding!!

    private var onUpgradeClickListener: (() -> Unit)? = null
    private var onCloseClickListener: (() -> Unit)? = null

    companion object {

        fun newInstance( ): ChargeMoneyDialogFragment {
            return ChargeMoneyDialogFragment().apply {

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChargeMoneyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // 设置对话框样式
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // 设置内容
        setText1Context()
        // 设置内容
        setText2Context()
    }

    private fun setText1Context() {
        val text = "小媞模型·每条聊天消耗2仙贝 或2仙玉 "
        val spannable = SpannableString(text)
        // 第一个图片：icon_unit_2，插入到第一个空格
        val firstSpace = text.indexOf(" ")
        val drawable1 = ContextCompat.getDrawable(dialog!!.context, R.mipmap.icon_unit_2)
        drawable1?.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)
        val imageSpan1 = drawable1?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
        if (imageSpan1 != null) {
            spannable.setSpan(imageSpan1, firstSpace, firstSpace + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // 第二个图片：icon_unit_1，插入到第二个空格
        val secondSpace = text.indexOf(" ", firstSpace + 1)
        val drawable2 = ContextCompat.getDrawable(dialog!!.context, R.mipmap.icon_unit_1)
        drawable2?.setBounds(0, 0, drawable2.intrinsicWidth, drawable2.intrinsicHeight)
        val imageSpan2 = drawable2?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
        if (imageSpan2 != null) {
            spannable.setSpan(imageSpan2, secondSpace, secondSpace + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvText1.text = spannable
    }

    private fun setText2Context() {
        val text = "星河模型·每条聊天消耗4仙贝 或4仙玉 "
        val spannable = SpannableString(text)
        // 第一个图片：icon_unit_2，插入到第一个空格
        val firstSpace = text.indexOf(" ")
        val drawable1 = ContextCompat.getDrawable(dialog!!.context, R.mipmap.icon_unit_2)
        drawable1?.setBounds(0, 0, drawable1.intrinsicWidth, drawable1.intrinsicHeight)
        val imageSpan1 = drawable1?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
        if (imageSpan1 != null) {
            spannable.setSpan(imageSpan1, firstSpace, firstSpace + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        // 第二个图片：icon_unit_1，插入到第二个空格
        val secondSpace = text.indexOf(" ", firstSpace + 1)
        val drawable2 = ContextCompat.getDrawable(dialog!!.context, R.mipmap.icon_unit_1)
        drawable2?.setBounds(0, 0, drawable2.intrinsicWidth, drawable2.intrinsicHeight)
        val imageSpan2 = drawable2?.let { ImageSpan(it, ImageSpan.ALIGN_BOTTOM) }
        if (imageSpan2 != null) {
            spannable.setSpan(imageSpan2, secondSpace, secondSpace + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.tvText2.text = spannable
    }

    private fun setupClickListeners() {
        // 关闭按钮点击事件 - 使用右上角的关闭图标
        binding.root.findViewById<View>(R.id.ivBg)?.setOnClickListener {
            onCloseClickListener?.invoke()
            dismiss()
        }

        // 立即提升按钮点击事件 - 使用底部的升级按钮
        binding.root.findViewById<View>(R.id.view)?.setOnClickListener {
            onUpgradeClickListener?.invoke()
            dismiss()
        }
    }

    /**
     * 设置升级点击监听器
     */
    fun setOnUpgradeClickListener(listener: () -> Unit): ChargeMoneyDialogFragment {
        onUpgradeClickListener = listener
        return this
    }

    /**
     * 设置关闭点击监听器
     */
    fun setOnCloseClickListener(listener: () -> Unit): ChargeMoneyDialogFragment {
        onCloseClickListener = listener
        return this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.ttai.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ttai.R
import com.example.ttai.databinding.DialogAddAiSizeBinding

/**
 * AI数量提升对话框
 * 使用 dialog_add_ai_size 布局样式
 */
class AddAISizeDialogFragment : DialogFragment() {

    private var _binding: DialogAddAiSizeBinding? = null
    private val binding get() = _binding!!

    private var onUpgradeClickListener: (() -> Unit)? = null
    private var onCloseClickListener: (() -> Unit)? = null

    companion object {
        private const val ARG_COST = "cost"
        private const val ARG_MESSAGE = "message"
        private const val ARG_DESCRIPTION = "description"

        /**
         * 创建对话框实例
         * @param cost 升级所需金币数量
         * @param message 标题消息
         * @param description 描述文本
         * @return AddAISizeDialogFragment实例
         */
        fun newInstance(
            cost: Int = 1000,
            message: String = "提升AI数量上限",
            description: String = "固定AI数量为5个，如需提升数量需消耗金币～"
        ): AddAISizeDialogFragment {
            return AddAISizeDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COST, cost)
                    putString(ARG_MESSAGE, message)
                    putString(ARG_DESCRIPTION, description)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddAiSizeBinding.inflate(inflater, container, false)
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
        arguments?.let { args ->
            val cost = args.getInt(ARG_COST, 1000)
            val message = args.getString(ARG_MESSAGE, "提升AI数量上限")
            val description = args.getString(ARG_DESCRIPTION, "固定AI数量为5个，如需提升数量需消耗金币～")

            binding.tvMessage.text = message
            binding.tvText.text = description
            binding.tvCoast.text = cost.toString()
        }
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
    fun setOnUpgradeClickListener(listener: () -> Unit): AddAISizeDialogFragment {
        onUpgradeClickListener = listener
        return this
    }

    /**
     * 设置关闭点击监听器
     */
    fun setOnCloseClickListener(listener: () -> Unit): AddAISizeDialogFragment {
        onCloseClickListener = listener
        return this
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    override fun onStart() {
//        super.onStart()
//        // 设置对话框宽度，距离屏幕左右两边10dp
//        dialog?.window?.let { window ->
//            val displayMetrics = resources.displayMetrics
//            val width = displayMetrics.widthPixels - (40 * resources.displayMetrics.density).toInt()
//            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
//        }
//    }
}
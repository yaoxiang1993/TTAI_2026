package com.example.ttai.ui.dialog


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ttai.R
import com.example.ttai.databinding.DialogTwoButtonBinding
import com.example.ttai.databinding.DialogUpdateBinding


class UpdateDialogFragment : DialogFragment() {

    // 回调接口
    interface OnButtonClickListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }

    private var _binding: DialogUpdateBinding? = null
    private val binding get() = _binding!!
    private var listener: OnButtonClickListener? = null

    // 数据参数
    private var message: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null

    // 设置回调
    fun setOnButtonClickListener(listener: OnButtonClickListener): UpdateDialogFragment {
        this.listener = listener
        return this
    }

    // 设置参数的静态方法
    companion object {
        fun newInstance(
            message: String,
            positiveText: String = "下载更新",
            negativeText: String = "暂时不更新"
        ): UpdateDialogFragment {
            return UpdateDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                    putString("positiveText", positiveText)
                    putString("negativeText", negativeText)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 获取参数
        arguments?.let {
            message = it.getString("message")
            positiveText = it.getString("positiveText")
            negativeText = it.getString("negativeText")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUpdateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // 设置对话框宽度，距离屏幕左右两边10dp
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val width = displayMetrics.widthPixels - (40 * resources.displayMetrics.density).toInt()
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 设置消息
        binding.tvMessage.text = message

        // 设置按钮文本
        binding.btnPositive.text = positiveText
        binding.btnNegative.text = negativeText

        // 按钮点击事件
        binding.btnPositive.setOnClickListener {
            listener?.onPositiveClick()
            dismiss()
        }
        binding.btnNegative.setOnClickListener {
            listener?.onNegativeClick()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
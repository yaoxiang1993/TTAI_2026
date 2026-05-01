package com.example.ttai.ui.dialog


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ttai.R
import com.example.ttai.databinding.DialogOneButtonBinding
import com.example.ttai.databinding.DialogTwoButtonBinding


class OneButtonDialogFragment : DialogFragment() {

    // 回调接口
    interface OnButtonClickListener {
        fun onPositiveClick()
    }

    private var _binding: DialogOneButtonBinding? = null
    private val binding get() = _binding!!
    private var listener: OnButtonClickListener? = null

    // 数据参数
    private var message: String? = null
    private var positiveText: String? = null

    // 设置回调
    fun setOnButtonClickListener(listener: OnButtonClickListener): OneButtonDialogFragment {
        this.listener = listener
        return this
    }

    // 设置参数的静态方法
    companion object {
        fun newInstance(
            message: String,
            positiveText: String = "好的"
        ): OneButtonDialogFragment {
            return OneButtonDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("message", message)
                    putString("positiveText", positiveText)
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
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOneButtonBinding.inflate(inflater, container, false)
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

        // 按钮点击事件
        binding.btnPositive.setOnClickListener {
            listener?.onPositiveClick()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
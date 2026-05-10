package com.example.ttai.ui.dialog


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.ttai.databinding.DialogBlindBoxBinding


class BlindBoxDialogFragment : DialogFragment() {

    // 回调接口
    interface OnButtonClickListener {
        fun onPositiveClick(data: String? ="")
        fun onNegativeClick()
    }

    private var _binding: DialogBlindBoxBinding? = null
    private val binding get() = _binding!!
    private var listener: OnButtonClickListener? = null

    private var rewardJade: Int? = 0

    // 设置回调
    fun setOnButtonClickListener(listener: OnButtonClickListener): BlindBoxDialogFragment {
        this.listener = listener
        return this
    }

    // 设置参数的静态方法
    companion object {
        fun newInstance(
            rewardJade: Int = 0,
        ): BlindBoxDialogFragment {
            return BlindBoxDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt("rewardJade", rewardJade)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取参数
        arguments?.let {
            rewardJade = it.getInt("rewardJade")
        }
        // 设置消息
        binding.tvMessage.text = "盲盒开出了${rewardJade}仙玉~"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBlindBoxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivClose.setOnClickListener {
            listener?.onNegativeClick()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

}
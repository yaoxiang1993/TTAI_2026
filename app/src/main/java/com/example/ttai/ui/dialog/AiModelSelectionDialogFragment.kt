package com.example.ttai.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.databinding.DialogAiModelSelectionBinding
import com.example.ttai.adapter.AiModelAdapter
import com.example.ttai.bean.AiModel

class AiModelSelectionDialogFragment : DialogFragment() {

    // 回调接口
    interface OnModelSelectedListener {
        fun onModelSelected(model: AiModel)
    }

    private var _binding: DialogAiModelSelectionBinding? = null
    private val binding get() = _binding!!
    private var modelSelectedListener: OnModelSelectedListener? = null
    private lateinit var aiModelAdapter: AiModelAdapter

    // 数据参数
    private var title: String? = null
    private var message: String? = null
    private var models: List<AiModel> = emptyList()

    // 设置回调
    fun setOnModelSelectedListener(listener: OnModelSelectedListener): AiModelSelectionDialogFragment {
        this.modelSelectedListener = listener
        return this
    }

    // 设置参数的静态方法
    companion object {
        fun newInstance(
            title: String? = null,
            message: String? = null,
            models: List<AiModel> = emptyList()
        ): AiModelSelectionDialogFragment {
            return AiModelSelectionDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("message", message)
                }
                this.models = models
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取参数
        arguments?.let {
            title = it.getString("title")
            message = it.getString("message")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAiModelSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupRecyclerView()
        setupClickListeners()
    }

    override fun onStart() {
        super.onStart()
        
        // 设置对话框宽度为屏幕的3/4
        dialog?.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val dialogWidth = (screenWidth * 0.75).toInt()
            
            window.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setupViews() {
        // 设置标题和消息
        title?.let { binding.tvTitle.text = it }
        message?.let { binding.tvMessage.text = it }
    }

    private fun setupRecyclerView() {
        aiModelAdapter = AiModelAdapter { model ->
            // 模型被点击时的处理 - 这里不需要做任何事情，因为选择是通过确认按钮完成的
        }

        binding.rvModels.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = aiModelAdapter
        }

        // 提交数据
        aiModelAdapter.submitList(models)
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnConfirm.setOnClickListener {
            val selectedModel = aiModelAdapter.getSelectedModel()
            if (selectedModel != null) {
                modelSelectedListener?.onModelSelected(selectedModel)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
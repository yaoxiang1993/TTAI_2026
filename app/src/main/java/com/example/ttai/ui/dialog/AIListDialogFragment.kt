package com.example.ttai.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ttai.databinding.DialogAiListBinding
import com.example.ttai.adapter.AIReplaceAdapter
import com.example.ttai.bean.Conversation


class AIListDialogFragment : DialogFragment() {

    // 回调接口
    interface OnButtonClickListener {
        fun onPositiveClick()
        fun onNegativeClick()
    }
    
    // 替换回调接口
    interface OnReplaceClickListener {
        fun onReplaceClick(conversation: Conversation)
    }
    
    // 确认替换回调接口
    interface OnConfirmReplaceListener {
        fun onConfirmReplace(conversation: Conversation?)
    }

    private var _binding: DialogAiListBinding? = null
    private val binding get() = _binding!!
    private var listener: OnButtonClickListener? = null
    private var replaceListener: OnReplaceClickListener? = null
    private var confirmReplaceListener: OnConfirmReplaceListener? = null
    private lateinit var aiReplaceAdapter: AIReplaceAdapter

    // 数据参数
    private var title: String? = null
    private var message: String? = null
    private var positiveText: String? = null
    private var negativeText: String? = null
    private var conversations: List<Conversation> = emptyList()

    // 设置回调
    fun setOnButtonClickListener(listener: OnButtonClickListener): AIListDialogFragment {
        this.listener = listener
        return this
    }
    
    // 设置替换回调
    fun setOnReplaceClickListener(listener: OnReplaceClickListener): AIListDialogFragment {
        this.replaceListener = listener
        return this
    }
    
    // 设置确认替换回调
    fun setOnConfirmReplaceListener(listener: OnConfirmReplaceListener): AIListDialogFragment {
        this.confirmReplaceListener = listener
        return this
    }
    
    // 设置会话数据
    fun setConversations(conversations: List<Conversation>): AIListDialogFragment {
        this.conversations = conversations
        return this
    }

    // 设置参数的静态方法
    companion object {
        fun newInstance(
            title: String? = null,
            message: String,
            positiveText: String = "确定",
            negativeText: String = "取消",
            conversations: List<Conversation> = emptyList()
        ): AIListDialogFragment {
            return AIListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putString("message", message)
                    putString("positiveText", positiveText)
                    putString("negativeText", negativeText)
                }
                this.conversations = conversations
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取参数
        arguments?.let {
            title = it.getString("title")
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
        _binding = DialogAiListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 设置消息
        binding.tvMessage.text = message

        // 设置按钮文本
        binding.btnPositive.text = positiveText
        binding.btnNegative.text = negativeText

        // 初始化RecyclerView
        setupRecyclerView()

        // 按钮点击事件
        binding.btnPositive.setOnClickListener {
            // 获取选中的会话
            val selectedConversation = aiReplaceAdapter.getSelectedConversation()
            // 调用确认替换回调
            confirmReplaceListener?.onConfirmReplace(selectedConversation)
            dismiss()
            // 如果有选中的会话，才调用按钮点击回调和关闭对话框
            if (selectedConversation != null) {
                listener?.onPositiveClick()
            }
            // 如果没有选中会话，不关闭对话框，让回调处理提示信息
        }

        binding.btnNegative.setOnClickListener {
            dismiss()
            listener?.onNegativeClick()
        }
    }
    
    private fun setupRecyclerView() {
        aiReplaceAdapter = AIReplaceAdapter { conversation ->
            replaceListener?.onReplaceClick(conversation)
        }
        
        binding.mRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = aiReplaceAdapter
            
            // 添加 item 间隔装饰器，设置 5dp 间隔
            addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: android.graphics.Rect,
                    view: View,
                    parent: androidx.recyclerview.widget.RecyclerView,
                    state: androidx.recyclerview.widget.RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    
                    // 设置 item 之间的垂直间隔为 5dp
                    val spacing = (5 * resources.displayMetrics.density).toInt()
                    outRect.top = spacing
                    outRect.bottom = spacing
                }
            })
        }
        
        // 设置数据
        aiReplaceAdapter.submitList(conversations)
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
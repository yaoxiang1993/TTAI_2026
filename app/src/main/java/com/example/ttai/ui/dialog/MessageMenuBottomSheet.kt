package com.example.ttai.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class MessageMenuBottomSheet : BottomSheetDialogFragment() {
//
//    private lateinit var message: Message
//    private var position: Int = 0
//
//    companion object {
//        fun newInstance(message: Message, position: Int): MessageMenuBottomSheet {
//            val fragment = MessageMenuBottomSheet()
//            val args = Bundle().apply {
//                putParcelable("message", message)
//                putInt("position", position)
//            }
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            message = it.getParcelable("message")!!
//            position = it.getInt("position")
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_message_menu, container, false)
//
//        // 绑定菜单项点击事件
//        view.findViewById<MaterialButton>(R.id.btn_copy)?.setOnClickListener { onCopy(message) }
//        view.findViewById<MaterialButton>(R.id.btn_delete)?.setOnClickListener { onDelete(message, position) }
//        view.findViewById<MaterialButton>(R.id.btn_forward)?.setOnClickListener { onForward(message) }
//        view.findViewById<MaterialButton>(R.id.btn_multi_select)?.setOnClickListener { onMultiSelect(message, position) }
//
//        return view
//    }
//
//    private fun onCopy(message: Message) {
//        // 实现复制逻辑，例如使用 ClipboardManager
//        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//        val clip = ClipData.newPlainText("message", message.content)
//        clipboard.setPrimaryClip(clip)
//        dismiss() // 关闭菜单
//        // 可选：Toast "已复制"
//    }
//
//    private fun onDelete(message: Message, position: Int) {
//        // 删除消息逻辑，例如更新 Adapter 数据
//        messages.removeAt(position) // 假设 messages 是你的数据源
//        notifyItemRemoved(position)
//        dismiss()
//        // 可选：确认对话框
//    }
//
//    private fun onForward(message: Message) {
//        // 转发逻辑，例如打开转发页面
//        // startActivity(Intent(context, ForwardActivity::class.java).apply { putExtra("message", message) })
//        dismiss()
//    }
//
//    private fun onMultiSelect(message: Message, position: Int) {
//        // 多选模式逻辑，例如切换到多选状态
//        // chatFragment.enterMultiSelectMode(position)
//        dismiss()
//    }
//
//    override fun getTheme(): Int {
//        return R.style.ThemeOverlay_MaterialComponents_BottomSheetDialog  // 自定义主题以匹配微信风格
//    }
}
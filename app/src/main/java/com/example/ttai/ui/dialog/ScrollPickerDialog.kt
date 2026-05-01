package com.example.ttai.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.ttai.R
import com.example.ttai.myenum.WorkMode

class ScrollPickerDialog(
    private val onConfirm: (WorkMode) -> Unit, // 回调返回整个枚举对象
    private val onCancel: (() -> Unit)? = null
) : DialogFragment() {

    private val modes = WorkMode.values()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_scroll_picker, container, false)
    }
    override fun onStart() {
        super.onStart()

        dialog?.window?.let { window ->
            // 获取屏幕参数
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels

            // 计算 80% 的宽度
            val targetWidth = (screenWidth * 0.8).toInt()

            // 设置窗口属性
            window.setLayout(
                targetWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT // 高度自适应内容
            )

            // 可选：如果你想让对话框背景透明（去掉默认的边距感）
            // window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        // 配置 NumberPicker 使用枚举中的文案
        numberPicker.minValue = 0
        numberPicker.maxValue = modes.size - 1
        numberPicker.displayedValues = WorkMode.getLabels()
        numberPicker.wrapSelectorWheel = false
        btnConfirm.setOnClickListener {
            // 根据索引获取对应的枚举标识
            val selectedMode = modes[numberPicker.value]
            onConfirm(selectedMode)
            dismiss()
        }

        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
    }
}
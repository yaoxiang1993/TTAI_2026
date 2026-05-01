package com.example.ttai.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import com.example.ttai.utils.dpToPx
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Calendar

/**
 * 年月选择器 DialogFragment。
 * @param onDateSelected 回调接口，当用户选择年月后调用。
 */
class YearMonthPickerDialogFragment(
    private val onDateSelected: (year: Int, month: Int) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1

        val minYear = currentYear - 10
        val maxYear = currentYear + 10
        val months = arrayOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")

        val yearPicker = NumberPicker(requireContext()).apply {
            minValue = minYear
            maxValue = maxYear
            value = currentYear
        }

        val monthPicker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = months.size - 1
            value = currentMonth - 1
            displayedValues = months
        }

        // 组合布局：年和月并排
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            setPadding(48.dpToPx(requireContext()), 24.dpToPx(requireContext()), 48.dpToPx(requireContext()), 24.dpToPx(requireContext()))
            weightSum = 2f
            addView(yearPicker, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            addView(monthPicker, android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("选择年月")
            .setView(layout)
            .setPositiveButton("确认") { _, _ ->
                val selectedYear = yearPicker.value
                val selectedMonth = monthPicker.value + 1
                onDateSelected(selectedYear, selectedMonth)
            }
            .setNegativeButton("取消", null)
            .create()
    }

    // 为了正确显示两个 Picker，需要自定义布局
    // 下面是完整版，使用 LinearLayout 组合
    companion object {
        fun newInstance(onDateSelected: (year: Int, month: Int) -> Unit): YearMonthPickerDialogFragment {
            return YearMonthPickerDialogFragment(onDateSelected)
        }
    }
}
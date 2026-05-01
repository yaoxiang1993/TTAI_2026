package com.example.ttai.myenum

enum class WorkMode(val label: String, val id: Int) {
    SUCTION("仅吸吮", 101),
    VIBRATION("仅震动", 102),
    BOTH("两端", 103);

    // 辅助方法：获取所有文案数组供 Picker 使用
    companion object {
        fun getLabels() = values().map { it.label }.toTypedArray()
    }
}
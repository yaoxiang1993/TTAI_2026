package com.example.ttai.utils

class ItemMapper {
    // 伴生对象，使其方法和属性可以直接通过类名 ItemMapper 访问，类似 Java 中的静态方法。
    companion object {
        private const val DEFAULT_NAME = "未知物品"

        // 使用 'lazy' 延迟初始化 Map，只在第一次访问时创建，提高效率。
        // Map 是 private 的，确保数据不会被外部意外修改。
        private val fairyItemsMap: Map<String, String> by lazy {
            mapOf(
                "fairy_jade" to "仙玉",
                "fairy_shells" to "仙贝"
                // 可以在这里添加更多条目
            )
        }

        /**
         * 根据输入的英文参数获取对应的中文名称。
         *
         * @param parameter 您传入的英文参数 (例如 "fairy_jade")。
         * @return 对应的中文名称，如果找不到则返回 "未知物品"。
         */
        fun getItemName(parameter: String?): String {
            // 对传入的参数进行非空检查和空白字符检查，增强代码健壮性
            if (parameter.isNullOrBlank()) {
                return DEFAULT_NAME
            }

            // 使用 getOrDefault() 获取值，如果键不存在，则返回预定义的默认值。
            return fairyItemsMap.getOrDefault(parameter, DEFAULT_NAME)
        }
    }
}
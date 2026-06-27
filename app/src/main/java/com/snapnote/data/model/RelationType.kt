package com.snapnote.data.model

enum class RelationType(val label: String, val icon: String) {
    SUBORDINATE("从属关系", "subdirectory_arrow_right"),
    PREREQUISITE("前置基础", "arrow_upward"),
    CONFUSION("易混淆", "warning"),
    CAUSAL("因果推导", "trending_flat"),
    RELATED("相关", "link"),
    SIMILAR("相似", "compare_arrows");

    companion object {
        fun fromString(value: String): RelationType {
            return entries.find { it.name == value } ?: RELATED
        }
    }
}

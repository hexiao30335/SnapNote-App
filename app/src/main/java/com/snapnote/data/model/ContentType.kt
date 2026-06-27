package com.snapnote.data.model

enum class ContentType(val label: String) {
    THEORY("理论知识点"),
    EXAMPLE("例题"),
    MISTAKE("错题"),
    FORMULA("公式"),
    CONCEPT("概念定义"),
    UNKNOWN("未分类");

    companion object {
        fun fromString(value: String): ContentType {
            return entries.find { it.name == value } ?: UNKNOWN
        }
    }
}

package com.snapnote.ai

import com.snapnote.data.model.ContentType

/**
 * AI 内容分类器：根据 OCR 文本判断内容类型
 */
class ContentClassifier {

    fun classify(text: String): ContentType {
        val lower = text.trim()
        return when {
            lower.contains("例题") || lower.contains("例") && lower.contains("解") -> ContentType.EXAMPLE
            lower.contains("易错") || lower.contains("错误") || lower.contains("错解") -> ContentType.MISTAKE
            lower.contains("公式") || lower.contains("定理") || containsFormula(lower) -> ContentType.FORMULA
            lower.contains("概念") || lower.contains("定义") || lower.contains("是指") -> ContentType.CONCEPT
            lower.contains("知识点") || lower.contains("原理") || lower.contains("性质") -> ContentType.THEORY
            else -> ContentType.THEORY
        }
    }

    private fun containsFormula(text: String): Boolean {
        val formulaPatterns = listOf(
            "=", "+", "-", "*", "^", "√", "∫", "∑", "∂",
            "\\frac", "\\sqrt", "\\sum", "\\int",
            "F=", "E=", "v=", "a=", "s=", "h="
        )
        return formulaPatterns.any { text.contains(it) }
    }

    fun extractTitle(text: String): String {
        val lines = text.lines().filter { it.isNotBlank() }
        val firstLine = lines.firstOrNull() ?: "未命名知识点"
        return when {
            firstLine.startsWith("第") && firstLine.contains("章") -> firstLine
            firstLine.matches(Regex("^\\d+\\.\\d+.*")) -> firstLine
            firstLine.length > 30 -> firstLine.take(30) + "..."
            else -> firstLine
        }
    }
}

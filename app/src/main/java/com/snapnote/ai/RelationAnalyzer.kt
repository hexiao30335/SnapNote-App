package com.snapnote.ai

import com.snapnote.data.model.ContentType
import com.snapnote.data.model.ExtractedKnowledgePoint
import com.snapnote.data.model.KnowledgePoint
import com.snapnote.data.model.RelationType
import com.snapnote.data.model.SuggestedRelation

/**
 * 知识关联分析器：分析知识点之间的逻辑关系
 */
class RelationAnalyzer {

    data class RelationSuggestion(
        val sourceTitle: String,
        val targetTitle: String,
        val relationType: RelationType,
        val strength: String,
        val reason: String
    )

    /**
     * 分析新知识点与已有知识点之间的关系
     */
    fun analyzeRelations(
        newPoints: List<ExtractedKnowledgePoint>,
        existingPoints: List<KnowledgePoint>
    ): List<RelationSuggestion> {
        val suggestions = mutableListOf<RelationSuggestion>()

        for (newPoint in newPoints) {
            for (existing in existingPoints) {
                val relation = detectRelation(newPoint, existing)
                if (relation != null) {
                    suggestions.add(relation)
                }
            }
        }

        // 分析新知识点之间的内部关系
        for (i in newPoints.indices) {
            for (j in i + 1 until newPoints.size) {
                val relation = detectInternalRelation(newPoints[i], newPoints[j])
                if (relation != null) {
                    suggestions.add(relation)
                }
            }
        }

        return suggestions
    }

    private fun detectRelation(
        newPoint: ExtractedKnowledgePoint,
        existing: KnowledgePoint
    ): RelationSuggestion? {
        val newText = (newPoint.title + " " + newPoint.content).lowercase()
        val existingText = (existing.title + " " + existing.content).lowercase()

        // 从属关系检测
        if (isSubordinate(newText, existingText)) {
            return RelationSuggestion(
                sourceTitle = existing.title,
                targetTitle = newPoint.title,
                relationType = RelationType.SUBORDINATE,
                strength = "strong",
                reason = "${newPoint.title} 是 ${existing.title} 的子知识点"
            )
        }

        // 前置基础检测
        if (isPrerequisite(existingText, newText)) {
            return RelationSuggestion(
                sourceTitle = existing.title,
                targetTitle = newPoint.title,
                relationType = RelationType.PREREQUISITE,
                strength = "strong",
                reason = "需要先掌握 ${existing.title} 才能理解 ${newPoint.title}"
            )
        }

        // 易混淆检测
        if (isConfusable(newText, existingText)) {
            return RelationSuggestion(
                sourceTitle = existing.title,
                targetTitle = newPoint.title,
                relationType = RelationType.CONFUSION,
                strength = "medium",
                reason = "两个知识点概念相近，容易混淆"
            )
        }

        // 因果推导检测
        if (isCausal(newText, existingText)) {
            return RelationSuggestion(
                sourceTitle = existing.title,
                targetTitle = newPoint.title,
                relationType = RelationType.CAUSAL,
                strength = "medium",
                reason = "${existing.title} 可以推导出 ${newPoint.title}"
            )
        }

        return null
    }

    private fun detectInternalRelation(
        point1: ExtractedKnowledgePoint,
        point2: ExtractedKnowledgePoint
    ): RelationSuggestion? {
        val text1 = (point1.title + " " + point1.content).lowercase()
        val text2 = (point2.title + " " + point2.content).lowercase()

        if (isSubordinate(text2, text1)) {
            return RelationSuggestion(
                sourceTitle = point1.title,
                targetTitle = point2.title,
                relationType = RelationType.SUBORDINATE,
                strength = "strong",
                reason = "层级包含关系"
            )
        }

        return null
    }

    private fun isSubordinate(child: String, parent: String): Boolean {
        val parentKeywords = extractKeywords(parent)
        val childKeywords = extractKeywords(child)
        return parentKeywords.any { pk ->
            childKeywords.any { ck -> ck.contains(pk) && ck != pk }
        } && child.length < parent.length * 0.8
    }

    private fun isPrerequisite(base: String, advanced: String): Boolean {
        val prereqKeywords = listOf("概念", "定义", "基础", "初速度", "位移")
        val hasPrereq = prereqKeywords.any { base.contains(it) }
        val hasAdvanced = advanced.contains("公式") || advanced.contains("推导") || advanced.contains("例题")
        return hasPrereq && hasAdvanced
    }

    private fun isConfusable(text1: String, text2: String): Boolean {
        val keywords1 = extractKeywords(text1)
        val keywords2 = extractKeywords(text2)
        val common = keywords1.intersect(keywords2.toSet())
        val similarity = common.size.toFloat() / maxOf(keywords1.size, keywords2.size)
        return similarity > 0.4 && similarity < 0.9
    }

    private fun isCausal(source: String, target: String): Boolean {
        val causalPatterns = listOf("推导", "得出", "所以", "因此", "推出")
        return causalPatterns.any { target.contains(it) }
    }

    private fun extractKeywords(text: String): List<String> {
        return text.split(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]+"))
            .filter { it.length >= 2 }
            .distinct()
    }
}

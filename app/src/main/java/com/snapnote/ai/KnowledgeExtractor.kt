package com.snapnote.ai

import com.snapnote.data.model.ContentType
import com.snapnote.data.model.ExtractedKnowledgePoint
import com.snapnote.data.model.OcrResult
import com.snapnote.data.model.ScanResult
import com.snapnote.data.model.ScanStatus
import com.snapnote.data.model.SuggestedRelation
import android.net.Uri

/**
 * 知识点拆解引擎：将 OCR 文本拆分为最小独立知识点单元
 * 内置去重逻辑，避免导入重复截图时产生重复知识点
 */
class KnowledgeExtractor(
    private val ocrEngine: OcrEngine,
    private val classifier: ContentClassifier
) {

    suspend fun processImage(uri: Uri, fileName: String): ScanResult {
        val ocrResult = ocrEngine.recognize(uri)

        if (ocrResult.text.isBlank()) {
            return ScanResult(
                uri = uri,
                fileName = fileName,
                ocrText = "",
                extractedKnowledgePoints = emptyList(),
                status = ScanStatus.FAILED
            )
        }

        val points = extractKnowledgePoints(ocrResult)
        return ScanResult(
            uri = uri,
            fileName = fileName,
            ocrText = ocrResult.text,
            extractedKnowledgePoints = points,
            status = ScanStatus.COMPLETED
        )
    }

    fun extractKnowledgePoints(ocrResult: OcrResult): List<ExtractedKnowledgePoint> {
        val text = ocrResult.text
        val sections = splitIntoSections(text)

        return sections.flatMap { section ->
            val type = classifier.classify(section)
            val title = classifier.extractTitle(section)

            if (type == ContentType.THEORY && section.length > 200) {
                val subPoints = splitLargeSection(section)
                subPoints.mapIndexed { index, sub ->
                    ExtractedKnowledgePoint(
                        title = if (index == 0) title else "$title (${index + 1})",
                        content = sub,
                        contentType = type,
                        suggestedRelations = suggestRelations(sub, type)
                    )
                }
            } else {
                listOf(
                    ExtractedKnowledgePoint(
                        title = title,
                        content = section,
                        contentType = type,
                        suggestedRelations = suggestRelations(section, type)
                    )
                )
            }
        }.mapIndexed { index, point ->
            point.copy(title = "#${String.format("%02d", index + 1)} ${point.title}")
        }
    }

    private fun splitIntoSections(text: String): List<String> {
        val chapterPattern = Regex("^(第[一二三四五六七八九十\\d]+章|\\d+\\.\\d+.*)$", RegexOption.MULTILINE)
        val sections = mutableListOf<String>()
        val matches = chapterPattern.findAll(text).toList()

        if (matches.isEmpty()) {
            return text.split("\n\n").filter { it.isNotBlank() }
        }

        for (i in matches.indices) {
            val start = matches[i].range.first
            val end = if (i + 1 < matches.size) matches[i + 1].range.first else text.length
            sections.add(text.substring(start, end).trim())
        }

        return sections
    }

    private fun splitLargeSection(section: String): List<String> {
        val subSections = section.split(Regex("\n(?=\\d+\\.\\d+)"))
        return if (subSections.size > 1) {
            subSections.filter { it.isNotBlank() }
        } else {
            section.split("\n\n").filter { it.isNotBlank() }
        }
    }

    private fun suggestRelations(text: String, type: ContentType): List<SuggestedRelation> {
        val suggestions = mutableListOf<SuggestedRelation>()

        when {
            text.contains("位移") && text.contains("速度") -> {
                suggestions.add(SuggestedRelation("速度公式", com.snapnote.data.model.RelationType.PREREQUISITE, "strong"))
            }
            text.contains("加速度") -> {
                suggestions.add(SuggestedRelation("速度概念", com.snapnote.data.model.RelationType.PREREQUISITE, "strong"))
            }
            text.contains("匀变速") -> {
                suggestions.add(SuggestedRelation("匀速直线运动", com.snapnote.data.model.RelationType.CONFUSION, "medium"))
            }
            text.contains("自由落体") -> {
                suggestions.add(SuggestedRelation("匀变速直线运动", com.snapnote.data.model.RelationType.SUBORDINATE, "strong"))
            }
        }

        return suggestions
    }

    /**
     * 计算两个文本的相似度（0.0 ~ 1.0）
     * 使用简单的字符级 Jaccard 相似度 + 关键词重合度
     */
    fun calculateSimilarity(text1: String, text2: String): Float {
        if (text1.isBlank() || text2.isBlank()) return 0f

        // 提取关键词
        val keywords1 = extractKeywords(text1)
        val keywords2 = extractKeywords(text2)

        if (keywords1.isEmpty() || keywords2.isEmpty()) return 0f

        // Jaccard 相似度
        val intersection = keywords1.intersect(keywords2.toSet())
        val union = keywords1.union(keywords2.toSet())
        val jaccard = if (union.isNotEmpty()) intersection.size.toFloat() / union.size else 0f

        // 长度比例惩罚（长度差距太大则降低相似度）
        val lengthRatio = minOf(text1.length, text2.length).toFloat() / maxOf(text1.length, text2.length)

        return (jaccard * 0.7f + lengthRatio * 0.3f)
    }

    /**
     * 判断新知识点是否与已有知识点重复
     * 阈值 0.6 以上认为是重复
     */
    fun isDuplicate(
        newPoint: ExtractedKnowledgePoint,
        existingPoint: com.snapnote.data.model.KnowledgePoint
    ): Boolean {
        val newTitle = newPoint.title.removePrefix(Regex("^#\\d{2}\\s*"))
        val existTitle = existingPoint.title.removePrefix(Regex("^#\\d{2}\\s*"))

        // 标题完全包含匹配
        if (newTitle.contains(existTitle) || existTitle.contains(newTitle)) return true
        if (newTitle == existTitle) return true

        // 内容相似度检测
        val contentSimilarity = calculateSimilarity(newPoint.content, existingPoint.content)
        if (contentSimilarity > 0.6f) return true

        // 标题相似度检测
        val titleSimilarity = calculateSimilarity(newTitle, existTitle)
        if (titleSimilarity > 0.7f) return true

        return false
    }

    private fun extractKeywords(text: String): Set<String> {
        return text
            .split(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]+"))
            .filter { it.length >= 2 }
            .toSet()
    }
}

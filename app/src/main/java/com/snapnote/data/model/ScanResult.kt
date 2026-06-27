package com.snapnote.data.model

import android.net.Uri

data class ScanResult(
    val uri: Uri,
    val fileName: String,
    val ocrText: String,
    val extractedKnowledgePoints: List<ExtractedKnowledgePoint>,
    val status: ScanStatus
)

enum class ScanStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class ExtractedKnowledgePoint(
    val title: String,
    val content: String,
    val contentType: ContentType,
    val suggestedRelations: List<SuggestedRelation> = emptyList()
)

data class SuggestedRelation(
    val targetTitle: String,
    val relationType: RelationType,
    val strength: String = "medium"
)

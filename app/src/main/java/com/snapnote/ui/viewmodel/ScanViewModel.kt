package com.snapnote.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapnote.ai.ContentClassifier
import com.snapnote.ai.KnowledgeExtractor
import com.snapnote.ai.LocalOcrEngine
import com.snapnote.ai.RelationAnalyzer
import com.snapnote.data.model.ExtractedKnowledgePoint
import com.snapnote.data.model.KnowledgePoint
import com.snapnote.data.model.Note
import com.snapnote.data.model.RelationType
import com.snapnote.data.model.ScanResult
import com.snapnote.data.model.ScanStatus
import com.snapnote.data.repository.KnowledgeRepository
import com.snapnote.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScanViewModel(context: Context) : ViewModel() {
    private val noteRepository = NoteRepository(context)
    private val knowledgeRepository = KnowledgeRepository(context)

    private val ocrEngine = LocalOcrEngine(context)
    private val classifier = ContentClassifier()
    private val extractor = KnowledgeExtractor(ocrEngine, classifier)
    private val relationAnalyzer = RelationAnalyzer()

    private val _scanResults = mutableStateListOf<ScanResult>()
    val scanResults: List<ScanResult> get() = _scanResults

    private val _isProcessing = mutableStateOf(false)
    val isProcessing: State<Boolean> = _isProcessing

    private val _currentNoteId = mutableStateOf<Long?>(null)
    val currentNoteId: State<Long?> = _currentNoteId

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _selectedImages = mutableStateListOf<Uri>()
    val selectedImages: List<Uri> get() = _selectedImages

    private val _noteTitle = mutableStateOf("")
    val noteTitle: State<String> = _noteTitle

    init {
        viewModelScope.launch {
            noteRepository.getAllNotes().collect { list ->
                _notes.value = list
            }
        }
    }

    fun setNoteTitle(title: String) {
        _noteTitle.value = title
    }

    fun selectImages(uris: List<Uri>) {
        _selectedImages.clear()
        _selectedImages.addAll(uris)
    }

    fun clearSelection() {
        _selectedImages.clear()
        _scanResults.clear()
        _noteTitle.value = ""
    }

    fun createNoteAndScan(context: Context) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val noteId = noteRepository.createNote(
                    title = _noteTitle.value.ifBlank { "新笔记 ${System.currentTimeMillis()}" }
                )
                _currentNoteId.value = noteId

                val imageUris = _selectedImages.map { it.toString() }
                noteRepository.addImagesToNote(noteId, imageUris)

                for (uri in _selectedImages) {
                    val result = extractor.processImage(uri, uri.lastPathSegment ?: "image")
                    _scanResults.add(result)

                    if (result.status == ScanStatus.COMPLETED) {
                        saveExtractedKnowledgePoints(noteId, result.extractedKnowledgePoints)
                    }
                }

                // 保存 AI 分析的关联关系到数据库
                saveAnalyzedRelations(noteId)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun addImagesToExistingNote(context: Context, noteId: Long) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                val imageUris = _selectedImages.map { it.toString() }
                noteRepository.addImagesToNote(noteId, imageUris)

                for (uri in _selectedImages) {
                    val result = extractor.processImage(uri, uri.lastPathSegment ?: "image")
                    _scanResults.add(result)

                    if (result.status == ScanStatus.COMPLETED) {
                        saveExtractedKnowledgePoints(noteId, result.extractedKnowledgePoints)
                    }
                }

                // 保存增量分析的关联关系
                saveAnalyzedRelations(noteId)
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * 保存提取的知识点，内置去重逻辑
     * - 重复知识点：跳过不创建
     * - 相似知识点：补充到已有知识点
     * - 新知识点：正常创建并绑定来源截图
     */
    private suspend fun saveExtractedKnowledgePoints(noteId: Long, points: List<ExtractedKnowledgePoint>) {
        val existingKps = knowledgeRepository.getKnowledgePointsByNote(noteId).first()
        var counter = existingKps.size + 1

        for (point in points) {
            // 使用去重引擎检测是否重复
            val duplicate = existingKps.find { existing ->
                extractor.isDuplicate(point, existing)
            }

            when {
                duplicate != null -> {
                    // 重复知识点：跳过，不创建新的
                    // 如果新内容有额外细节，可以追加补充（但不会创建新记录）
                }
                else -> {
                    // 新知识点：创建并绑定 imageSourceId
                    val newPoint = KnowledgePoint(
                        noteId = noteId,
                        number = String.format("%02d", counter++),
                        title = point.title,
                        content = point.content,
                        contentType = point.contentType
                    )
                    val newId = knowledgeRepository.addKnowledgePoint(newPoint)

                    // 保存建议的关联关系
                    for (suggested in point.suggestedRelations) {
                        val target = existingKps.find {
                            it.title.contains(suggested.targetTitle) || suggested.targetTitle.contains(it.title)
                        }
                        if (target != null) {
                            knowledgeRepository.createRelation(
                                sourceId = newId,
                                targetId = target.id,
                                relationType = suggested.relationType,
                                strength = suggested.strength,
                                isAuto = true
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 保存 RelationAnalyzer 分析的跨知识点关联
     */
    private suspend fun saveAnalyzedRelations(noteId: Long) {
        val allKps = knowledgeRepository.getKnowledgePointsByNote(noteId).first()
        val kpMap = allKps.associateBy { it.title.replace(Regex("^#\\d{2}\\s*"), "") }

        for (result in _scanResults) {
            if (result.status != ScanStatus.COMPLETED) continue

            val suggestions = relationAnalyzer.analyzeRelations(
                result.extractedKnowledgePoints,
                allKps
            )

            for (suggestion in suggestions) {
                val sourceKp = kpMap[suggestion.sourceTitle.replace(Regex("^#\\d{2}\\s*"), "")]
                val targetKp = kpMap[suggestion.targetTitle.replace(Regex("^#\\d{2}\\s*"), "")]

                if (sourceKp != null && targetKp != null && sourceKp.id != targetKp.id) {
                    try {
                        knowledgeRepository.createRelation(
                            sourceId = sourceKp.id,
                            targetId = targetKp.id,
                            relationType = suggestion.relationType,
                            strength = suggestion.strength,
                            isAuto = true
                        )
                    } catch (_: Exception) {
                        // 忽略重复关联错误
                    }
                }
            }
        }
    }
}

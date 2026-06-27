package com.snapnote.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapnote.data.local.entity.KnowledgePointEntity
import com.snapnote.data.local.entity.RelationEntity
import com.snapnote.data.model.ContentType
import com.snapnote.data.model.KnowledgePoint
import com.snapnote.data.model.KnowledgePointRelation
import com.snapnote.data.model.RelationType
import com.snapnote.data.repository.KnowledgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RelationsViewModel(context: Context) : ViewModel() {
    private val repository = KnowledgeRepository(context)

    private val _knowledgePoints = MutableStateFlow<List<KnowledgePoint>>(emptyList())
    val knowledgePoints: StateFlow<List<KnowledgePoint>> = _knowledgePoints

    private val _selectedPoint = MutableStateFlow<KnowledgePoint?>(null)
    val selectedPoint: StateFlow<KnowledgePoint?> = _selectedPoint

    private val _relations = MutableStateFlow<List<KnowledgePointRelation>>(emptyList())
    val relations: StateFlow<List<KnowledgePointRelation>> = _relations

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // --- 编辑知识点 ---
    private val _isEditing = mutableStateOf(false)
    val isEditing: Boolean get() = _isEditing.value
    private val _editTitle = mutableStateOf("")
    val editTitle: String get() = _editTitle.value
    private val _editContent = mutableStateOf("")
    val editContent: String get() = _editContent.value
    private val _editContentType = mutableStateOf(ContentType.THEORY)
    val editContentType: ContentType get() = _editContentType.value

    // --- 删除回收站 ---
    private val _deletedPoints = MutableStateFlow<List<KnowledgePoint>>(emptyList())
    val deletedPoints: StateFlow<List<KnowledgePoint>> = _deletedPoints

    fun loadKnowledgePoints(noteId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getKnowledgePointsByNote(noteId).collect { list ->
                    _knowledgePoints.value = list
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectKnowledgePoint(pointId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val point = repository.getKnowledgePointWithRelations(pointId)
                _selectedPoint.value = point
                _relations.value = point?.relations ?: emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ========== 编辑功能 ==========

    fun startEditing(point: KnowledgePoint) {
        _isEditing.value = true
        _editTitle.value = point.title
        _editContent.value = point.content
        _editContentType.value = point.contentType
    }

    fun stopEditing() {
        _isEditing.value = false
    }

    fun updateEditTitle(title: String) {
        _editTitle.value = title
    }

    fun updateEditContent(content: String) {
        _editContent.value = content
    }

    fun updateEditContentType(type: ContentType) {
        _editContentType.value = type
    }

    fun saveEditedPoint(pointId: Long) {
        viewModelScope.launch {
            val original = _selectedPoint.value ?: return@launch
            val updated = original.copy(
                title = _editTitle.value,
                content = _editContent.value,
                contentType = _editContentType.value
            )
            repository.updateKnowledgePoint(updated)
            _selectedPoint.value = updated
            _isEditing.value = false
            // 刷新关联列表
            selectKnowledgePoint(pointId)
        }
    }

    // ========== 删除 / 回收站 ==========

    fun softDeletePoint(point: KnowledgePoint) {
        viewModelScope.launch {
            // 记录到回收站列表（本地内存，可扩展为数据库持久化）
            val currentDeleted = _deletedPoints.value.toMutableList()
            currentDeleted.add(point)
            _deletedPoints.value = currentDeleted

            // 从数据库删除
            repository.deleteKnowledgePoint(point)

            // 清除选中状态
            if (_selectedPoint.value?.id == point.id) {
                _selectedPoint.value = null
                _relations.value = emptyList()
            }
        }
    }

    fun restorePoint(point: KnowledgePoint) {
        viewModelScope.launch {
            // 从回收站恢复
            val currentDeleted = _deletedPoints.value.toMutableList()
            currentDeleted.removeAll { it.id == point.id }
            _deletedPoints.value = currentDeleted

            // 重新添加到数据库
            repository.addKnowledgePoint(point)
        }
    }

    fun permanentlyDeletePoint(point: KnowledgePoint) {
        viewModelScope.launch {
            val currentDeleted = _deletedPoints.value.toMutableList()
            currentDeleted.removeAll { it.id == point.id }
            _deletedPoints.value = currentDeleted
        }
    }

    fun emptyTrash() {
        _deletedPoints.value = emptyList()
    }

    // ========== 关联管理 ==========

    fun createRelation(sourceId: Long, targetId: Long, relationType: RelationType, strength: String) {
        viewModelScope.launch {
            repository.createRelation(sourceId, targetId, relationType, strength, isAuto = false)
            selectKnowledgePoint(sourceId)
        }
    }

    fun deleteRelation(relationId: Long) {
        viewModelScope.launch {
            repository.deleteRelation(relationId)
            _selectedPoint.value?.let { selectKnowledgePoint(it.id) }
        }
    }

    fun searchKnowledgePoints(query: String) {
        viewModelScope.launch {
            repository.searchKnowledgePoints(query).collect { list ->
                _knowledgePoints.value = list
            }
        }
    }
}

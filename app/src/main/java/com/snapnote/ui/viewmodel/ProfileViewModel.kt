package com.snapnote.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapnote.data.local.AppDatabase
import com.snapnote.export.MarkdownExporter
import com.snapnote.nas.NasSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val exporter = MarkdownExporter(context)
    private val nasManager = NasSyncManager(context)

    private val _totalNotes = MutableStateFlow(0)
    val totalNotes: StateFlow<Int> = _totalNotes

    private val _totalKnowledgePoints = MutableStateFlow(0)
    val totalKnowledgePoints: StateFlow<Int> = _totalKnowledgePoints

    private val _totalRelations = MutableStateFlow(0)
    val totalRelations: StateFlow<Int> = _totalRelations

    private val _exportStatus = MutableStateFlow<String?>(null)
    val exportStatus: StateFlow<String?> = _exportStatus

    private val _nasStatus = MutableStateFlow<String?>(null)
    val nasStatus: StateFlow<String?> = _nasStatus

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            val noteCount = database.noteDao().getAllNotes().let { flow ->
                var count = 0
                flow.collect { count = it.size }
                count
            }
            _totalNotes.value = noteCount
            _totalKnowledgePoints.value = database.knowledgePointDao().getKnowledgePointCount()
        }
    }

    fun exportAllToMarkdown() {
        viewModelScope.launch {
            _exportStatus.value = "正在导出..."
            try {
                exporter.exportAllNotes()
                _exportStatus.value = "导出成功"
            } catch (e: Exception) {
                _exportStatus.value = "导出失败: ${e.message}"
            }
        }
    }

    fun syncToNas(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _nasStatus.value = "正在同步..."
            try {
                nasManager.configure(serverUrl, username, password)
                nasManager.sync()
                _nasStatus.value = "同步成功"
            } catch (e: Exception) {
                _nasStatus.value = "同步失败: ${e.message}"
            }
        }
    }

    fun clearStatus() {
        _exportStatus.value = null
        _nasStatus.value = null
    }
}

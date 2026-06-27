package com.snapnote.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapnote.data.model.Note
import com.snapnote.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class NotesViewModel(context: Context) : ViewModel() {
    private val repository = NoteRepository(context)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .collect { query ->
                    loadNotes()
                }
        }
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val query = _searchQuery.value
                val flow = if (query.isBlank()) {
                    repository.getAllNotes()
                } else {
                    repository.searchNotes(query)
                }
                flow.collect { list ->
                    _notes.value = list
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            if (category == null) {
                repository.getAllNotes().collect { _notes.value = it }
            } else {
                repository.getNotesByCategory(category).collect { _notes.value = it }
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun archiveNote(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isArchived = true))
        }
    }
}

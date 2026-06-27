package com.snapnote.data.repository

import android.content.Context
import com.snapnote.data.local.AppDatabase
import com.snapnote.data.local.entity.ImageSourceEntity
import com.snapnote.data.local.entity.NoteEntity
import com.snapnote.data.model.Note
import com.snapnote.data.model.toEntity
import com.snapnote.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class NoteRepository(context: Context) {
    private val noteDao = AppDatabase.getDatabase(context).noteDao()
    private val knowledgePointDao = AppDatabase.getDatabase(context).knowledgePointDao()
    private val imageSourceDao = AppDatabase.getDatabase(context).imageSourceDao()

    fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { notes ->
            notes.map { noteEntity ->
                val kpCount = knowledgePointDao.getKnowledgePointCount()
                noteEntity.toModel(knowledgePointCount = kpCount)
            }
        }
    }

    fun searchNotes(query: String): Flow<List<Note>> {
        return noteDao.searchNotes(query).map { notes ->
            notes.map { it.toModel() }
        }
    }

    fun getNotesByCategory(category: String): Flow<List<Note>> {
        return noteDao.getNotesByCategory(category).map { notes ->
            notes.map { it.toModel() }
        }
    }

    suspend fun getNoteById(noteId: Long): Note? {
        return noteDao.getNoteById(noteId)?.toModel()
    }

    suspend fun createNote(title: String, category: String = ""): Long {
        val note = NoteEntity(
            title = title,
            category = category,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note.toEntity().copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }

    suspend fun addImagesToNote(noteId: Long, uris: List<String>) {
        val images = uris.map { uri ->
            ImageSourceEntity(
                noteId = noteId,
                uri = uri,
                fileName = uri.substringAfterLast("/")
            )
        }
        imageSourceDao.insertImages(images)
        noteDao.updateTimestamp(noteId)
    }

    fun getNoteWithDetails(noteId: Long): Flow<Pair<Note?, List<com.snapnote.data.local.entity.KnowledgePointEntity>>> {
        val noteFlow = kotlinx.coroutines.flow.flow {
            val entity = noteDao.getNoteById(noteId)
            emit(entity?.toModel())
        }
        val kpFlow = knowledgePointDao.getKnowledgePointsByNote(noteId)
        return noteFlow.combine(kpFlow) { note, kps ->
            note to kps
        }
    }
}

package com.snapnote.data.model

import com.snapnote.data.local.entity.NoteEntity

data class Note(
    val id: Long = 0,
    val title: String,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val knowledgePointCount: Int = 0,
    val imageCount: Int = 0
)

fun NoteEntity.toModel(knowledgePointCount: Int = 0, imageCount: Int = 0): Note {
    return Note(
        id = id,
        title = title,
        category = category,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isArchived = isArchived,
        knowledgePointCount = knowledgePointCount,
        imageCount = imageCount
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        title = title,
        category = category,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isArchived = isArchived
    )
}

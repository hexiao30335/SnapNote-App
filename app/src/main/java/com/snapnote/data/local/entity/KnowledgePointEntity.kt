package com.snapnote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "knowledge_points",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId")]
)
data class KnowledgePointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val noteId: Long,
    val number: String,
    val title: String,
    val content: String,
    val contentType: String,
    val parentId: Long? = null,
    val imageSourceId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

package com.snapnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snapnote.data.local.entity.KnowledgePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgePointDao {
    @Query("SELECT * FROM knowledge_points WHERE noteId = :noteId ORDER BY number ASC")
    fun getKnowledgePointsByNote(noteId: Long): Flow<List<KnowledgePointEntity>>

    @Query("SELECT * FROM knowledge_points WHERE id = :id")
    suspend fun getKnowledgePointById(id: Long): KnowledgePointEntity?

    @Query("SELECT * FROM knowledge_points WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'")
    fun searchKnowledgePoints(query: String): Flow<List<KnowledgePointEntity>>

    @Query("SELECT * FROM knowledge_points WHERE contentType = :type AND noteId = :noteId")
    fun getKnowledgePointsByType(noteId: Long, type: String): Flow<List<KnowledgePointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgePoint(kp: KnowledgePointEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledgePoints(kps: List<KnowledgePointEntity>)

    @Update
    suspend fun updateKnowledgePoint(kp: KnowledgePointEntity)

    @Delete
    suspend fun deleteKnowledgePoint(kp: KnowledgePointEntity)

    @Query("DELETE FROM knowledge_points WHERE noteId = :noteId")
    suspend fun deleteKnowledgePointsByNote(noteId: Long)

    @Query("SELECT COUNT(*) FROM knowledge_points")
    suspend fun getKnowledgePointCount(): Int
}

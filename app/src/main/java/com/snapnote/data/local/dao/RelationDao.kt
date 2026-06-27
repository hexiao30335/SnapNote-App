package com.snapnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snapnote.data.local.entity.RelationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RelationDao {
    @Query("SELECT * FROM relations WHERE sourceId = :sourceId")
    fun getRelationsBySource(sourceId: Long): Flow<List<RelationEntity>>

    @Query("SELECT * FROM relations WHERE targetId = :targetId")
    fun getRelationsByTarget(targetId: Long): Flow<List<RelationEntity>>

    @Query("SELECT * FROM relations WHERE sourceId = :knowledgePointId OR targetId = :knowledgePointId")
    fun getAllRelationsForKnowledgePoint(knowledgePointId: Long): Flow<List<RelationEntity>>

    @Query("SELECT * FROM relations WHERE relationType = :type")
    fun getRelationsByType(type: String): Flow<List<RelationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: RelationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(relations: List<RelationEntity>)

    @Delete
    suspend fun deleteRelation(relation: RelationEntity)

    @Query("DELETE FROM relations WHERE sourceId = :knowledgePointId OR targetId = :knowledgePointId")
    suspend fun deleteRelationsByKnowledgePoint(knowledgePointId: Long)
}

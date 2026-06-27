package com.snapnote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.snapnote.data.local.entity.ImageSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageSourceDao {
    @Query("SELECT * FROM image_sources WHERE noteId = :noteId ORDER BY createdAt DESC")
    fun getImagesByNote(noteId: Long): Flow<List<ImageSourceEntity>>

    @Query("SELECT * FROM image_sources WHERE id = :id")
    suspend fun getImageById(id: Long): ImageSourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageSourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ImageSourceEntity>)

    @Update
    suspend fun updateImage(image: ImageSourceEntity)

    @Delete
    suspend fun deleteImage(image: ImageSourceEntity)

    @Query("UPDATE image_sources SET status = :status, ocrText = :ocrText WHERE id = :imageId")
    suspend fun updateImageStatus(imageId: Long, status: String, ocrText: String)
}

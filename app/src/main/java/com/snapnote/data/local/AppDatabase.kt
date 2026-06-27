package com.snapnote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.snapnote.data.local.dao.ImageSourceDao
import com.snapnote.data.local.dao.KnowledgePointDao
import com.snapnote.data.local.dao.NoteDao
import com.snapnote.data.local.dao.RelationDao
import com.snapnote.data.local.entity.ImageSourceEntity
import com.snapnote.data.local.entity.KnowledgePointEntity
import com.snapnote.data.local.entity.NoteEntity
import com.snapnote.data.local.entity.RelationEntity

@Database(
    entities = [
        NoteEntity::class,
        KnowledgePointEntity::class,
        ImageSourceEntity::class,
        RelationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun knowledgePointDao(): KnowledgePointDao
    abstract fun imageSourceDao(): ImageSourceDao
    abstract fun relationDao(): RelationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "snapnote_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

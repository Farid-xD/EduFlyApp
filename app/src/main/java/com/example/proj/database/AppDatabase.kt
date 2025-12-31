// File: com/example/proj/database/AppDatabase.kt
package com.example.proj.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.proj.models.User
import com.example.proj.database.entities.CourseContentEntity
import com.example.proj.database.entities.CourseEntity
import com.example.proj.database.entities.AssetEntity
import com.example.proj.database.entities.CourseProgressEntity

@Database(
    entities = [
        CourseEntity::class,           // Database entity
        CourseContentEntity::class,
        User::class,                   // User entity (has @Entity and @TypeConverters)
        AssetEntity::class,
        CourseProgressEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)  // Global converters for all entities
abstract class AppDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun courseContentDao(): CourseContentDao
    abstract fun assetDao(): AssetDao
    abstract fun userDao(): UserDao
    abstract fun courseProgressDao(): CourseProgressDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "edufly_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
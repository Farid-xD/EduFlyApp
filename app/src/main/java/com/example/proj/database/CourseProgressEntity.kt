package com.example.proj.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "course_progress",
    primaryKeys = ["user_id", "course_id"])
data class CourseProgressEntity(
    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "course_id")
    val courseId: String,

    @ColumnInfo(name = "progress_percentage")
    val progressPercentage: Int = 0,

    @ColumnInfo(name = "completed_lessons")
    val completedLessons: List<String> = emptyList(),

    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Date = Date(),

    @ColumnInfo(name = "started_at")
    val startedAt: Date = Date(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Date? = null,

    @ColumnInfo(name = "total_time_spent")
    val totalTimeSpent: Long = 0
)
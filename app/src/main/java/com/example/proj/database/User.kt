// File: com/example/proj/models/User.kt
package com.example.proj.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val id: String,

    @ColumnInfo(name = "email", index = true)
    val email: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "profile_image_url")
    val profileImageUrl: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "last_login")
    val lastLogin: Date? = null,

    @ColumnInfo(name = "role")
    val role: String = "student",

    @ColumnInfo(name = "enrolled_courses")
    val enrolledCourses: List<String> = emptyList(),

    @ColumnInfo(name = "completed_courses")
    val completedCourses: List<String> = emptyList(),

    @ColumnInfo(name = "total_progress")
    val totalProgress: Int = 0,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
package com.example.proj.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "course_content")
data class CourseContentEntity(
    @PrimaryKey
    val courseId: String,

    // Full AI-generated content
    val fullContent: String,

    // Parsed sections
    val overview: String = "",
    val learningObjectives: String = "",
    val modulesJson: String = "", // JSON string of modules
    val detailedContent: String = "",

    // Assessment content
    val quizQuestions: String = "", // JSON string
    val mcqQuestions: String = "", // JSON string

    // Video suggestions
    val videoSuggestions: String = "", // JSON string

    // Additional resources
    val finalProject: String = "",
    val additionalResources: String = "",

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)


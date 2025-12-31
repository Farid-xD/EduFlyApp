package com.example.proj.models

// Data classes for structured content
data class CourseModule(
    val moduleNumber: Int,
    val title: String,
    val topics: List<String>,
    val description: String,
    val isCompleted: Boolean = false
)

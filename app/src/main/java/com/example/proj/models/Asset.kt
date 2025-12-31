package com.example.proj.models

data class Asset(
    val id: String,
    val courseId: String,
    val title: String,
    val description: String,
    val type: String, // "video", "pdf", "quiz", etc.
    val url: String,
    val duration: Int? = null,
    val fileSize: Long? = null,
    val order: Int = 0,
    val isDownloaded: Boolean = false,
    val downloadPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
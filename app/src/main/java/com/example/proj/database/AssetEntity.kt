package com.example.proj.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey
    @ColumnInfo(name = "asset_id")
    val id: String,

    @ColumnInfo(name = "course_id")
    val courseId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "duration")
    val duration: Int? = null,

    @ColumnInfo(name = "file_size")
    val fileSize: Long? = null,

    @ColumnInfo(name = "asset_order")
    val order: Int = 0,

    @ColumnInfo(name = "is_downloaded")
    val isDownloaded: Boolean = false,

    @ColumnInfo(name = "download_path")
    val downloadPath: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
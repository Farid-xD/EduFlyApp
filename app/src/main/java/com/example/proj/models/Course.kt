package com.example.proj.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Course(
    val id: String,
    val title: String,
    val code: String,
    val description: String,
    val instructor: String,
    val duration: String,
    val progress: Int,
    val lessons: Int,
    val students: Int,
    val imageUrl: String,
    val category: String,
    val rating: Float,
    val isEnrolled: Boolean,
    val difficulty: String,
    val price: String,
    val lastUpdated: String,
    // Additional fields that might be in your database
    val instructorId: String = "",
    val thumbnailUrl: String? = null,
    val semester: String = "Spring 2024",
    val credits: Int = 3,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
    val isActive: Boolean = true,
    val enrollmentDate: Long? = null,
    val totalAssets: Int = 0,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable
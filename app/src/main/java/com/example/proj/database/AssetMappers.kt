// File: com/example/proj/utils/CourseMapper.kt
package com.example.proj.utils

import com.example.proj.database.entities.CourseEntity
import com.example.proj.database.entities.AssetEntity
import com.example.proj.models.Course
import com.example.proj.models.Asset

object CourseMapper {
    // CourseEntity to Course
    fun toCourse(entity: CourseEntity): Course {
        return Course(
            id = entity.id,
            title = entity.title,
            code = entity.code,
            description = entity.description,
            instructor = entity.instructor,
            duration = entity.duration,
            progress = entity.progress,
            lessons = entity.lessons,
            students = entity.students,
            imageUrl = entity.imageUrl,
            category = entity.category,
            rating = entity.rating,
            isEnrolled = entity.isEnrolled,
            difficulty = entity.difficulty,
            price = entity.price,
            lastUpdated = entity.lastUpdated,
            instructorId = entity.instructorId,
            thumbnailUrl = entity.thumbnailUrl,
            semester = entity.semester,
            credits = entity.credits,
            startDate = entity.startDate,
            endDate = entity.endDate,
            isActive = entity.isActive,
            enrollmentDate = entity.enrollmentDate,
            totalAssets = entity.totalAssets,
            isFavorite = entity.isFavorite,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    // Course to CourseEntity
    fun toCourseEntity(model: Course): CourseEntity {
        return CourseEntity(
            id = model.id,
            title = model.title,
            code = model.code,
            description = model.description,
            instructor = model.instructor,
            duration = model.duration,
            progress = model.progress,
            lessons = model.lessons,
            students = model.students,
            imageUrl = model.imageUrl,
            category = model.category,
            rating = model.rating,
            isEnrolled = model.isEnrolled,
            difficulty = model.difficulty,
            price = model.price,
            lastUpdated = model.lastUpdated,
            instructorId = model.instructorId,
            thumbnailUrl = model.thumbnailUrl,
            semester = model.semester,
            credits = model.credits,
            startDate = model.startDate,
            endDate = model.endDate,
            isActive = model.isActive,
            enrollmentDate = model.enrollmentDate,
            totalAssets = model.totalAssets,
            isFavorite = model.isFavorite,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }

    // List conversions for Course
    fun toCourseList(entities: List<CourseEntity>): List<Course> {
        return entities.map { toCourse(it) }
    }

    fun toCourseEntityList(models: List<Course>): List<CourseEntity> {
        return models.map { toCourseEntity(it) }
    }
}

object AssetMapper {
    // AssetEntity to Asset
    fun toAsset(entity: AssetEntity): Asset {
        return Asset(
            id = entity.id,
            courseId = entity.courseId,
            title = entity.title,
            description = entity.description,
            type = entity.type,
            url = entity.url,
            duration = entity.duration,
            fileSize = entity.fileSize,
            order = entity.order,
            isDownloaded = entity.isDownloaded,
            downloadPath = entity.downloadPath,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    // Asset to AssetEntity
    fun toEntity(model: Asset): AssetEntity {
        return AssetEntity(
            id = model.id,
            courseId = model.courseId,
            title = model.title,
            description = model.description,
            type = model.type,
            url = model.url,
            duration = model.duration,
            fileSize = model.fileSize,
            order = model.order,
            isDownloaded = model.isDownloaded,
            downloadPath = model.downloadPath,
            createdAt = model.createdAt,
            updatedAt = model.updatedAt
        )
    }

    // List conversions for Asset
    fun toAssetList(entities: List<AssetEntity>): List<Asset> {
        return entities.map { toAsset(it) }
    }

    fun toEntityList(models: List<Asset>): List<AssetEntity> {
        return models.map { toEntity(it) }
    }
}


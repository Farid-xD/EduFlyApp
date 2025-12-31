package com.example.proj.repository

import com.example.proj.database.CourseDao
import com.example.proj.database.entities.CourseEntity
import com.example.proj.models.Course
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CourseRepository(
    private val courseDao: CourseDao
) {

    // Convert Course to CourseEntity
    private fun Course.toEntity(): CourseEntity {
        return CourseEntity(
            id = this.id,
            title = this.title,
            code = this.code,
            description = this.description,
            instructor = this.instructor,
            instructorId = this.instructorId,
            duration = this.duration,
            progress = this.progress,
            lessons = this.lessons,
            students = this.students,
            imageUrl = this.imageUrl,
            category = this.category,
            rating = this.rating,
            isEnrolled = this.isEnrolled,
            difficulty = this.difficulty,
            price = this.price,
            lastUpdated = this.lastUpdated,
            thumbnailUrl = this.thumbnailUrl,
            semester = this.semester,
            credits = this.credits,
            startDate = this.startDate,
            endDate = this.endDate,
            isActive = this.isActive,
            enrollmentDate = this.enrollmentDate,
            totalAssets = this.totalAssets,
            isFavorite = this.isFavorite,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    // Convert CourseEntity to Course
    private fun CourseEntity.toCourse(): Course {
        return Course(
            id = this.id,
            title = this.title,
            code = this.code,
            description = this.description,
            instructor = this.instructor,
            duration = this.duration,
            progress = this.progress,
            lessons = this.lessons,
            students = this.students,
            imageUrl = this.imageUrl,
            category = this.category,
            rating = this.rating,
            isEnrolled = this.isEnrolled,
            difficulty = this.difficulty,
            price = this.price,
            lastUpdated = this.lastUpdated,
            instructorId = this.instructorId,
            thumbnailUrl = this.thumbnailUrl,
            semester = this.semester,
            credits = this.credits,
            startDate = this.startDate,
            endDate = this.endDate,
            isActive = this.isActive,
            enrollmentDate = this.enrollmentDate,
            totalAssets = this.totalAssets,
            isFavorite = this.isFavorite,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    // Create
    suspend fun insertCourse(course: Course) = courseDao.insertCourse(course.toEntity())

    suspend fun insertAllCourses(courses: List<Course>) =
        courseDao.insertAllCourses(courses.map { it.toEntity() })

    // Read - returns Flow<List<Course>>
    fun getAllCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses().map { entities ->
            entities.map { it.toCourse() }
        }
    }

    suspend fun getCourseById(courseId: String): Course? {
        return courseDao.getCourseById(courseId)?.toCourse()
    }

    fun getCourseByIdLive(courseId: String): Flow<Course?> {
        return courseDao.getCourseByIdLive(courseId).map { entity ->
            entity?.toCourse()
        }
    }

    fun getCoursesByCategory(category: String): Flow<List<Course>> {
        return courseDao.getCoursesByCategory(category).map { entities ->
            entities.map { it.toCourse() }
        }
    }

    fun getEnrolledCourses(): Flow<List<Course>> {
        return courseDao.getEnrolledCourses().map { entities ->
            entities.map { it.toCourse() }
        }
    }

    fun getFavoriteCourses(): Flow<List<Course>> {
        return courseDao.getFavoriteCourses().map { entities ->
            entities.map { it.toCourse() }
        }
    }

    // Search
    fun searchCourses(query: String): Flow<List<Course>> {
        return courseDao.searchCourses(query).map { entities ->
            entities.map { it.toCourse() }
        }
    }

    // Update
    suspend fun updateCourse(course: Course) = courseDao.updateCourse(course.toEntity())

    suspend fun updateCourseProgress(courseId: String, progress: Int) =
        courseDao.updateCourseProgress(courseId, progress)

    suspend fun updateCourseEnrollment(courseId: String, isEnrolled: Boolean) =
        courseDao.updateCourseEnrollment(courseId, isEnrolled)

    suspend fun updateCourseFavorite(courseId: String, isFavorite: Boolean) =
        courseDao.updateCourseFavorite(courseId, isFavorite)

    // Delete
    suspend fun deleteCourse(course: Course) = courseDao.deleteCourse(course.toEntity())

    suspend fun deleteCourseById(courseId: String) = courseDao.deleteCourseById(courseId)

    suspend fun deleteAllCourses() = courseDao.deleteAllCourses()

    // Statistics
    suspend fun getCourseCount(): Int = courseDao.getCourseCount()

    suspend fun getEnrolledCourseCount(): Int = courseDao.getEnrolledCourseCount()

    // Create course (for AI generation)
    suspend fun createCourse(course: Course) {
        insertCourse(course)
    }
}
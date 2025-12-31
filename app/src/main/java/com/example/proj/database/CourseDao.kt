package com.example.proj.database

import androidx.room.*
import com.example.proj.database.entities.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    // CREATE
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCourses(courses: List<CourseEntity>): List<Long>

    // READ - Flow operations
    @Query("SELECT * FROM courses ORDER BY title ASC")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseByIdLive(courseId: String): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE category = :category ORDER BY title ASC")
    fun getCoursesByCategory(category: String): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE isEnrolled = 1 ORDER BY lastUpdated DESC")
    fun getEnrolledCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteCourses(): Flow<List<CourseEntity>>

    // SEARCH
    @Query("SELECT * FROM courses WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchCourses(query: String): Flow<List<CourseEntity>>

    // UPDATE
    @Update
    suspend fun updateCourse(course: CourseEntity): Int

    @Query("UPDATE courses SET progress = :progress WHERE id = :courseId")
    suspend fun updateCourseProgress(courseId: String, progress: Int): Int

    @Query("UPDATE courses SET isEnrolled = :isEnrolled WHERE id = :courseId")
    suspend fun updateCourseEnrollment(courseId: String, isEnrolled: Boolean): Int

    @Query("UPDATE courses SET isFavorite = :isFavorite WHERE id = :courseId")
    suspend fun updateCourseFavorite(courseId: String, isFavorite: Boolean): Int

    // DELETE
    @Delete
    suspend fun deleteCourse(course: CourseEntity): Int

    @Query("DELETE FROM courses WHERE id = :courseId")
    suspend fun deleteCourseById(courseId: String): Int

    @Query("DELETE FROM courses")
    suspend fun deleteAllCourses()

    // STATISTICS
    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int

    @Query("SELECT COUNT(*) FROM courses WHERE isEnrolled = 1")
    suspend fun getEnrolledCourseCount(): Int
}
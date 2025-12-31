package com.example.proj.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.proj.database.entities.CourseProgressEntity

@Dao
interface CourseProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: CourseProgressEntity)

    @Query("SELECT * FROM course_progress WHERE user_id = :userId AND course_id = :courseId")
    suspend fun getProgress(userId: String, courseId: String): CourseProgressEntity?

    @Query("SELECT * FROM course_progress WHERE user_id = :userId")
    fun getUserProgress(userId: String): LiveData<List<CourseProgressEntity>>

    @Query("SELECT * FROM course_progress WHERE course_id = :courseId")
    fun getCourseProgress(courseId: String): LiveData<List<CourseProgressEntity>>

    @Update
    suspend fun updateProgress(progress: CourseProgressEntity)

    @Query("UPDATE course_progress SET progress_percentage = :progress WHERE user_id = :userId AND course_id = :courseId")
    suspend fun updateProgressPercentage(userId: String, courseId: String, progress: Int)

    @Query("UPDATE course_progress SET completed_lessons = :completedLessons WHERE user_id = :userId AND course_id = :courseId")
    suspend fun updateCompletedLessons(userId: String, courseId: String, completedLessons: List<String>)

    @Query("UPDATE course_progress SET total_time_spent = total_time_spent + :additionalTime WHERE user_id = :userId AND course_id = :courseId")
    suspend fun addTimeSpent(userId: String, courseId: String, additionalTime: Long)

    @Query("UPDATE course_progress SET completed_at = :completedAt WHERE user_id = :userId AND course_id = :courseId")
    suspend fun markAsCompleted(userId: String, courseId: String, completedAt: Long)

    @Delete
    suspend fun deleteProgress(progress: CourseProgressEntity)
}
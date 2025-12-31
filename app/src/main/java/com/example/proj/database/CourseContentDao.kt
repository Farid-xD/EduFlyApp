package com.example.proj.database

import androidx.room.*
import com.example.proj.database.entities.CourseContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseContentDao {

    // CREATE/UPDATE - Insert or replace existing content
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: CourseContentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllContents(contents: List<CourseContentEntity>): List<Long>

    // UPDATE specific operations
    @Update
    suspend fun updateContent(content: CourseContentEntity): Int

    @Query("UPDATE course_content SET lastAccessed = :timestamp WHERE courseId = :courseId")
    suspend fun updateLastAccessed(courseId: String, timestamp: Long): Int

    @Query("UPDATE course_content SET overview = :overview, learningObjectives = :learningObjectives, lastAccessed = :timestamp WHERE courseId = :courseId")
    suspend fun updateCourseOverview(courseId: String, overview: String, learningObjectives: String, timestamp: Long): Int

    // READ operations
    @Query("SELECT * FROM course_content WHERE courseId = :courseId")
    fun getContentByCourseId(courseId: String): Flow<CourseContentEntity?>

    @Query("SELECT * FROM course_content WHERE courseId = :courseId")
    suspend fun getContentByCourseIdSuspend(courseId: String): CourseContentEntity?

    @Query("SELECT * FROM course_content ORDER BY lastAccessed DESC")
    fun getAllContentsSortedByAccess(): Flow<List<CourseContentEntity>>

    @Query("SELECT * FROM course_content WHERE courseId IN (:courseIds)")
    fun getContentsForCourses(courseIds: List<String>): Flow<List<CourseContentEntity>>

    @Query("SELECT * FROM course_content ORDER BY createdAt DESC")
    fun getAllContents(): Flow<List<CourseContentEntity>>

    // Search within full content
    @Query("SELECT * FROM course_content WHERE fullContent LIKE '%' || :query || '%' OR overview LIKE '%' || :query || '%' OR detailedContent LIKE '%' || :query || '%'")
    fun searchContents(query: String): Flow<List<CourseContentEntity>>

    // DELETE operations
    @Delete
    suspend fun deleteContent(content: CourseContentEntity): Int

    @Query("DELETE FROM course_content WHERE courseId = :courseId")
    suspend fun deleteContentByCourseId(courseId: String): Int

    @Query("DELETE FROM course_content")
    suspend fun deleteAllContents()

    // UTILITY operations
    @Query("SELECT COUNT(*) FROM course_content WHERE courseId = :courseId")
    suspend fun hasContentForCourse(courseId: String): Int

    @Query("SELECT COUNT(*) FROM course_content")
    suspend fun getContentCount(): Int

    // Get specific content sections
    @Query("SELECT overview FROM course_content WHERE courseId = :courseId")
    suspend fun getOverviewByCourseId(courseId: String): String?

    @Query("SELECT learningObjectives FROM course_content WHERE courseId = :courseId")
    suspend fun getLearningObjectivesByCourseId(courseId: String): String?

    @Query("SELECT modulesJson FROM course_content WHERE courseId = :courseId")
    suspend fun getModulesJsonByCourseId(courseId: String): String?

    @Query("SELECT quizQuestions FROM course_content WHERE courseId = :courseId")
    suspend fun getQuizQuestionsByCourseId(courseId: String): String?

    @Query("SELECT mcqQuestions FROM course_content WHERE courseId = :courseId")
    suspend fun getMcqQuestionsByCourseId(courseId: String): String?

    // BATCH operations
    @Transaction
    suspend fun upsertContent(content: CourseContentEntity) {
        if (hasContentForCourse(content.courseId) > 0) {
            updateContent(content)
        } else {
            insertContent(content)
        }
    }

    @Transaction
    suspend fun upsertAllContents(contents: List<CourseContentEntity>) {
        contents.forEach { content ->
            upsertContent(content)
        }
    }

    // Get recently accessed contents
    @Query("SELECT * FROM course_content WHERE lastAccessed > :sinceTimestamp ORDER BY lastAccessed DESC")
    fun getRecentlyAccessed(sinceTimestamp: Long): Flow<List<CourseContentEntity>>

    // Check if content exists for a course
    @Query("SELECT EXISTS(SELECT 1 FROM course_content WHERE courseId = :courseId)")
    fun contentExists(courseId: String): Flow<Boolean>

    // Update specific sections
    @Query("UPDATE course_content SET modulesJson = :modulesJson, lastAccessed = :timestamp WHERE courseId = :courseId")
    suspend fun updateModules(courseId: String, modulesJson: String, timestamp: Long): Int

    @Query("UPDATE course_content SET quizQuestions = :quizQuestions, lastAccessed = :timestamp WHERE courseId = :courseId")
    suspend fun updateQuizQuestions(courseId: String, quizQuestions: String, timestamp: Long): Int

    @Query("UPDATE course_content SET mcqQuestions = :mcqQuestions, lastAccessed = :timestamp WHERE courseId = :courseId")
    suspend fun updateMcqQuestions(courseId: String, mcqQuestions: String, timestamp: Long): Int
}
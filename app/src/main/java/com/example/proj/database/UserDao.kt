package com.example.proj.database

import androidx.room.*
import com.example.proj.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun authenticateUser(email: String, password: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET last_login = :lastLogin WHERE user_id = :userId")
    suspend fun updateLastLogin(userId: String, lastLogin: Long)

    @Query("UPDATE users SET enrolled_courses = :enrolledCourses WHERE user_id = :userId")
    suspend fun updateEnrolledCourses(userId: String, enrolledCourses: List<String>)

    @Query("UPDATE users SET completed_courses = :completedCourses WHERE user_id = :userId")
    suspend fun updateCompletedCourses(userId: String, completedCourses: List<String>)

    @Query("UPDATE users SET total_progress = :totalProgress WHERE user_id = :userId")
    suspend fun updateTotalProgress(userId: String, totalProgress: Int)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteUserById(userId: String)
}
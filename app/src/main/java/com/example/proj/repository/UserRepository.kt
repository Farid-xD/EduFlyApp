package com.example.proj.repository

import com.example.proj.database.UserDao
import com.example.proj.models.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao
) {

    // Get user by ID
    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)
    }

    // Get user by email
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    // Login user - check credentials
    suspend fun loginUser(email: String, password: String): User? {
        return userDao.authenticateUser(email, password)
    }

    // Register new user
    suspend fun registerUser(user: User): Boolean {
        return try {
            // Check if user already exists
            val existingUser = getUserByEmail(user.email)
            if (existingUser == null) {
                userDao.insertUser(user)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Update user
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    // Get all users as Flow
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }

    // Delete user
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun deleteUserById(userId: String) {
        userDao.deleteUserById(userId)
    }

    // Update last login
    suspend fun updateLastLogin(userId: String, lastLogin: Long) {
        userDao.updateLastLogin(userId, lastLogin)
    }

    // Update enrolled courses
    suspend fun updateEnrolledCourses(userId: String, enrolledCourses: List<String>) {
        userDao.updateEnrolledCourses(userId, enrolledCourses)
    }

    // Update completed courses
    suspend fun updateCompletedCourses(userId: String, completedCourses: List<String>) {
        userDao.updateCompletedCourses(userId, completedCourses)
    }

    // Update total progress
    suspend fun updateTotalProgress(userId: String, totalProgress: Int) {
        userDao.updateTotalProgress(userId, totalProgress)
    }
}
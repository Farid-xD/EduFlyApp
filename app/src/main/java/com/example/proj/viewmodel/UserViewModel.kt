package com.example.proj.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.proj.database.AppDatabase
import com.example.proj.models.User
import com.example.proj.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository

    // LiveData for UI
    private val _loginResult = MutableLiveData<Pair<Boolean, String>?>()
    private val _registerResult = MutableLiveData<Pair<Boolean, String>?>()
    private val _currentUser = MutableLiveData<User?>()
    private val _isLoading = MutableLiveData(false)
    private val _allUsers = MutableLiveData<List<User>>(emptyList())

    // Public LiveData
    val loginResult: MutableLiveData<Pair<Boolean, String>?> get() = _loginResult
    val registerResult: MutableLiveData<Pair<Boolean, String>?> get() = _registerResult
    val currentUser: MutableLiveData<User?> get() = _currentUser
    val isLoading: MutableLiveData<Boolean> get() = _isLoading
    val allUsers: MutableLiveData<List<User>> get() = _allUsers

    // Flow for reactive updates
    val usersFlow: Flow<List<User>> get() = repository.getAllUsers()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = UserRepository(database.userDao())

        // Seed default admin user
        viewModelScope.launch {
            seedDefaultUser()
        }

        // Observe all users from Flow
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _allUsers.value = users
            }
        }
    }

    private suspend fun seedDefaultUser() {
        val existingUser = repository.getUserByEmail("admin@example.com")
        if (existingUser == null) {
            val adminUser = User(
                id = UUID.randomUUID().toString(),
                email = "admin@example.com",
                password = "admin123",
                name = "Admin User",
                lastLogin = null,
                role = "admin"
            )
            repository.registerUser(adminUser)
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _loginResult.value = Pair(false, "Please fill all fields")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = repository.loginUser(email, password)
                if (user != null) {
                    // Update last login
                    val updatedUser = user.copy(lastLogin = Date())
                    repository.updateUser(updatedUser)

                    _currentUser.value = updatedUser
                    _loginResult.value = Pair(true, "Login successful!")
                } else {
                    _loginResult.value = Pair(false, "Invalid email or password")
                }
            } catch (e: Exception) {
                _loginResult.value = Pair(false, "Login failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _registerResult.value = Pair(false, "Please fill all fields")
            return
        }

        if (password.length < 6) {
            _registerResult.value = Pair(false, "Password must be at least 6 characters")
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = User(
                    id = UUID.randomUUID().toString(),
                    email = email,
                    password = password,
                    name = name,
                    lastLogin = null,
                    role = "student"
                )

                val success = repository.registerUser(user)
                if (success) {
                    _registerResult.value = Pair(true, "Registration successful!")
                    // Automatically log in after registration
                    login(email, password)
                } else {
                    _registerResult.value = Pair(false, "Email already exists")
                }
            } catch (e: Exception) {
                _registerResult.value = Pair(false, "Registration failed: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginResult.value = null
        _registerResult.value = null
    }

    fun clearResults() {
        _loginResult.value = null
        _registerResult.value = null
    }

    // Additional user management functions

    fun updateUserProfile(user: User) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.updateUser(user)
                _currentUser.value = user
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                // Handle error
            }
        }
    }

    fun getUserById(userId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                // You could create another LiveData for this or update currentUser
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateLastLogin(userId: String) {
        viewModelScope.launch {
            repository.updateLastLogin(userId, Date().time)
        }
    }

    fun enrollInCourse(userId: String, courseId: String) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            user?.let {
                val enrolledCourses = it.enrolledCourses.toMutableList()
                if (!enrolledCourses.contains(courseId)) {
                    enrolledCourses.add(courseId)
                    repository.updateEnrolledCourses(userId, enrolledCourses)
                }
            }
        }
    }

    fun completeCourse(userId: String, courseId: String) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            user?.let {
                val completedCourses = it.completedCourses.toMutableList()
                if (!completedCourses.contains(courseId)) {
                    completedCourses.add(courseId)
                    repository.updateCompletedCourses(userId, completedCourses)

                    // Update progress percentage
                    val totalEnrolled = it.enrolledCourses.size
                    val totalCompleted = completedCourses.size
                    val progress = if (totalEnrolled > 0) {
                        (totalCompleted * 100) / totalEnrolled
                    } else {
                        0
                    }
                    repository.updateTotalProgress(userId, progress)
                }
            }
        }
    }

    fun updateProgress(userId: String, progress: Int) {
        viewModelScope.launch {
            repository.updateTotalProgress(userId, progress)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUserById(userId)
            // If deleting current user, logout
            if (_currentUser.value?.id == userId) {
                logout()
            }
        }
    }

    fun searchUsers(query: String): List<User> {
        return _allUsers.value?.filter { user ->
            user.name.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)
        } ?: emptyList()
    }

    fun isEmailTaken(email: String): Boolean {
        return _allUsers.value?.any { it.email.equals(email, ignoreCase = true) } ?: false
    }

    fun validatePassword(password: String): Pair<Boolean, String> {
        return when {
            password.length < 6 -> Pair(false, "Password must be at least 6 characters")
            !password.any { it.isDigit() } -> Pair(false, "Password must contain at least one number")
            !password.any { it.isLetter() } -> Pair(false, "Password must contain at least one letter")
            else -> Pair(true, "Password is valid")
        }
    }

    fun resetPassword(email: String, newPassword: String) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            user?.let {
                val updatedUser = user.copy(password = newPassword)
                repository.updateUser(updatedUser)
                // You could add a LiveData for password reset result
            }
        }
    }

    // Convert Flow to LiveData for XML views
    fun getUsersAsLiveData() = repository.getAllUsers().asLiveData()
}
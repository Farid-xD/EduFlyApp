package com.example.proj.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.proj.database.AppDatabase
import com.example.proj.models.Course
import com.example.proj.database.entities.CourseContentEntity
import com.example.proj.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CourseViewModel(
    application: Application,
    private val repository: CourseRepository
) : AndroidViewModel(application) {

    // LiveData for courses list
    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    // Loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Error messages
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Selected course for detail view
    private val _selectedCourse = MutableLiveData<Course?>()
    val selectedCourse: LiveData<Course?> = _selectedCourse

    // DAOs for course content operations
    private val courseContentDao = AppDatabase.getDatabase(application).courseContentDao()

    init {
        refreshCourses()
    }

    fun refreshCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Use Flow to observe courses
                repository.getAllCourses().collect { courseList ->
                    _courses.value = courseList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load courses: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // FLOW METHODS (returns Flow)
    fun getAllCoursesFlow(): Flow<List<Course>> = repository.getAllCourses()

    fun getCourseByIdFlow(courseId: String): Flow<Course?> = repository.getCourseByIdLive(courseId)

    fun getEnrolledCoursesFlow(): Flow<List<Course>> = repository.getEnrolledCourses()

    fun getFavoriteCoursesFlow(): Flow<List<Course>> = repository.getFavoriteCourses()

    fun searchCoursesFlow(query: String): Flow<List<Course>> = repository.searchCourses(query)

    // LIVE DATA METHODS (returns LiveData for Activities)
    fun getCourseById(courseId: String) = repository.getCourseByIdLive(courseId).asLiveData()

    fun getAllCoursesAsLiveData() = repository.getAllCourses().asLiveData()

    fun getEnrolledCoursesAsLiveData() = repository.getEnrolledCourses().asLiveData()

    fun getFavoriteCoursesAsLiveData() = repository.getFavoriteCourses().asLiveData()

    fun searchCoursesAsLiveData(query: String) = repository.searchCourses(query).asLiveData()

    // CREATE METHODS
    suspend fun createCourse(course: Course) {
        repository.createCourse(course)
        refreshCourses()
    }

    fun createCourseAsync(course: Course) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.createCourse(course)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create course: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // READ METHODS
    suspend fun getCourseByIdSuspend(courseId: String): Course? {
        return repository.getCourseById(courseId)
    }

    suspend fun insertCourse(course: Course) {
        repository.insertCourse(course)
        refreshCourses()
    }

    suspend fun insertAllCourses(courses: List<Course>) {
        repository.insertAllCourses(courses)
        refreshCourses()
    }

    fun loadCourseById(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedCourse.value = repository.getCourseById(courseId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load course: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // UPDATE METHODS
    suspend fun updateCourse(course: Course) {
        repository.updateCourse(course)
        refreshCourses()
    }

    fun updateCourseAsync(course: Course) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCourse(course)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update course: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCourseEnrollment(courseId, true)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to enroll in course: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unenrollFromCourse(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCourseEnrollment(courseId, false)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to unenroll from course: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(courseId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCourseFavorite(courseId, isFavorite)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update favorite: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProgress(courseId: String, progress: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCourseProgress(courseId, progress)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update progress: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Combined update methods for CourseDetailActivity
    fun updateCourseEnrollmentAndProgress(courseId: String, isEnrolled: Boolean, progress: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.updateCourseEnrollment(courseId, isEnrolled)
                repository.updateCourseProgress(courseId, progress)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update course: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // DELETE METHODS
    suspend fun deleteCourse(course: Course) {
        repository.deleteCourse(course)
        refreshCourses()
    }

    suspend fun deleteCourseById(courseId: String) {
        repository.deleteCourseById(courseId)
        refreshCourses()
    }

    // STATISTICS
    suspend fun getCourseCount(): Int = repository.getCourseCount()

    suspend fun getEnrolledCourseCount(): Int = repository.getEnrolledCourseCount()

    // Search method for CoursesActivity
    fun searchCourses(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.searchCourses(query).collect { courseList ->
                    _courses.value = courseList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search courses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Error handling
    fun clearError() {
        _errorMessage.value = null
    }

    // Get all courses (for CoursesActivity)
    fun getAllCourses() {
        refreshCourses()
    }

    // COURSE CONTENT METHODS (from the first implementation)

    /**
     * Get full course content (modules, MCQs, videos, etc.)
     */
    fun getCourseContent(courseId: String): Flow<CourseContentEntity?> {
        return courseContentDao.getContentByCourseId(courseId)
    }

    /**
     * Save full AI-generated course content
     */
    fun saveCourseContent(content: CourseContentEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                courseContentDao.insertContent(content)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save course content: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update course content
     */
    fun updateCourseContent(content: CourseContentEntity) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                courseContentDao.updateContent(content)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update course content: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete course content
     */
    fun deleteCourseContent(courseId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                courseContentDao.deleteContentByCourseId(courseId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete course content: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Update last accessed timestamp for course content
     */
    fun updateContentLastAccessed(courseId: String) {
        viewModelScope.launch {
            try {
                courseContentDao.updateLastAccessed(courseId, System.currentTimeMillis())
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update last accessed time: ${e.message}"
            }
        }
    }

    /**
     * Combined method: Get course with its content
     */
    fun getCourseWithContent(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load course
                _selectedCourse.value = repository.getCourseById(courseId)
                // Update last accessed time for content
                updateContentLastAccessed(courseId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load course with content: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete course and its associated content
     */
    fun deleteCourseWithContent(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Delete course content first
                courseContentDao.deleteContentByCourseId(courseId)
                // Then delete the course
                repository.deleteCourseById(courseId)
                refreshCourses()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete course with content: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
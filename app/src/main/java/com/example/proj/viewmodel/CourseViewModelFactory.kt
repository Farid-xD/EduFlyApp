// File: com/example/proj/viewmodel/CourseViewModelFactory.kt
package com.example.proj.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proj.database.AppDatabase
import com.example.proj.repository.CourseRepository

class CourseViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = CourseRepository(database.courseDao())


            return CourseViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
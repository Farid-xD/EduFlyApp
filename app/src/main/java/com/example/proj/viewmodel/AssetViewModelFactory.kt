package com.example.proj.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.proj.database.AppDatabase
import com.example.proj.repository.AssetRepository

class AssetViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssetViewModel::class.java)) {
            val database = AppDatabase.getDatabase(application)
            val repository = AssetRepository(database.assetDao())
            return AssetViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
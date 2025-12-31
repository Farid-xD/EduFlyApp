package com.example.proj.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.proj.database.AppDatabase
import com.example.proj.models.Asset
import com.example.proj.repository.AssetRepository
import kotlinx.coroutines.launch

class AssetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AssetRepository

    // LiveData for UI
    private val _assets = MutableLiveData<List<Asset>>()
    private val _selectedAsset = MutableLiveData<Asset?>()
    private val _isLoading = MutableLiveData(false)
    private val _error = MutableLiveData<String?>()

    val assets: MutableLiveData<List<Asset>> get() = _assets
    val selectedAsset: MutableLiveData<Asset?> get() = _selectedAsset
    val isLoading: MutableLiveData<Boolean> get() = _isLoading
    val error: MutableLiveData<String?> get() = _error

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AssetRepository(database.assetDao())
    }

    // Load all assets
    fun loadAllAssets() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAllAssets().collect { assetList ->
                    _assets.value = assetList
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load assets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load assets by course ID
    fun loadAssetsByCourse(courseId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAssetsByCourseId(courseId).collect { assetList ->
                    _assets.value = assetList
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load assets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load assets by type
    fun loadAssetsByType(assetType: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAssetsByType(assetType).collect { assetList ->
                    _assets.value = assetList
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load assets: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get asset by ID
    fun loadAssetById(assetId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.getAssetById(assetId).collect { asset ->
                    _selectedAsset.value = asset
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load asset: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add asset
    fun addAsset(asset: Asset) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.upsertAsset(asset)
                // Refresh the list
                if (asset.courseId.isNotEmpty()) {
                    loadAssetsByCourse(asset.courseId)
                } else {
                    loadAllAssets()
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to add asset: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update asset
    fun updateAsset(asset: Asset) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.upsertAsset(asset)
                // Refresh the current view
                if (asset.courseId.isNotEmpty()) {
                    loadAssetsByCourse(asset.courseId)
                }
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to update asset: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Delete asset
    fun deleteAsset(asset: Asset) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.deleteAsset(asset)
                // Remove from current list
                val currentAssets = _assets.value?.toMutableList() ?: mutableListOf()
                currentAssets.removeAll { it.id == asset.id }
                _assets.value = currentAssets
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to delete asset: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Clear error message
    fun clearError() {
        _error.value = null
    }

    // Clear selected asset
    fun clearSelectedAsset() {
        _selectedAsset.value = null
    }
}
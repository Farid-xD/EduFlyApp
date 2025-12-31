package com.example.proj.repository

import com.example.proj.database.AssetDao
import com.example.proj.models.Asset
import com.example.proj.utils.AssetMapper  // Import AssetMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AssetRepository(
    private val assetDao: AssetDao
) {

    // Get all assets as Flow
    fun getAllAssets(): Flow<List<Asset>> {
        return assetDao.getAllAssets().map { entities ->
            // Use AssetMapper instead of extension function
            AssetMapper.toAssetList(entities)
        }
    }

    // Get assets by course ID
    fun getAssetsByCourseId(courseId: String): Flow<List<Asset>> {
        return assetDao.getAssetsByCourseId(courseId).map { entities ->
            // Use AssetMapper instead of extension function
            AssetMapper.toAssetList(entities)
        }
    }

    // Get asset by ID
    fun getAssetById(assetId: String): Flow<Asset?> {
        return assetDao.getAssetById(assetId).map { entity ->
            // Use AssetMapper instead of extension function
            entity?.let { AssetMapper.toAsset(it) }
        }
    }

    // Get assets by type
    fun getAssetsByType(assetType: String): Flow<List<Asset>> {
        return assetDao.getAssetsByType(assetType).map { entities ->
            // Use AssetMapper instead of extension function
            AssetMapper.toAssetList(entities)
        }
    }

    // Insert or update asset
    suspend fun upsertAsset(asset: Asset) {
        // Use AssetMapper instead of extension function
        assetDao.upsertAsset(AssetMapper.toEntity(asset))
    }

    // Delete asset
    suspend fun deleteAsset(asset: Asset) {
        // Use AssetMapper instead of extension function
        assetDao.deleteAsset(AssetMapper.toEntity(asset))
    }

    // Delete asset by ID
    suspend fun deleteAssetById(assetId: String) {
        assetDao.deleteAssetById(assetId)
    }

    // Clear all assets
    suspend fun clearAllAssets() {
        assetDao.clearAllAssets()
    }
}
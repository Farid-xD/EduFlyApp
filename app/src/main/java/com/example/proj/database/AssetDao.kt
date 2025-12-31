package com.example.proj.database

import androidx.room.*
import com.example.proj.database.entities.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {

    @Query("SELECT * FROM assets")
    fun getAllAssets(): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE course_id = :courseId ORDER BY asset_order ASC")
    fun getAssetsByCourseId(courseId: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE asset_id = :assetId")
    fun getAssetById(assetId: String): Flow<AssetEntity?>

    @Query("SELECT * FROM assets WHERE type = :assetType")
    fun getAssetsByType(assetType: String): Flow<List<AssetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsset(asset: AssetEntity)

    @Delete
    suspend fun deleteAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE asset_id = :assetId")
    suspend fun deleteAssetById(assetId: String)

    @Query("DELETE FROM assets")
    suspend fun clearAllAssets()
}
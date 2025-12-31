package com.example.proj.services

import android.util.Log
import com.example.proj.api.YouTubeApiClient
import com.example.proj.models.VideoSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class VideoService {

    companion object {
        private const val TAG = "VideoService"

        suspend fun enhanceVideoSuggestions(
            videoSuggestions: List<VideoSuggestion>
        ): List<VideoSuggestion> {
            return try {
                val enhancedVideos = mutableListOf<VideoSuggestion>()

                for (video in videoSuggestions) {
                    try {
                        Log.d(TAG, "Processing video: ${video.title}")

                        // If video already has a search query, keep it
                        if (video.searchQuery.isNotBlank()) {
                            // Try to get a valid YouTube video ID
                            val youtubeVideoId = getYouTubeVideoId(video.searchQuery)

                            val enhancedVideo = if (youtubeVideoId != null) {
                                // We got a valid video ID
                                video.copy(
                                    videoId = youtubeVideoId,
                                    thumbnailUrl = "https://img.youtube.com/vi/$youtubeVideoId/hqdefault.jpg"
                                )
                            } else {
                                // Keep original, no video ID found
                                video
                            }

                            enhancedVideos.add(enhancedVideo)
                        } else {
                            // No search query, keep original
                            enhancedVideos.add(video)
                        }

                        // Add small delay to avoid rate limiting
                        kotlinx.coroutines.delay(300)

                    } catch (e: Exception) {
                        Log.e(TAG, "Error enhancing video '${video.title}': ${e.message}", e)
                        // Add original video as fallback
                        enhancedVideos.add(video)
                    }
                }

                Log.d(TAG, "Enhanced ${enhancedVideos.size} videos")
                enhancedVideos

            } catch (e: Exception) {
                Log.e(TAG, "Error in enhanceVideoSuggestions: ${e.message}", e)
                videoSuggestions // Return original if enhancement fails
            }
        }

        private suspend fun getYouTubeVideoId(searchQuery: String): String? {
            return withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Getting YouTube video ID for: $searchQuery")

                    // Clean up the query
                    val cleanQuery = searchQuery
                        .replace(Regex("[^a-zA-Z0-9\\s]"), " ")
                        .trim()
                        .take(50) // Limit query length

                    if (cleanQuery.isBlank()) return@withContext null

                    // Search YouTube
                    val response = YouTubeApiClient.service.searchVideos(
                        query = cleanQuery,
                        maxResults = 1
                    ).execute()

                    if (response.isSuccessful) {
                        val items = response.body()?.items ?: emptyList()
                        if (items.isNotEmpty()) {
                            val videoId = items[0].id.videoId
                            Log.d(TAG, "Found video ID: $videoId for query: $cleanQuery")

                            // Validate video ID format
                            if (videoId.length == 11 && videoId.matches(Regex("[A-Za-z0-9_-]{11}"))) {
                                return@withContext videoId
                            }
                        }
                    }

                    null

                } catch (e: Exception) {
                    Log.e(TAG, "Error getting YouTube video ID: ${e.message}")
                    null
                }
            }
        }
    }
}
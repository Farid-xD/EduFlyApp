package com.example.proj.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    fun searchVideos(
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 5,
        @Query("key") apiKey: String = "AIzaSyC5XG_ueTlqxkmYMDCSSyOPvcbcLx3jfa0"
    ): Call<YouTubeSearchResponse>

    @GET("videos")
    fun getVideoDetails(
        @Query("id") videoId: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("key") apiKey: String = "AIzaSyC5XG_ueTlqxkmYMDCSSyOPvcbcLx3jfa0"
    ): Call<YouTubeVideoResponse>
}

data class YouTubeSearchResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: YouTubeVideoId,
    val snippet: YouTubeSnippet
)

data class YouTubeVideoId(
    val videoId: String
)

data class YouTubeSnippet(
    val title: String,
    val description: String,
    val thumbnails: YouTubeThumbnails,
    val channelTitle: String
)

data class YouTubeThumbnails(
    val medium: YouTubeThumbnail
)

data class YouTubeThumbnail(
    val url: String
)

data class YouTubeVideoResponse(
    val items: List<YouTubeVideoDetail>
)

data class YouTubeVideoDetail(
    val id: String,
    val snippet: YouTubeSnippet,
    val contentDetails: YouTubeContentDetails
)

data class YouTubeContentDetails(
    val duration: String
)

object YouTubeApiClient {
    private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: YouTubeApiService = retrofit.create(YouTubeApiService::class.java)
}
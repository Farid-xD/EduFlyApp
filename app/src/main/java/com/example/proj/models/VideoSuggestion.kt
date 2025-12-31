// VideoSuggestion.kt
package com.example.proj.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoSuggestion(
    val title: String,
    val description: String,
    val searchQuery: String,
    var videoId: String? = null,
    var thumbnailUrl: String? = null,
    var duration: String? = null,
    var channelName: String? = null
) : Parcelable {

    // Helper function to format duration (PT1H30M20S -> 1:30:20)
    fun formatDuration(): String? {
        return duration?.let { isoDuration ->
            try {
                // Remove PT prefix
                var timeStr = isoDuration.substring(2)
                var hours = 0
                var minutes = 0
                var seconds = 0

                // Extract hours
                if (timeStr.contains("H")) {
                    hours = timeStr.substringBefore("H").toInt()
                    timeStr = timeStr.substringAfter("H")
                }

                // Extract minutes
                if (timeStr.contains("M")) {
                    minutes = timeStr.substringBefore("M").toInt()
                    timeStr = timeStr.substringAfter("M")
                }

                // Extract seconds
                if (timeStr.contains("S")) {
                    seconds = timeStr.substringBefore("S").toInt()
                }

                // Format
                return if (hours > 0) {
                    String.format("%d:%02d:%02d", hours, minutes, seconds)
                } else {
                    String.format("%d:%02d", minutes, seconds)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}
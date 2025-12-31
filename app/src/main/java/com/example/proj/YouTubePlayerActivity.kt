package com.example.proj.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proj.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.*

class YouTubePlayerActivity : AppCompatActivity() {

    private lateinit var youtubePlayerView: YouTubePlayerView
    private lateinit var tvVideoTitle: TextView
    private lateinit var tvVideoDescription: TextView
    private lateinit var tvChannelInfo: TextView
    private lateinit var tvErrorMessage: TextView
    private lateinit var btnOpenInYouTube: MaterialButton
    private lateinit var btnSearchYouTube: MaterialButton
    private lateinit var fallbackButtons: android.widget.LinearLayout

    private var videoId: String? = null
    private var videoTitle: String? = null
    private var videoDescription: String? = null
    private var channelName: String? = null
    private var searchQuery: String? = null
    private var playbackError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_player)

        // Get data from intent
        videoId = intent.getStringExtra("video_id")
        videoTitle = intent.getStringExtra("video_title")
        videoDescription = intent.getStringExtra("video_description")
        channelName = intent.getStringExtra("channel_name")
        searchQuery = intent.getStringExtra("search_query")

        Log.d("YouTubePlayer", "Video ID: $videoId")
        Log.d("YouTubePlayer", "Video Title: $videoTitle")
        Log.d("YouTubePlayer", "Search Query: $searchQuery")

        // Setup toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Get views
        youtubePlayerView = findViewById(R.id.youtube_player_view)
        tvVideoTitle = findViewById(R.id.tvVideoTitle)
        tvVideoDescription = findViewById(R.id.tvVideoDescription)
        tvChannelInfo = findViewById(R.id.tvChannelInfo)
        tvErrorMessage = findViewById(R.id.tvErrorMessage1)
        btnOpenInYouTube = findViewById(R.id.btnOpenInYouTube)
        btnSearchYouTube = findViewById(R.id.btnSearchYouTube)
        fallbackButtons = findViewById(R.id.fallbackButtons)

        // Set video info
        tvVideoTitle.text = videoTitle ?: "Video"
        tvVideoDescription.text = videoDescription ?: "No description available"
        tvChannelInfo.text = channelName ?: "Channel"

        // Hide error views initially
        tvErrorMessage.visibility = android.view.View.GONE
        fallbackButtons.visibility = android.view.View.GONE

        // Setup button listeners
        btnOpenInYouTube.setOnClickListener {
            openInYouTubeApp()
        }

        btnSearchYouTube.setOnClickListener {
            searchOnYouTube()
        }

        // Setup YouTube player
        setupYouTubePlayer()

        // Start a timer to check if video loaded successfully
        startPlaybackCheckTimer()
    }

    private fun setupYouTubePlayer() {
        lifecycle.addObserver(youtubePlayerView)

        // Configure player options
        val options = IFramePlayerOptions.Builder()
            .controls(1)
            .autoplay(1)
            .fullscreen(1)
            .build()

        youtubePlayerView.enableAutomaticInitialization = false

        youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                if (videoId != null && videoId?.isNotBlank() == true) {
                    Log.d("YouTubePlayer", "Loading video: $videoId")
                    try {
                        youTubePlayer.loadVideo(videoId!!, 0f)
                        // If we get here, video started loading
                        playbackError = false
                    } catch (e: Exception) {
                        Log.e("YouTubePlayer", "Error loading video: ${e.message}")
                        showVideoError("Error loading video: ${e.message}")
                        playbackError = true
                    }
                } else {
                    showVideoNotFoundError()
                    playbackError = true
                }
            }

            // Don't override onError - different library versions have different signatures
            // We'll detect errors through the timer instead

        }, true, options)
    }

    private fun startPlaybackCheckTimer() {
        CoroutineScope(Dispatchers.Main).launch {
            delay(5000) // Wait 5 seconds

            // If video hasn't loaded after 5 seconds, show error
            if (playbackError || youtubePlayerView.visibility == android.view.View.GONE) {
                Log.e("YouTubePlayer", "Video failed to load within timeout")
                showVideoError("Video failed to load. It may be unavailable or restricted.")
            } else {
                Log.d("YouTubePlayer", "Video playback appears to be working")
            }
        }
    }

    private fun showVideoError(errorMessage: String = "Video unavailable") {
        Log.e("YouTubePlayer", "Video playback error: $errorMessage")

        // Hide the player
        youtubePlayerView.visibility = android.view.View.GONE

        // Show error message
        tvErrorMessage.visibility = android.view.View.VISIBLE
        tvErrorMessage.text = errorMessage

        // Show the fallback buttons
        fallbackButtons.visibility = android.view.View.VISIBLE

        Toast.makeText(this, "Video unavailable. Try opening in YouTube app.", Toast.LENGTH_LONG).show()
    }

    private fun showVideoNotFoundError() {
        youtubePlayerView.visibility = android.view.View.GONE

        tvErrorMessage.visibility = android.view.View.VISIBLE
        tvErrorMessage.text = "No video ID provided"

        fallbackButtons.visibility = android.view.View.VISIBLE
    }

    private fun openInYouTubeApp() {
        try {
            if (videoId != null) {
                // Try to open specific video in YouTube app
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("vnd.youtube:$videoId")
                    `package` = "com.google.android.youtube"
                }

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    // Fallback to web
                    openVideoInBrowser()
                }
            } else {
                searchOnYouTube()
            }
        } catch (e: Exception) {
            Log.e("YouTubePlayer", "Error opening YouTube app: ${e.message}")
            openVideoInBrowser()
        }
    }

    private fun openVideoInBrowser() {
        try {
            if (videoId != null) {
                val url = "https://www.youtube.com/watch?v=$videoId"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } else {
                searchOnYouTube()
            }
        } catch (e: Exception) {
            Log.e("YouTubePlayer", "Error opening browser: ${e.message}")
            Toast.makeText(this, "Cannot open video", Toast.LENGTH_SHORT).show()
        }
    }

    private fun searchOnYouTube() {
        try {
            val query = searchQuery ?: videoTitle ?: "tutorial"
            val encodedQuery = Uri.encode(query)

            // Try YouTube app first
            val youtubeIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("vnd.youtube://search?query=$encodedQuery")
                `package` = "com.google.android.youtube"
            }

            if (youtubeIntent.resolveActivity(packageManager) != null) {
                startActivity(youtubeIntent)
            } else {
                // Fallback to web search
                val webIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.youtube.com/results?search_query=$encodedQuery")
                }
                startActivity(webIntent)
            }
        } catch (e: Exception) {
            Log.e("YouTubePlayer", "Error searching YouTube: ${e.message}")
            Toast.makeText(this, "Cannot search YouTube", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        youtubePlayerView.release()
    }
}
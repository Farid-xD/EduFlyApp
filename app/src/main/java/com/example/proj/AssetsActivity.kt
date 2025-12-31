package com.example.proj.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proj.R
import com.example.proj.adapters.SimpleAssetsAdapter
import com.example.proj.models.Asset
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class AssetsActivity : AppCompatActivity() {

    // All views from XML - now including the ones that were missing
    private lateinit var btnBack: Button
    private lateinit var backButton: ImageView
    private lateinit var btnAddAsset: Button
    private lateinit var btnRefresh: Button
    private lateinit var btnLogout: Button
    private lateinit var searchIcon: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: MaterialButton
    private lateinit var allChip: Chip
    private lateinit var documentsChip: Chip
    private lateinit var imagesChip: Chip
    private lateinit var videosChip: Chip
    private lateinit var audioChip: Chip
    private lateinit var assetsRecyclerView: RecyclerView
    private lateinit var loadingLayout: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyLayout: LinearLayout
    private lateinit var emptyState: TextView
    private lateinit var uploadButton: MaterialButton
    private lateinit var errorLayout: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var retryButton: MaterialButton
    private lateinit var dashboardTab: LinearLayout
    private lateinit var coursesTab: LinearLayout
    private lateinit var assetsTab: LinearLayout
    private lateinit var profileTab: LinearLayout

    private lateinit var assetAdapter: SimpleAssetsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assets)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupFilterChips()
        setupBottomNavigation()
        loadAssets()
    }

    private fun initializeViews() {
        // App Bar buttons
        btnBack = findViewById(R.id.btnBack)
        backButton = findViewById(R.id.backButton)
        btnAddAsset = findViewById(R.id.btnAddAsset)
        btnRefresh = findViewById(R.id.btnRefresh)
        btnLogout = findViewById(R.id.btnLogout)
        searchIcon = findViewById(R.id.searchIcon)

        // Search Bar
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)

        // Filter Chips
        allChip = findViewById(R.id.allChip)
        documentsChip = findViewById(R.id.documentsChip)
        imagesChip = findViewById(R.id.imagesChip)
        videosChip = findViewById(R.id.videosChip)
        audioChip = findViewById(R.id.audioChip)

        // Main Content Area
        assetsRecyclerView = findViewById(R.id.assetsRecyclerView)
        loadingLayout = findViewById(R.id.loadingLayout)
        progressBar = findViewById(R.id.progressBar)
        emptyLayout = findViewById(R.id.emptyLayout)
        emptyState = findViewById(R.id.emptyState)
        uploadButton = findViewById(R.id.uploadButton)
        errorLayout = findViewById(R.id.errorLayout)
        errorMessage = findViewById(R.id.errorMessage)
        retryButton = findViewById(R.id.retryButton)

        // Bottom Navigation
        dashboardTab = findViewById(R.id.dashboardTab)
        coursesTab = findViewById(R.id.coursesTab)
        assetsTab = findViewById(R.id.assetsTab)
        profileTab = findViewById(R.id.profileTab)
    }

    private fun setupRecyclerView() {
        assetsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        assetAdapter = SimpleAssetsAdapter(
            assets = emptyList(),
            onItemClick = { asset ->
                onAssetItemClick(asset)
            },
            onDownloadClick = { asset ->
                onDownloadAsset(asset)
            }
        )
        assetsRecyclerView.adapter = assetAdapter
    }

    private fun setupClickListeners() {
        // You can use either btnBack or backButton
        btnBack.setOnClickListener { onBackPressed() }
        backButton.setOnClickListener { onBackPressed() }

        btnAddAsset.setOnClickListener {
            openAddAssetScreen()
        }

        btnRefresh.setOnClickListener {
            loadAssets()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }

        searchIcon.setOnClickListener {
            searchEditText.requestFocus()
        }

        searchButton.setOnClickListener {
            performSearch(searchEditText.text.toString())
        }

        uploadButton.setOnClickListener {
            openAddAssetScreen()
        }

        retryButton.setOnClickListener {
            loadAssets()
        }
    }

    private fun setupFilterChips() {
        val filterChips = listOf(allChip, documentsChip, imagesChip, videosChip, audioChip)

        allChip.setOnClickListener {
            selectFilterChip(allChip, filterChips)
            filterAssetsByType(null)
        }

        documentsChip.setOnClickListener {
            selectFilterChip(documentsChip, filterChips)
            filterAssetsByType("document")
        }

        imagesChip.setOnClickListener {
            selectFilterChip(imagesChip, filterChips)
            filterAssetsByType("image")
        }

        videosChip.setOnClickListener {
            selectFilterChip(videosChip, filterChips)
            filterAssetsByType("video")
        }

        audioChip.setOnClickListener {
            selectFilterChip(audioChip, filterChips)
            filterAssetsByType("audio")
        }
    }

    private fun selectFilterChip(selectedChip: Chip, allChips: List<Chip>) {
        allChips.forEach { chip ->
            chip.isChecked = chip.id == selectedChip.id
        }
    }

    private fun setupBottomNavigation() {
        dashboardTab.setOnClickListener {
            // Navigate to Dashboard
        }

        coursesTab.setOnClickListener {
            // Navigate to Courses
        }

        assetsTab.setOnClickListener {
            // Already here, refresh
            loadAssets()
        }

        profileTab.setOnClickListener {
            // Navigate to Profile
        }
    }

    private fun loadAssets() {
        showLoading()

        lifecycleScope.launch {
            try {
                // Simulate network delay
                Thread.sleep(1000)

                val assets = getSampleAssets()

                if (assets.isEmpty()) {
                    showEmptyState()
                } else {
                    assetAdapter.updateAssets(assets)
                    showContent()
                }

            } catch (e: Exception) {
                showError("Failed to load assets: ${e.localizedMessage}")
            }
        }
    }

    private fun performSearch(query: String) {
        // Implement search logic
    }

    private fun filterAssetsByType(type: String?) {
        // Implement filter logic
    }

    // UI State Management
    private fun showLoading() {
        loadingLayout.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        assetsRecyclerView.visibility = View.GONE
        emptyLayout.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showContent() {
        loadingLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
        assetsRecyclerView.visibility = View.VISIBLE
        emptyLayout.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showEmptyState() {
        loadingLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
        assetsRecyclerView.visibility = View.GONE
        emptyLayout.visibility = View.VISIBLE
        emptyState.visibility = View.VISIBLE
        errorLayout.visibility = View.GONE
    }

    private fun showError(message: String) {
        loadingLayout.visibility = View.GONE
        progressBar.visibility = View.GONE
        assetsRecyclerView.visibility = View.GONE
        emptyLayout.visibility = View.GONE
        emptyState.visibility = View.GONE
        errorLayout.visibility = View.VISIBLE
        errorMessage.text = message
    }

    private fun onAssetItemClick(asset: Asset) {
        // Handle asset click
    }

    private fun onDownloadAsset(asset: Asset) {
        // Handle download
    }

    private fun openAddAssetScreen() {
        // Open add asset screen
    }

    private fun showLogoutDialog() {
        // Show logout dialog
    }

    private fun getSampleAssets(): List<Asset> {
        // Get current timestamp
        val currentTime = System.currentTimeMillis()

        return listOf(
            Asset(
                id = "1",
                courseId = "course1",
                title = "Lecture Notes.pdf",
                description = "Complete lecture notes for week 1",
                type = "document",
                url = "http://example.com/notes.pdf",
                fileSize = 2048L,  // Must be Long (add 'L')
                order = 1,
                createdAt = currentTime - 7 * 24 * 60 * 60 * 1000, // 7 days ago
                updatedAt = currentTime - 7 * 24 * 60 * 60 * 1000
            ),
            Asset(
                id = "2",
                courseId = "course1",
                title = "Class Diagram.png",
                description = "UML diagram for the project",
                type = "image",
                url = "http://example.com/diagram.png",
                fileSize = 512L,  // Must be Long (add 'L')
                order = 2,
                createdAt = currentTime - 6 * 24 * 60 * 60 * 1000, // 6 days ago
                updatedAt = currentTime - 6 * 24 * 60 * 60 * 1000
            ),
            Asset(
                id = "3",
                courseId = "course1",
                title = "Introduction Video.mp4",
                description = "Welcome video for the course",
                type = "video",
                url = "http://example.com/video.mp4",
                duration = 300,  // 5 minutes in seconds
                fileSize = 10240L,  // Must be Long (add 'L')
                order = 3,
                createdAt = currentTime - 5 * 24 * 60 * 60 * 1000, // 5 days ago
                updatedAt = currentTime - 5 * 24 * 60 * 60 * 1000
            ),
            Asset(
                id = "4",
                courseId = "course1",
                title = "Audio Lecture.mp3",
                description = "Audio recording of lecture",
                type = "audio",
                url = "http://example.com/audio.mp3",
                duration = 1200,  // 20 minutes in seconds
                fileSize = 3072L,  // Must be Long (add 'L')
                order = 4,
                createdAt = currentTime - 4 * 24 * 60 * 60 * 1000, // 4 days ago
                updatedAt = currentTime - 4 * 24 * 60 * 60 * 1000
            ),
            Asset(
                id = "5",
                courseId = "course1",
                title = "Assignment Template.docx",
                description = "Template for assignments",
                type = "document",
                url = "http://example.com/template.docx",
                fileSize = 1024L,  // Must be Long (add 'L')
                order = 5,
                createdAt = currentTime - 3 * 24 * 60 * 60 * 1000, // 3 days ago
                updatedAt = currentTime - 3 * 24 * 60 * 60 * 1000
            ),
            Asset(
                id = "6",
                courseId = "course1",
                title = "Syllabus.pdf",
                description = "Course syllabus and schedule",
                type = "document",
                url = "http://example.com/syllabus.pdf",
                fileSize = 1536L,  // Must be Long (add 'L')
                order = 6,
                createdAt = currentTime - 2 * 24 * 60 * 60 * 1000, // 2 days ago
                updatedAt = currentTime - 2 * 24 * 60 * 60 * 1000
            )
        )
    }

    // Handle the unused start function
    @Suppress("unused")
    private fun start() {
        // Your start function logic
    }
}
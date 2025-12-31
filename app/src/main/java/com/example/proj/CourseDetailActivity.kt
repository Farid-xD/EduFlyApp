package com.example.proj.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proj.R
import com.example.proj.adapters.MCQsAdapter
import com.example.proj.adapters.ModulesAdapter
import com.example.proj.adapters.VideosAdapter
import com.example.proj.database.entities.CourseContentEntity
import com.example.proj.models.*
import com.example.proj.services.VideoService
import com.example.proj.viewmodel.CourseViewModel
import com.example.proj.viewmodel.CourseViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class CourseDetailActivity : AppCompatActivity() {

    private lateinit var tvCourseTitle: TextView
    private lateinit var tvCourseDescription: TextView
    private lateinit var tvInstructor: TextView
    private lateinit var tvCategory: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvLessonsCount: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvStudentsCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnContinue: Button
    private lateinit var btnEnroll: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnMore: ImageView
    private lateinit var btnShare: MaterialButton

    // TabLayout
    private lateinit var tabLayout: TabLayout

    // Content containers
    private lateinit var overviewContent: LinearLayout
    private lateinit var modulesContent: LinearLayout
    private lateinit var assessmentsContent: LinearLayout
    private lateinit var videosContent: LinearLayout
    private lateinit var tvOverviewText: TextView
    private lateinit var rvModules: RecyclerView
    private lateinit var rvAssessments: RecyclerView
    private lateinit var rvVideos: RecyclerView

    // Empty state views
    private lateinit var tvNoModules: TextView
    private lateinit var tvNoAssessments: TextView
    private lateinit var tvNoVideos: TextView

    private val viewModel: CourseViewModel by viewModels {
        CourseViewModelFactory(application)
    }

    private var courseId: String? = null
    private var currentCourse: Course? = null
    private val gson = Gson()

    companion object {
        private const val TAG = "CourseDetail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_detail)

        courseId = intent.getStringExtra("course_id")
        Log.d(TAG, "Opening course: $courseId")

        initializeViews()
        setupRecyclerViews()
        setupTabLayout()
        setupActionListeners()
        loadCourseData()
    }

    private fun initializeViews() {
        tvCourseTitle = findViewById(R.id.tvCourseTitle)
        tvCourseDescription = findViewById(R.id.tvCourseDescription)
        tvInstructor = findViewById(R.id.tvInstructor)
        tvCategory = findViewById(R.id.tvCategory)
        tvProgress = findViewById(R.id.tvProgress)
        tvLessonsCount = findViewById(R.id.tvLessonsCount)
        tvDuration = findViewById(R.id.tvDuration)
        tvStudentsCount = findViewById(R.id.tvStudentsCount)
        progressBar = findViewById(R.id.progressBar)
        btnContinue = findViewById(R.id.btnContinue)
        btnEnroll = findViewById(R.id.btnenroll)
        btnBack = findViewById(R.id.btnBack)
        btnMore = findViewById(R.id.btnMore)
        btnShare = findViewById(R.id.btnShare)

        // TabLayout
        tabLayout = findViewById(R.id.tabLayout)

        // Content containers
        overviewContent = findViewById(R.id.overviewContent)
        modulesContent = findViewById(R.id.modulesContent)
        assessmentsContent = findViewById(R.id.assessmentsContent)
        videosContent = findViewById(R.id.videosContent)
        tvOverviewText = findViewById(R.id.tvOverviewText)
        rvModules = findViewById(R.id.rvModules)
        rvAssessments = findViewById(R.id.rvAssessments)
        rvVideos = findViewById(R.id.rvVideos)

        // Initialize empty state TextViews
        tvNoModules = createEmptyStateView("No modules available yet")
        tvNoAssessments = createEmptyStateView("No assessments available yet")
        tvNoVideos = createEmptyStateView("No video recommendations available for this course.")
    }

    private fun createEmptyStateView(message: String): TextView {
        return TextView(this).apply {
            text = message
            textSize = 14f
            setTextColor(getColor(R.color.text_secondary))
            setPadding(32, 32, 32, 32)
            gravity = android.view.Gravity.CENTER
        }
    }

    private fun setupRecyclerViews() {
        // Setup all RecyclerViews with LinearLayoutManager
        rvModules.apply {
            layoutManager = LinearLayoutManager(this@CourseDetailActivity)
            setHasFixedSize(true)
        }

        rvAssessments.apply {
            layoutManager = LinearLayoutManager(this@CourseDetailActivity)
            setHasFixedSize(true)
        }

        rvVideos.apply {
            layoutManager = LinearLayoutManager(this@CourseDetailActivity)
            setHasFixedSize(true)
        }
    }

    private fun setupTabLayout() {
        // Add tabs to TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("Overview"))
        tabLayout.addTab(tabLayout.newTab().setText("Modules"))
        tabLayout.addTab(tabLayout.newTab().setText("Assessments"))
        tabLayout.addTab(tabLayout.newTab().setText("Videos"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        Log.d(TAG, "Overview tab selected")
                        showTab(TabType.OVERVIEW)
                    }
                    1 -> {
                        Log.d(TAG, "Modules tab selected")
                        showTab(TabType.MODULES)
                    }
                    2 -> {
                        Log.d(TAG, "Assessments tab selected")
                        showTab(TabType.ASSESSMENTS)
                    }
                    3 -> {
                        Log.d(TAG, "Videos tab selected")
                        showTab(TabType.VIDEOS)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showTab(tab: TabType) {
        // Hide all content
        overviewContent.visibility = View.GONE
        modulesContent.visibility = View.GONE
        assessmentsContent.visibility = View.GONE
        videosContent.visibility = View.GONE

        // Show selected tab
        when (tab) {
            TabType.OVERVIEW -> {
                overviewContent.visibility = View.VISIBLE
            }
            TabType.MODULES -> {
                modulesContent.visibility = View.VISIBLE
                checkModulesContent()
            }
            TabType.ASSESSMENTS -> {
                assessmentsContent.visibility = View.VISIBLE
                checkAssessmentsContent()
            }
            TabType.VIDEOS -> {
                videosContent.visibility = View.VISIBLE
                checkVideosContent()
            }
        }
    }

    private fun checkModulesContent() {
        val adapter = rvModules.adapter as? ModulesAdapter
        if (adapter == null || adapter.itemCount == 0) {
            Log.d(TAG, "No modules to display")
            showEmptyState(modulesContent, tvNoModules)
        }
    }

    private fun checkAssessmentsContent() {
        val adapter = rvAssessments.adapter as? MCQsAdapter
        if (adapter == null || adapter.itemCount == 0) {
            Log.d(TAG, "No assessments to display")
            showEmptyState(assessmentsContent, tvNoAssessments)
        }
    }

    private fun checkVideosContent() {
        val adapter = rvVideos.adapter as? VideosAdapter
        if (adapter == null || adapter.itemCount == 0) {
            Log.d(TAG, "No videos to display")
            showEmptyState(videosContent, tvNoVideos)
        }
    }

    private fun showEmptyState(container: LinearLayout, emptyView: TextView) {
        // Remove empty view if already added
        if (emptyView.parent != null) {
            (emptyView.parent as? LinearLayout)?.removeView(emptyView)
        }
        // Add empty view
        container.addView(emptyView)
    }

    private fun setupActionListeners() {
        btnBack.setOnClickListener { finish() }

        btnMore.setOnClickListener { showMoreOptions() }

        btnShare.setOnClickListener {
            currentCourse?.let { course ->
                val shareText = "Check out: ${course.title}\n\nBy ${course.instructor}"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Course"))
            }
        }

        btnContinue.setOnClickListener {
            val currentProgress = progressBar.progress
            val newProgress = (currentProgress + 10).coerceAtMost(100)

            progressBar.progress = newProgress
            tvProgress.text = "$newProgress%"

            courseId?.let { id ->
                viewModel.updateProgress(id, newProgress)
            }

            if (newProgress >= 100) {
                Toast.makeText(this, "🎉 Course Completed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Progress: $newProgress%", Toast.LENGTH_SHORT).show()
            }
        }

        btnEnroll.setOnClickListener {
            courseId?.let { id ->
                viewModel.enrollInCourse(id)
                btnEnroll.visibility = View.GONE
                btnContinue.visibility = View.VISIBLE
                Toast.makeText(this, "✅ Enrolled!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadCourseData() {
        courseId?.let { id ->
            Log.d(TAG, "Loading course data for: $id")

            // Load course basic data
            viewModel.getCourseById(id).observe(this, Observer { course ->
                if (course != null) {
                    Log.d(TAG, "Course loaded: ${course.title}")
                    currentCourse = course
                    displayCourse(course)
                    loadCourseContent(id)
                } else {
                    Log.e(TAG, "Course not found: $id")
                    Toast.makeText(this, "Course not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
        } ?: run {
            Log.e(TAG, "No course ID provided")
            Toast.makeText(this, "Invalid course", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun displayCourse(course: Course) {
        // Remove markdown formatting from title
        tvCourseTitle.text = cleanMarkdown(course.title)
        tvCourseDescription.text = cleanMarkdown(course.description).take(200)
        tvInstructor.text = course.instructor
        tvCategory.text = course.category
        progressBar.progress = course.progress
        tvProgress.text = "${course.progress}%"
        tvLessonsCount.text = "${course.lessons}"
        tvDuration.text = course.duration
        tvStudentsCount.text = "${course.students}"

        btnContinue.visibility = if (course.isEnrolled) View.VISIBLE else View.GONE
        btnEnroll.visibility = if (course.isEnrolled) View.GONE else View.VISIBLE
    }

    private fun loadCourseContent(courseId: String) {
        Log.d(TAG, "Loading course content for: $courseId")

        // Load full AI-generated content from database
        viewModel.getCourseContent(courseId).asLiveData().observe(this, Observer { contentEntity ->
            if (contentEntity != null) {
                Log.d(TAG, "Course content found, checking properties...")
                Log.d(TAG, "modulesJson length: ${contentEntity.modulesJson.length}")
                Log.d(TAG, "mcqQuestions length: ${contentEntity.mcqQuestions.length}")
                Log.d(TAG, "videoSuggestions length: ${contentEntity.videoSuggestions.length}")

                displayCourseContent(contentEntity)
            } else {
                Log.w(TAG, "No course content found in database, using fallback")
                displayFallbackContent()
            }
        })
    }

    private fun displayFallbackContent() {
        Log.d(TAG, "Using fallback content")

        // Show course description as overview
        val fallbackText = currentCourse?.description ?: "No content available yet. Generate course content to see modules, assessments, and video recommendations."
        tvOverviewText.text = cleanMarkdown(fallbackText)

        // Create default modules
        val lessonCount = currentCourse?.lessons ?: 4
        val defaultModules = List(lessonCount) { index ->
            CourseModule(
                moduleNumber = index + 1,
                title = "Module ${index + 1}: ${currentCourse?.title ?: "Course Topic"}",
                topics = listOf("Introduction", "Key Concepts", "Practice Exercises"),
                description = "Learn essential concepts in this module",
                isCompleted = false
            )
        }
        setupModulesAdapter(defaultModules)

        // Create sample MCQs
        val sampleMCQs = listOf(
            MCQQuestion(
                id = "1",
                question = "Sample Question: What is the main purpose of ${currentCourse?.title ?: "this course"}?",
                options = listOf("Option A", "Option B", "Option C", "Option D"),
                correctAnswer = 0,
                explanation = "This is a sample question. Real questions will be generated by AI."
            )
        )
        setupMCQsAdapter(sampleMCQs)

        // Create sample video suggestions
        val sampleVideos = listOf(
            VideoSuggestion(
                title = "Introduction to ${currentCourse?.title ?: "Course Topic"}",
                description = "Beginner-friendly tutorial covering basics",
                searchQuery = "${currentCourse?.title ?: "tutorial"} for beginners",
                videoId = null,
                channelName = "Sample Channel"
            )
        )
        setupVideosAdapter(sampleVideos)

        Toast.makeText(this, "Using sample content - AI content not available", Toast.LENGTH_LONG).show()
    }

    private fun displayCourseContent(content: CourseContentEntity) {
        Log.d(TAG, "=== DISPLAYING COURSE CONTENT ===")

        // Display overview - clean markdown and show full content
        val overviewText = if (content.overview.isNotBlank()) {
            cleanMarkdown(content.overview)
        } else if (content.fullContent.isNotBlank()) {
            cleanMarkdown(content.fullContent) // Show FULL content, not truncated
        } else {
            cleanMarkdown(currentCourse?.description ?: "No overview available")
        }

        tvOverviewText.text = overviewText
        Log.d(TAG, "Overview set, length: ${overviewText.length}")

        // Parse and display modules
        parseAndDisplayModules(content)

        // Parse and display MCQs
        parseAndDisplayMCQs(content)

        // Parse and display videos using coroutine
        lifecycleScope.launch {
            parseAndDisplayVideos(content)
        }
    }

    // Helper function to clean markdown symbols
    private fun cleanMarkdown(text: String): String {
        return text
            .replace(Regex("\\*\\*|\\*|#|`|_"), "") // Remove **, *, #, `, _
            .replace(Regex("\\[.*?\\]\\(.*?\\)")) { match -> // Replace [text](url) with just text
                match.value.substringAfter("[").substringBefore("]")
            }
            .replace(Regex("\\n{3,}"), "\n\n") // Replace 3+ newlines with 2
            .trim()
    }

    // Helper function to extract URL from markdown link
    private fun extractUrlFromMarkdown(text: String): String {
        // If text is in markdown format [text](url), extract the url
        val pattern = Regex("""\[.*?\]\((.*?)\)""")
        val match = pattern.find(text)
        return match?.groupValues?.get(1) ?: text
    }

    private fun parseAndDisplayModules(content: CourseContentEntity) {
        try {
            if (content.modulesJson.isNotBlank() && content.modulesJson != "[]") {
                Log.d(TAG, "Parsing modules JSON...")

                val modules: List<CourseModule> = gson.fromJson(
                    content.modulesJson,
                    object : TypeToken<List<CourseModule>>() {}.type
                )

                Log.d(TAG, "Successfully parsed ${modules.size} modules")

                if (modules.isNotEmpty()) {
                    // Clean module titles and descriptions
                    val cleanedModules = modules.map { module ->
                        module.copy(
                            title = cleanMarkdown(module.title),
                            description = cleanMarkdown(module.description),
                            topics = module.topics.map { cleanMarkdown(it) }
                        )
                    }
                    setupModulesAdapter(cleanedModules)
                } else {
                    Log.w(TAG, "Parsed modules list is empty, extracting from content")
                    extractModulesFromContent(content.fullContent)
                }
            } else {
                Log.w(TAG, "modulesJson is empty or blank, extracting from content")
                extractModulesFromContent(content.fullContent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing modules JSON: ${e.message}", e)
            extractModulesFromContent(content.fullContent)
        }
    }

    private fun parseAndDisplayMCQs(content: CourseContentEntity) {
        try {
            if (content.mcqQuestions.isNotBlank() && content.mcqQuestions != "[]") {
                Log.d(TAG, "Parsing MCQs JSON...")

                val mcqs: List<MCQQuestion> = gson.fromJson(
                    content.mcqQuestions,
                    object : TypeToken<List<MCQQuestion>>() {}.type
                )

                Log.d(TAG, "Successfully parsed ${mcqs.size} MCQs")

                if (mcqs.isNotEmpty()) {
                    // Clean MCQ text
                    val cleanedMCQs = mcqs.map { mcq ->
                        mcq.copy(
                            question = cleanMarkdown(mcq.question),
                            explanation = cleanMarkdown(mcq.explanation),
                            options = mcq.options.map { cleanMarkdown(it) }
                        )
                    }
                    setupMCQsAdapter(cleanedMCQs)
                } else {
                    Log.w(TAG, "Parsed MCQs list is empty, using fallback")
                    extractMCQsFromContent(content.fullContent)
                }
            } else {
                Log.w(TAG, "mcqQuestions is empty or blank, extracting from content")
                extractMCQsFromContent(content.fullContent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing MCQs JSON: ${e.message}", e)
            extractMCQsFromContent(content.fullContent)
        }
    }

    private suspend fun parseAndDisplayVideos(content: CourseContentEntity) {
        try {
            if (content.videoSuggestions.isNotBlank() && content.videoSuggestions != "[]") {
                Log.d(TAG, "Parsing videos JSON...")

                val videos: List<VideoSuggestion> = gson.fromJson(
                    content.videoSuggestions,
                    object : TypeToken<List<VideoSuggestion>>() {}.type
                )

                Log.d(TAG, "Successfully parsed ${videos.size} videos")

                if (videos.isNotEmpty()) {
                    // Fetch real YouTube data for these videos
                    val enhancedVideos = VideoService.enhanceVideoSuggestions(videos)

                    // Process videos
                    val processedVideos = enhancedVideos.map { video ->
                        // Extract URL from markdown if present
                        val processedSearchQuery = extractUrlFromMarkdown(video.searchQuery)

                        video.copy(
                            title = cleanMarkdown(video.title),
                            description = cleanMarkdown(video.description),
                            searchQuery = processedSearchQuery,
                            videoId = video.videoId,
                            thumbnailUrl = video.thumbnailUrl,
                            channelName = video.channelName?.let { cleanMarkdown(it) }
                        )
                    }

                    setupVideosAdapter(processedVideos)
                } else {
                    Log.w(TAG, "Parsed videos list is empty, using fallback")
                    extractVideosFromContent(content.fullContent)
                }
            } else {
                Log.w(TAG, "videoSuggestions is empty or blank, extracting from content")
                Log.d(TAG, "videoSuggestions value: '${content.videoSuggestions}'")
                extractVideosFromContent(content.fullContent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing videos JSON: ${e.message}", e)
            Log.e(TAG, "JSON that failed: ${content.videoSuggestions.take(500)}...")
            extractVideosFromContent(content.fullContent)
        }
    }

    private fun setupModulesAdapter(modules: List<CourseModule>) {
        Log.d(TAG, "Setting up modules adapter with ${modules.size} items")

        val adapter = ModulesAdapter(modules) { module ->
            showModuleDetails(module)
        }

        rvModules.adapter = adapter
        adapter.notifyDataSetChanged()

        if (modules.isNotEmpty() && tvNoModules.parent != null) {
            (tvNoModules.parent as? LinearLayout)?.removeView(tvNoModules)
        }
    }

    private fun showModuleDetails(module: CourseModule) {
        AlertDialog.Builder(this)
            .setTitle(module.title)
            .setMessage("""
${module.description}

Topics covered:
${module.topics.joinToString("\n• ", "• ")}

${if (module.isCompleted) "✅ Completed" else "⏳ In Progress"}
            """.trimIndent())
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Mark as Complete") { _, _ ->
                Toast.makeText(this, "✅ Module marked as complete!", Toast.LENGTH_SHORT).show()
                // TODO: Update module completion in database
            }
            .show()
    }

    private fun setupMCQsAdapter(mcqs: List<MCQQuestion>) {
        Log.d(TAG, "Setting up MCQs adapter with ${mcqs.size} items")

        val adapter = MCQsAdapter(mcqs) { mcq ->
            showMCQDialog(mcq)
        }

        rvAssessments.adapter = adapter
        adapter.notifyDataSetChanged()

        if (mcqs.isNotEmpty() && tvNoAssessments.parent != null) {
            (tvNoAssessments.parent as? LinearLayout)?.removeView(tvNoAssessments)
        }
    }

    private fun setupVideosAdapter(videos: List<VideoSuggestion>) {
        Log.d(TAG, "Setting up videos adapter with ${videos.size} items")

        val adapter = VideosAdapter(videos) { video ->
            openYouTubeWithSearch(video)
        }

        rvVideos.adapter = adapter
        adapter.notifyDataSetChanged()

        if (videos.isNotEmpty() && tvNoVideos.parent != null) {
            (tvNoVideos.parent as? LinearLayout)?.removeView(tvNoVideos)
        }
    }

    // ====== BEST SOLUTION: YouTube Search with Smart Options ======

    private fun openYouTubeWithSearch(video: VideoSuggestion) {
        Log.d(TAG, "Opening YouTube for: ${video.title}")

        // Get the search query
        val searchQuery = if (video.searchQuery.isNotBlank()) {
            video.searchQuery
        } else {
            video.title
        }

        // Encode the query for URL
        val encodedQuery = Uri.encode(searchQuery)
        val youtubeSearchUrl = "https://www.youtube.com/results?search_query=$encodedQuery"

        Log.d(TAG, "Search query: $searchQuery")
        Log.d(TAG, "Encoded query: $encodedQuery")
        Log.d(TAG, "YouTube URL: $youtubeSearchUrl")

        // Try to open YouTube in the BEST possible way
        openYouTubeSmart(youtubeSearchUrl, searchQuery)
    }

    private fun openYouTubeSmart(youtubeUrl: String, searchQuery: String) {
        try {
            // 1. FIRST: Try to open in any browser with chooser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))

            // Check if ANY app can handle YouTube URLs
            val canHandle = browserIntent.resolveActivity(packageManager) != null

            if (canHandle) {
                // Show chooser to let user select browser
                val chooserTitle = "Search YouTube: \"$searchQuery\""
                val chooser = Intent.createChooser(browserIntent, chooserTitle)

                try {
                    startActivity(chooser)
                    return // Success!
                } catch (e: Exception) {
                    Log.e(TAG, "Chooser failed: ${e.message}")
                    // Continue to direct method
                }

                // Try direct browser intent
                try {
                    startActivity(browserIntent)
                    return // Success!
                } catch (e: Exception) {
                    Log.e(TAG, "Direct browser failed: ${e.message}")
                }
            }

            // 2. SECOND: If browser fails, try YouTube app directly
            try {
                // Try to launch YouTube app
                val youtubePackage = "com.google.android.youtube"
                val youtubeIntent = packageManager.getLaunchIntentForPackage(youtubePackage)

                if (youtubeIntent != null) {
                    startActivity(youtubeIntent)

                    // Show toast with search query
                    Toast.makeText(this, "Search in YouTube: \"$searchQuery\"", Toast.LENGTH_LONG).show()
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "YouTube app launch failed: ${e.message}")
            }

            // 3. THIRD: If nothing works, show the SMART dialog
            showSmartYouTubeDialog(searchQuery, youtubeUrl)

        } catch (e: Exception) {
            Log.e(TAG, "Error opening YouTube: ${e.message}", e)
            showSmartYouTubeDialog(searchQuery, youtubeUrl) // Show dialog as fallback
        }
    }

    private fun showSmartYouTubeDialog(searchQuery: String, youtubeUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Search YouTube")
            .setMessage("""
Search for: "$searchQuery"

Choose an option below:
            """.trimIndent())
            .setPositiveButton("📱 Open in Browser") { _, _ ->
                // Copy URL to clipboard first
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("YouTube Search", youtubeUrl)
                clipboard.setPrimaryClip(clip)

                // Try to open browser one more time
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Copied to clipboard. Please open browser manually.", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("📋 Copy Search") { _, _ ->
                // Copy search query to clipboard
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("YouTube Search", searchQuery)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Search query copied to clipboard", Toast.LENGTH_LONG).show()

                // Also try to open YouTube homepage
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
                    startActivity(intent)
                } catch (e: Exception) {
                    // Ignore if can't open
                }
            }
            .setNeutralButton("🔄 Try Other Apps") { _, _ ->
                // Show all possible options
                showAllYouTubeOptions(searchQuery, youtubeUrl)
            }
            .show()
    }

    private fun showAllYouTubeOptions(searchQuery: String, youtubeUrl: String) {
        val options = arrayOf(
            "Open YouTube Website",
            "Open Google Chrome",
            "Open Firefox",
            "Copy URL Only",
            "Install YouTube App",
            "Cancel"
        )

        AlertDialog.Builder(this)
            .setTitle("More Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openBrowser(youtubeUrl, "Opening YouTube...")
                    1 -> openSpecificBrowser("com.android.chrome", youtubeUrl)
                    2 -> openSpecificBrowser("org.mozilla.firefox", youtubeUrl)
                    3 -> {
                        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("YouTube URL", youtubeUrl)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "URL copied to clipboard", Toast.LENGTH_LONG).show()
                    }
                    4 -> installYouTubeApp()
                    // 5 is Cancel
                }
            }
            .show()
    }

    private fun openBrowser(url: String, message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Cannot open browser", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSpecificBrowser(packageName: String, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.`package` = packageName
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to any browser
            openBrowser(url, "Opening in browser...")
        }
    }

    private fun installYouTubeApp() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.google.android.youtube")
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web Play Store
            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.youtube")
            }
            startActivity(webIntent)
        }
    }

    private fun extractModulesFromContent(content: String) {
        Log.d(TAG, "Extracting modules from content")

        val extractedModules = List(4) { i ->
            CourseModule(
                moduleNumber = i + 1,
                title = "Module ${i + 1}: ${currentCourse?.title ?: "Course"}",
                topics = listOf("Topic 1", "Topic 2", "Topic 3"),
                description = "Learn essential concepts in this module",
                isCompleted = false
            )
        }
        setupModulesAdapter(extractedModules)
    }

    private fun extractMCQsFromContent(content: String) {
        Log.d(TAG, "Extracting MCQs from content")

        val fallbackMCQs = listOf(
            MCQQuestion(
                id = "fallback_1",
                question = "What did you learn from ${currentCourse?.title ?: "this course"}?",
                options = listOf("Important concepts", "Practical skills", "Both", "Nothing yet"),
                correctAnswer = 2,
                explanation = "A good course should teach both concepts and skills"
            )
        )
        setupMCQsAdapter(fallbackMCQs)
    }

    private fun extractVideosFromContent(content: String) {
        Log.d(TAG, "Extracting videos from content")

        val fallbackVideos = listOf(
            VideoSuggestion(
                title = "Learn ${currentCourse?.title ?: "Skills"}",
                description = "YouTube tutorials for better understanding",
                searchQuery = "${currentCourse?.title ?: "tutorial"} for beginners",
                videoId = null,
                channelName = null
            )
        )
        setupVideosAdapter(fallbackVideos)
    }

    private fun showMCQDialog(mcq: MCQQuestion) {
        val options = mcq.options.toTypedArray()
        var selectedOption = -1

        AlertDialog.Builder(this)
            .setTitle("Quiz: ${mcq.question}")
            .setSingleChoiceItems(options, -1) { _, which ->
                selectedOption = which
            }
            .setPositiveButton("Submit") { _, _ ->
                if (selectedOption == mcq.correctAnswer) {
                    Toast.makeText(this, "✅ Correct! ${mcq.explanation}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "❌ Incorrect. ${mcq.explanation}", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMoreOptions() {
        val options = arrayOf("Download Resources", "Save for Later", "Unenroll", "Regenerate Content")
        AlertDialog.Builder(this)
            .setTitle("Course Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, "Downloading resources...", Toast.LENGTH_SHORT).show()
                    1 -> courseId?.let {
                        viewModel.toggleFavorite(it, true)
                        Toast.makeText(this, "Course saved for later", Toast.LENGTH_SHORT).show()
                    }
                    2 -> courseId?.let {
                        viewModel.unenrollFromCourse(it)
                        finish()
                    }
                    3 -> {
                        Toast.makeText(this, "Regenerating content...", Toast.LENGTH_SHORT).show()
                        // TODO: Add AI content regeneration
                    }
                }
            }
            .show()
    }

    enum class TabType {
        OVERVIEW, MODULES, ASSESSMENTS, VIDEOS
    }
}
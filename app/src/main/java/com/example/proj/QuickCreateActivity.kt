package com.example.proj.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.proj.R
import com.example.proj.api.ApiResult
import com.example.proj.api.GeminiApiService
import com.example.proj.database.entities.CourseContentEntity
import com.example.proj.models.Course
import com.example.proj.models.CourseModule
import com.example.proj.models.MCQQuestion
import com.example.proj.models.QuizQuestion
import com.example.proj.models.VideoSuggestion
import com.example.proj.viewmodel.CourseViewModel
import com.example.proj.viewmodel.CourseViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class QuickCreateActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var btnGenerate: MaterialButton
    private lateinit var viewModel: CourseViewModel

    private var selectedFileUri: Uri? = null
    private var selectedFileName: String? = null
    private var selectedFileSize: Long = 0



    // Course details
    private var selectedDifficulty: String = "Beginner"
    private var selectedCategory: String = "General"

    // AI options
    private var includeQuiz = false
    private var includeMCQs = false
    private var includeVideo = false

    private val difficulties = arrayOf("Beginner", "Intermediate", "Advanced")
    private val categories = arrayOf(
        "Programming", "Design", "Business",
        "Marketing", "Data Science", "Personal Development", "General"
    )

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleSelectedFile(uri)
            }
        }
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openFilePicker()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_create)

        // Initialize ViewModel with factory
        viewModel = ViewModelProvider(
            this,
            CourseViewModelFactory(application)
        ).get(CourseViewModel::class.java)

        // Initialize views
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        btnGenerate = findViewById(R.id.btnGenerate)

        setupViews()
        setupListeners()
    }

    private fun setupViews() {
        // Setup difficulty spinner
        val difficultySpinner = findViewById<Spinner>(R.id.spinnerDifficulty)
        val difficultyAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            difficulties
        )
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        difficultySpinner.adapter = difficultyAdapter

        // Setup category spinner
        val categorySpinner = findViewById<Spinner>(R.id.spinnerCategory)
        val categoryAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        // Set default selections
        difficultySpinner.setSelection(0) // Beginner
        categorySpinner.setSelection(categories.size - 1) // General

        // Set listeners for spinners
        difficultySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDifficulty = difficulties[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnUploadFile).setOnClickListener {
            checkStoragePermission()
        }

        findViewById<View>(R.id.btnRemoveFile)?.setOnClickListener {
            removeSelectedFile()
        }

        // AI Options Checkboxes
        findViewById<MaterialCheckBox>(R.id.cbIncludeQuiz)?.setOnCheckedChangeListener { _, isChecked ->
            includeQuiz = isChecked
        }

        findViewById<MaterialCheckBox>(R.id.cbIncludeMCQs)?.setOnCheckedChangeListener { _, isChecked ->
            includeMCQs = isChecked
        }

        findViewById<MaterialCheckBox>(R.id.cbIncludeVideo)?.setOnCheckedChangeListener { _, isChecked ->
            includeVideo = isChecked
        }

        btnGenerate.setOnClickListener {
            generateCourseWithAI()
        }
    }

    private fun generateCourseWithAI() {
        // Get all input values
        val etCourseTitle = findViewById<EditText>(R.id.etCourseTitle)
        val etInstructor = findViewById<EditText>(R.id.etInstructor)
        val etDuration = findViewById<EditText>(R.id.etDuration)
        val etPrice = findViewById<EditText>(R.id.etPrice)
        val etPrompt = findViewById<EditText>(R.id.etPrompt)

        val courseTitle = etCourseTitle.text?.toString()?.trim() ?: ""
        val instructorName = etInstructor.text?.toString()?.trim() ?: ""
        val duration = etDuration.text?.toString()?.trim() ?: getString(R.string.default_duration)
        val price = etPrice.text?.toString()?.trim() ?: getString(R.string.free)
        val courseDescription = etPrompt.text?.toString()?.trim() ?: ""

        // Validate inputs
        if (courseTitle.isEmpty()) {
            etCourseTitle.error = "Please enter a course title"
            etCourseTitle.requestFocus()
            return
        }

        if (instructorName.isEmpty()) {
            etInstructor.error = "Please enter instructor name"
            etInstructor.requestFocus()
            return
        }

        if (courseDescription.isEmpty()) {
            etPrompt.error = "Please describe your course"
            etPrompt.requestFocus()
            return
        }

        // Show loading
        showLoading(true)
        btnGenerate.text = getString(R.string.ai_generating)

        // Build comprehensive prompt for Gemini
        val fullPrompt = buildAIPrompt(
            courseTitle,
            courseDescription,
            instructorName,
            duration,
            selectedDifficulty,
            selectedCategory,
            selectedFileUri
        )

        // Call Gemini API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = GeminiApiService.generateCourseContent(
                    prompt = fullPrompt,
                    includeQuiz = includeQuiz,
                    includeMCQs = includeMCQs,
                    includeVideo = includeVideo
                )

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    btnGenerate.text = getString(R.string.generate_course_ai)

                    when (result) {
                        is ApiResult.Success -> {
                            // Parse the AI response and create course
                            val generatedContent = result.data
                            createCourseFromAIResponse(
                                courseTitle,
                                instructorName,
                                duration,
                                price,
                                courseDescription,
                                generatedContent
                            )
                        }
                        is ApiResult.Error -> {
                            showErrorDialog(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    btnGenerate.text = getString(R.string.generate_course_ai)
                    showErrorDialog("Error: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun buildAIPrompt(
        title: String,
        description: String,
        instructor: String,
        duration: String,
        difficulty: String,
        category: String,
        fileUri: Uri?
    ): String {
        var prompt = """
            Create a comprehensive, professional course with the following details:
            
            COURSE TITLE: $title
            INSTRUCTOR: $instructor
            DURATION: $duration
            DIFFICULTY: $difficulty
            CATEGORY: $category
            
            DESCRIPTION: $description
            
            IMPORTANT: Please structure the response EXACTLY as follows:
            
            1. COURSE OVERVIEW
            [Provide a comprehensive overview here]
            
            2. LEARNING OBJECTIVES
            [List 3-5 specific learning objectives with bullet points]
            
            3. COURSE MODULES
            Module 1: [Module Title]
            - Topic 1
            - Topic 2
            - Topic 3
            [Brief description]
            
            Module 2: [Module Title]
            - Topic 1
            - Topic 2
            - Topic 3
            [Brief description]
            
            Module 3: [Module Title]
            - Topic 1
            - Topic 2
            - Topic 3
            [Brief description]
            
            Module 4: [Module Title]
            - Topic 1
            - Topic 2
            - Topic 3
            [Brief description]
        """.trimIndent()

        // Add quiz section if selected
        if (includeQuiz) {
            prompt += """
                
                4. ASSESSMENT QUESTIONS
                Question 1: [Open-ended question]
                Answer guidelines: [Brief answer]
                
                Question 2: [Open-ended question]
                Answer guidelines: [Brief answer]
                
                Question 3: [Open-ended question]
                Answer guidelines: [Brief answer]
            """.trimIndent()
        }

        // Add MCQs if selected
        if (includeMCQs) {
            prompt += """
                
                5. MULTIPLE CHOICE QUESTIONS
                MCQ 1: [Question]
                Options:
                A) [Option A]
                B) [Option B]
                C) [Option C]
                D) [Option D]
                Correct Answer: [Letter]
                Explanation: [Why this is correct]
                
                MCQ 2: [Question]
                Options:
                A) [Option A]
                B) [Option B]
                C) [Option C]
                D) [Option D]
                Correct Answer: [Letter]
                Explanation: [Why this is correct]
                
                MCQ 3: [Question]
                Options:
                A) [Option A]
                B) [Option B]
                C) [Option C]
                D) [Option D]
                Correct Answer: [Letter]
                Explanation: [Why this is correct]
            """.trimIndent()
        }

        // Add video section if selected
        if (includeVideo) {
            prompt += """
                
                6. RECOMMENDED VIDEOS
                Video 1: "[Video Title]" - [Brief description of what the video covers]
                Video 2: "[Video Title]" - [Brief description of what the video covers]
                Video 3: "[Video Title]" - [Brief description of what the video covers]
            """.trimIndent()
        }

        prompt += """
            
            7. FINAL PROJECT
            [Describe a practical project with key deliverables]
            
            8. ADDITIONAL RESOURCES
            - [Resource 1: Book/Website/Tool]
            - [Resource 2: Book/Website/Tool]
            - [Resource 3: Book/Website/Tool]
        """.trimIndent()

        // Add file content if available
        if (fileUri != null) {
            try {
                val fileContent = readFileContent(fileUri)
                if (fileContent.isNotBlank()) {
                    prompt += "\n\nADDITIONAL CONTEXT FROM UPLOADED FILE:\n$fileContent"
                }
            } catch (_: Exception) {
                // Ignore file reading errors
            }
        }

        return prompt
    }

    private fun readFileContent(uri: Uri): String {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText().take(2000) // Limit to first 2000 chars
                }
            } ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun createCourseFromAIResponse(
        title: String,
        instructor: String,
        duration: String,
        price: String,
        description: String,
        aiGeneratedContent: String
    ) {
        // Parse AI response to extract course details
        val parsedData = parseSimpleAIResponse(aiGeneratedContent)

        val courseId = "ai_${System.currentTimeMillis()}"

        val course = Course(
            id = courseId,
            title = parsedData.title.ifEmpty { title },
            code = "AI-${System.currentTimeMillis().toString().takeLast(4)}",
            description = parsedData.description.ifEmpty { description },
            instructor = instructor,
            duration = duration,
            progress = 0,
            lessons = parsedData.modulesCount,
            students = 1,
            imageUrl = "",
            category = selectedCategory,
            rating = 4.5f,
            isEnrolled = true,
            difficulty = selectedDifficulty,
            price = price,
            lastUpdated = "Just now",
            instructorId = "ai_instructor",
            thumbnailUrl = null,
            semester = "AI Generated - ${getCurrentSemester()}",
            credits = calculateCredits(duration),
            startDate = System.currentTimeMillis(),
            endDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
            isActive = true,
            enrollmentDate = System.currentTimeMillis(),
            totalAssets = parsedData.modulesCount * 3, // Estimate
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Save to database
        viewModel.createCourseAsync(course)

        // Parse and save course content
        val detailedParsedData = DetailedParsedCourseData(
            overview = extractOverview(aiGeneratedContent),
            learningObjectives = extractLearningObjectives(aiGeneratedContent),
            modules = parseModules(aiGeneratedContent),
            detailedContent = extractDetailedContent(aiGeneratedContent),
            quizQuestions = extractQuizQuestions(aiGeneratedContent),
            mcqQuestions = extractMCQs(aiGeneratedContent),
            videoSuggestions = extractVideoSuggestions(aiGeneratedContent),
            finalProject = extractFinalProject(aiGeneratedContent),
            resources = extractResources(aiGeneratedContent)
        )

        saveCourseContent(courseId, aiGeneratedContent, detailedParsedData)

        // Show success dialog with AI content
        showImprovedSuccessDialog(course)
    }

    private fun parseSimpleAIResponse(content: String): SimpleParsedCourseData {
        // Extract title if present
        val titleRegex = """(?:COURSE TITLE|Title)[:\s]+(.+?)(?:\n|$)""".toRegex(RegexOption.IGNORE_CASE)
        val title = titleRegex.find(content)?.groupValues?.get(1)?.trim() ?: ""

        // Extract a SHORT description (first 2-3 sentences of overview)
        val descRegex = """(?:OVERVIEW|INTRODUCTION|DESCRIPTION)[:\s]+((?:[^.!?]+[.!?]){1,3})""".toRegex(
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val description = descRegex.find(content)?.groupValues?.get(1)?.trim()?.take(300) ?: ""

        // Count modules
        val moduleRegex = """(?:MODULE|Module)\s*\d+""".toRegex(RegexOption.IGNORE_CASE)
        val modulesCount = moduleRegex.findAll(content).toList().size.coerceAtLeast(4).coerceAtMost(12)

        return SimpleParsedCourseData(title, description, modulesCount)
    }

    // ========== NEW METHODS ADDED BELOW ==========

    private fun saveCourseContent(courseId: String, fullContent: String, parsedData: DetailedParsedCourseData) {
        // Convert lists to JSON strings
        val modulesJson = JSONArray().apply {
            parsedData.modules.forEach { module ->
                put(JSONObject().apply {
                    put("moduleNumber", module.moduleNumber)
                    put("title", module.title)
                    put("topics", JSONArray(module.topics))
                    put("description", module.description)
                    put("isCompleted", module.isCompleted)
                })
            }
        }.toString()

        val quizJson = JSONArray().apply {
            parsedData.quizQuestions.forEach { quiz ->
                put(JSONObject().apply {
                    put("id", quiz.id)
                    put("question", quiz.question)
                    put("answer", quiz.answer)
                    put("points", quiz.points)
                })
            }
        }.toString()

        val mcqJson = JSONArray().apply {
            parsedData.mcqQuestions.forEach { mcq ->
                put(JSONObject().apply {
                    put("id", mcq.id)
                    put("question", mcq.question)
                    put("options", JSONArray(mcq.options))
                    put("correctAnswer", mcq.correctAnswer)
                    put("explanation", mcq.explanation)
                })
            }
        }.toString()

        val videoJson = JSONArray().apply {
            parsedData.videoSuggestions.forEach { video ->
                put(JSONObject().apply {
                    put("title", video.title)
                    put("description", video.description)
                    put("searchQuery", video.searchQuery)
                })
            }
        }.toString()

        val courseContent = CourseContentEntity(
            courseId = courseId,
            fullContent = fullContent,
            overview = parsedData.overview,
            learningObjectives = parsedData.learningObjectives,
            modulesJson = modulesJson,
            detailedContent = parsedData.detailedContent,
            quizQuestions = quizJson,
            mcqQuestions = mcqJson,
            videoSuggestions = videoJson,
            finalProject = parsedData.finalProject,
            additionalResources = parsedData.resources
        )

        // Save to database
        viewModel.saveCourseContent(courseContent)
    }

    private fun extractOverview(content: String): String {
        val overviewRegex = """OVERVIEW|INTRODUCTION[:\s]*([\s\S]+?)(?=LEARNING OBJECTIVES|MODULES|COURSE CONTENT|$)""".toRegex(
            RegexOption.IGNORE_CASE
        )
        return overviewRegex.find(content)?.groupValues?.get(1)?.trim() ?: content.take(500)
    }

    private fun extractLearningObjectives(content: String): String {
        val objectivesRegex = """LEARNING OBJECTIVES|OUTCOMES|GOALS[:\s]*([\s\S]+?)(?=MODULES|COURSE CONTENT|WEEK|$)""".toRegex(
            RegexOption.IGNORE_CASE
        )
        return objectivesRegex.find(content)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun parseModules(content: String): List<CourseModule> {
        val modules = mutableListOf<CourseModule>()
        val moduleRegex = """(?:MODULE|Module)\s*(\d+)[:\s]*([\s\S]+?)(?=(?:MODULE|Module)\s*\d+|QUIZ|ASSESSMENT|$|FINAL PROJECT)""".toRegex(
            RegexOption.IGNORE_CASE
        )

        moduleRegex.findAll(content).forEachIndexed { index, matchResult ->
            val moduleNumber = matchResult.groupValues[1].toIntOrNull() ?: (index + 1)
            val moduleContent = matchResult.groupValues[2].trim()

            // Extract title (first line after module number)
            val lines = moduleContent.lines()
            val title = lines.firstOrNull()?.trim()?.removePrefix("Module $moduleNumber:")?.trim() ?: "Module $moduleNumber"

            // Extract topics (look for bullet points or numbered lists)
            val topics = moduleContent.lines()
                .filter { it.trim().matches(Regex("""^[\-\*•\d\.].+""")) }
                .map { it.trim().substring(1).trim() }
                .takeIf { it.isNotEmpty() } ?: listOf("Topic 1", "Topic 2", "Topic 3")

            // Use content as description
            val description = moduleContent.take(200)

            modules.add(CourseModule(
                moduleNumber = moduleNumber,
                title = title,
                topics = topics,
                description = description
            ))
        }

        // If no modules found, create default modules
        if (modules.isEmpty()) {
            for (i in 1..4) {
                modules.add(CourseModule(
                    moduleNumber = i,
                    title = "Module $i: Introduction",
                    topics = listOf("Basic Concepts", "Fundamentals", "Practice Exercises"),
                    description = "Introduction to the course concepts"
                ))
            }
        }

        return modules
    }

    private fun extractDetailedContent(content: String): String {
        // Extract everything after overview and objectives as detailed content
        val startRegex = """MODULES|COURSE CONTENT|DETAILED CONTENT[:\s]*""".toRegex(RegexOption.IGNORE_CASE)
        val startMatch = startRegex.find(content)

        return if (startMatch != null) {
            content.substring(startMatch.range.first).trim()
        } else {
            content // Fallback to full content
        }
    }

    private fun extractQuizQuestions(content: String): List<QuizQuestion> {
        val quizQuestions = mutableListOf<QuizQuestion>()
        val quizRegex = """(?:QUIZ|QUESTION)\s*\d*[:\s]*([^\n?]+\?)[\s\S]*?(?:ANSWER|SOLUTION)[:\s]*([^\n]+)""".toRegex(
            RegexOption.IGNORE_CASE
        )

        quizRegex.findAll(content).forEachIndexed { index, matchResult ->
            val question = matchResult.groupValues[1].trim()
            val answer = matchResult.groupValues[2].trim()
            quizQuestions.add(QuizQuestion(
                id = "quiz_$index",
                question = question,
                answer = answer
            ))
        }

        return quizQuestions
    }

    private fun extractMCQs(content: String): List<MCQQuestion> {
        val mcqs = mutableListOf<MCQQuestion>()
        val mcqRegex = """MCQ\s*\d*[:\s]*([^\n?]+\?)[\s\S]*?Options?[:\s]*([\s\S]+?)Correct Answer[:\s]*([^\n]+)""".toRegex(
            RegexOption.IGNORE_CASE
        )

        mcqRegex.findAll(content).forEachIndexed { index, matchResult ->
            val question = matchResult.groupValues[1].trim()
            val optionsText = matchResult.groupValues[2].trim()
            val correctAnswerText = matchResult.groupValues[3].trim()

            // Parse options (A, B, C, D format)
            val options = mutableListOf<String>()
            val optionRegex = """[A-D][\.\)]\s*(.+?)(?=\n[A-D][\.\)]|\nCorrect|\nExplanation|\n$)""".toRegex()
            optionRegex.findAll(optionsText).forEach { optionMatch ->
                options.add(optionMatch.groupValues[1].trim())
            }

            // If regex didn't work, try splitting by lines
            if (options.isEmpty()) {
                options.addAll(optionsText.lines()
                    .filter { it.trim().matches(Regex("""^[A-D][\.\)].+""")) }
                    .map { it.trim().substring(2).trim() })
            }

            // Fill with defaults if still empty
            if (options.isEmpty()) {
                options.addAll(listOf("Option A", "Option B", "Option C", "Option D"))
            }

            // Determine correct answer index
            val correctAnswerIndex = when {
                correctAnswerText.contains("A", ignoreCase = true) -> 0
                correctAnswerText.contains("B", ignoreCase = true) -> 1
                correctAnswerText.contains("C", ignoreCase = true) -> 2
                correctAnswerText.contains("D", ignoreCase = true) -> 3
                else -> 0
            }

            if (options.size >= 2) {
                mcqs.add(MCQQuestion(
                    id = "mcq_$index",
                    question = question,
                    options = options,
                    correctAnswer = correctAnswerIndex,
                    explanation = "Correct answer is ${correctAnswerText}"
                ))
            }
        }

        return mcqs
    }

    private fun extractVideoSuggestions(content: String): List<VideoSuggestion> {
        val videos = mutableListOf<VideoSuggestion>()

        // Try different patterns to find videos
        val videoPatterns = listOf(
            """Video\s*\d*:?\s*"([^"]+)"\s*-\s*([^\n]+)""".toRegex(RegexOption.IGNORE_CASE),
            """RECOMMENDED VIDEOS[\s\S]*?Video.*?:?([^\n"]+)"?([^\n]+)""".toRegex(RegexOption.IGNORE_CASE),
            """"([^"]+)"\s*-\s*([^\n]+)""".toRegex(RegexOption.IGNORE_CASE)
        )

        for (pattern in videoPatterns) {
            pattern.findAll(content).forEach { match ->
                val title = match.groupValues[1].trim()
                val description = match.groupValues[2].trim()

                if (title.isNotBlank() && !title.contains("RECOMMENDED", ignoreCase = true)) {
                    val searchQuery = if (description.isNotBlank()) "$title $description" else title
                    videos.add(VideoSuggestion(
                        title = title,
                        description = description,
                        searchQuery = searchQuery
                    ))
                }
            }
        }

        // If no videos found with patterns, check if section exists
        if (videos.isEmpty() && (content.contains("RECOMMENDED VIDEOS", ignoreCase = true) ||
                    content.contains("VIDEO RESOURCES", ignoreCase = true))) {

            videos.addAll(listOf(
                VideoSuggestion(
                    title = "Introduction Tutorial",
                    description = "Beginner-friendly overview of the course topic",
                    searchQuery = "${title} tutorial beginner"
                ),
                VideoSuggestion(
                    title = "Advanced Concepts",
                    description = "Deep dive into advanced topics",
                    searchQuery = "${title} advanced concepts"
                )
            ))
        }

        return videos
    }

    private fun extractFinalProject(content: String): String {
        val projectRegex = """FINAL PROJECT|CAPSTONE PROJECT|PROJECT[:\s]*([\s\S]+?)(?=ADDITIONAL RESOURCES|CONCLUSION|$)""".toRegex(
            RegexOption.IGNORE_CASE
        )
        return projectRegex.find(content)?.groupValues?.get(1)?.trim() ?: "Create a comprehensive project applying all course concepts."
    }

    private fun extractResources(content: String): String {
        val resourcesRegex = """ADDITIONAL RESOURCES|FURTHER READING|REFERENCES[:\s]*([\s\S]+?)(?=$)""".toRegex(
            RegexOption.IGNORE_CASE
        )
        return resourcesRegex.find(content)?.groupValues?.get(1)?.trim() ?: "No additional resources provided."
    }

    private fun showImprovedSuccessDialog(course: Course) {
        AlertDialog.Builder(this)
            .setTitle("🎉 Course Created Successfully!")
            .setMessage("""
                ✅ Course: ${course.title}
                
                📚 Code: ${course.code}
                👨‍🏫 Instructor: ${course.instructor}
                ⏱️ Duration: ${course.duration}
                📊 Category: ${course.category}
                🎯 Difficulty: ${course.difficulty}
                
                All course content has been generated and saved.
                """.trimIndent())
            .setPositiveButton("View Course") { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(this, CourseDetailActivity::class.java).apply {
                    putExtra("course_id", course.id)
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Create Another") { dialog, _ ->
                dialog.dismiss()
                resetForm()
            }
            .setNeutralButton("Go to Courses") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, CoursesActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showSuccessDialog(course: Course, aiContent: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_course_preview, null)

        dialogView.findViewById<TextView>(R.id.tvPreviewTitle).text = course.title
        dialogView.findViewById<TextView>(R.id.tvPreviewInstructor).text = "By ${course.instructor}"
        dialogView.findViewById<TextView>(R.id.tvVideosContent).text = aiContent

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("View Course") { _, _ ->
                val intent = Intent(this, CourseDetailActivity::class.java).apply {
                    putExtra("course_id", course.id)
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Create Another") { _, _ ->
                resetForm()
            }
            .setNeutralButton("Go to Courses") { _, _ ->
                startActivity(Intent(this, CoursesActivity::class.java))
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Generation Failed")
            .setMessage(message)
            .setPositiveButton("Retry") { _, _ ->
                // User can try again
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetForm() {
        findViewById<EditText>(R.id.etCourseTitle).text?.clear()
        findViewById<EditText>(R.id.etInstructor).text?.clear()
        findViewById<EditText>(R.id.etDuration).setText(getString(R.string.default_duration))
        findViewById<EditText>(R.id.etPrice).setText(getString(R.string.free))
        findViewById<EditText>(R.id.etPrompt).text?.clear()
        findViewById<Spinner>(R.id.spinnerDifficulty).setSelection(0)
        findViewById<Spinner>(R.id.spinnerCategory).setSelection(categories.size - 1)
        findViewById<MaterialCheckBox>(R.id.cbIncludeQuiz)?.isChecked = false
        findViewById<MaterialCheckBox>(R.id.cbIncludeMCQs)?.isChecked = false
        findViewById<MaterialCheckBox>(R.id.cbIncludeVideo)?.isChecked = false
        removeSelectedFile()
    }

    private fun checkStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openFilePicker()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "text/plain",
                "image/*"
            ))
        }

        try {
            filePickerLauncher.launch(Intent.createChooser(intent, "Select a file"))
        } catch (e: Exception) {
            Toast.makeText(this, "No file picker found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSelectedFile(uri: Uri) {
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    selectedFileName = cursor.getString(nameIndex)
                    selectedFileSize = cursor.getLong(sizeIndex)
                    selectedFileUri = uri

                    updateFileUI()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFileUI() {
        val selectedFileCard = findViewById<View>(R.id.selectedFileCard)
        val tvFileName = findViewById<TextView>(R.id.tvFileName)
        val tvFileSize = findViewById<TextView>(R.id.tvFileSize)
        val ivFileIcon = findViewById<ImageView>(R.id.ivFileIcon)

        selectedFileCard.visibility = View.VISIBLE
        tvFileName.text = selectedFileName

        // Set icon based on file type
        selectedFileName?.let { fileName ->
            when {
                fileName.endsWith(".pdf", true) -> ivFileIcon.setImageResource(R.drawable.ic_pdf)
                fileName.endsWith(".jpg", true) ||
                        fileName.endsWith(".png", true) -> ivFileIcon.setImageResource(R.drawable.ic_image)
                fileName.endsWith(".txt", true) -> ivFileIcon.setImageResource(R.drawable.ic_text)
                else -> ivFileIcon.setImageResource(R.drawable.ic_document)
            }
        }

        tvFileSize.text = Formatter.formatShortFileSize(this, selectedFileSize)
        Toast.makeText(this, "✅ File attached: $selectedFileName", Toast.LENGTH_SHORT).show()
    }

    private fun removeSelectedFile() {
        selectedFileUri = null
        selectedFileName = null
        selectedFileSize = 0
        findViewById<View>(R.id.selectedFileCard).visibility = View.GONE
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnGenerate.isEnabled = !show
    }

    private fun getCurrentSemester(): String {
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH)
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        return when (month) {
            in 0..5 -> "Spring $year"
            in 6..8 -> "Summer $year"
            else -> "Fall $year"
        }
    }

    private fun calculateCredits(duration: String): Int {
        val weeks = duration.filter { it.isDigit() }.toIntOrNull() ?: 8
        return when {
            weeks <= 4 -> 2
            weeks <= 8 -> 3
            weeks <= 12 -> 4
            else -> 5
        }
    }

    // ========== DATA CLASSES ==========

    private data class SimpleParsedCourseData(
        val title: String,
        val description: String,
        val modulesCount: Int
    )

    private data class DetailedParsedCourseData(
        val overview: String,
        val learningObjectives: String,
        val modules: List<CourseModule>,
        val detailedContent: String,
        val quizQuestions: List<QuizQuestion>,
        val mcqQuestions: List<MCQQuestion>,
        val videoSuggestions: List<VideoSuggestion>,
        val finalProject: String,
        val resources: String
    )
}
package com.example.proj.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proj.R
import com.example.proj.adapters.SimpleCoursesAdapter
import com.example.proj.models.Course
import com.example.proj.viewmodel.CourseViewModel
import com.example.proj.viewmodel.CourseViewModelFactory
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CoursesActivity : AppCompatActivity() {

    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var loadingLayout: LinearLayout
    private lateinit var emptyLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var errorMessage: TextView
    private lateinit var searchEditText: EditText

    private lateinit var adapter: SimpleCoursesAdapter
    private val viewModel: CourseViewModel by viewModels {
        CourseViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        // Initialize views using findViewById
        coursesRecyclerView = findViewById(R.id.coursesRecyclerView)
        loadingLayout = findViewById(R.id.loadingLayout)
        emptyLayout = findViewById(R.id.emptyLayout)
        errorLayout = findViewById(R.id.errorLayout)
        errorMessage = findViewById(R.id.errorMessage)
        searchEditText = findViewById(R.id.searchEditText)

        val backButton: ImageView = findViewById(R.id.backButton)
        val refreshButton: ImageView = findViewById(R.id.refreshButton)
        val searchButton: MaterialButton = findViewById(R.id.searchButton)
        val createCourseButton: MaterialButton = findViewById(R.id.createCourseButton)
        val retryButton: MaterialButton = findViewById(R.id.retryButton)
        val dashboardTab: LinearLayout = findViewById(R.id.dashboardTab)
        val coursesTab: LinearLayout = findViewById(R.id.coursesTab)
        val assetsTab: LinearLayout = findViewById(R.id.assetsTab)
        val profileTab: LinearLayout = findViewById(R.id.profileTab)

        setupRecyclerView()
        setupListeners(
            backButton, refreshButton, searchButton, createCourseButton,
            retryButton, dashboardTab, coursesTab, assetsTab, profileTab
        )
        setupObservers()

        // Load courses initially
        showLoading(true)
        viewModel.refreshCourses()
    }

    private fun setupRecyclerView() {
        adapter = SimpleCoursesAdapter(
            courses = emptyList(),
            onItemClick = { course ->
                openCourseDetail(course)
            },
            onContinueClick = { course ->
                // Handle continue/enroll button click
                if (course.isEnrolled) {
                    openCourseDetail(course)
                } else {
                    enrollInCourse(course)
                }
            },
            onShareClick = { course ->
                shareCourse(course)
            }
        )

        coursesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CoursesActivity)
            adapter = this@CoursesActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners(
        backButton: ImageView,
        refreshButton: ImageView,
        searchButton: MaterialButton,
        createCourseButton: MaterialButton,
        retryButton: MaterialButton,
        dashboardTab: LinearLayout,
        coursesTab: LinearLayout,
        assetsTab: LinearLayout,
        profileTab: LinearLayout
    ) {
        // Back button
        backButton.setOnClickListener {
            finish()
        }

        // Refresh button
        refreshButton.setOnClickListener {
            showLoading(true)
            viewModel.refreshCourses()
            Toast.makeText(this, "Refreshing courses...", Toast.LENGTH_SHORT).show()
        }

        // Search button
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                // Use Flow to search courses
                lifecycleScope.launch {
                    showLoading(true)
                    viewModel.searchCoursesFlow(query).collectLatest { courses ->
                        showLoading(false)
                        if (courses.isEmpty()) {
                            showEmptyState(true)
                            Toast.makeText(
                                this@CoursesActivity,
                                "No courses found for '$query'",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showEmptyState(false)
                            adapter.updateCourses(courses)
                            Toast.makeText(
                                this@CoursesActivity,
                                "Found ${courses.size} courses",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                // Show all courses if search is empty
                showLoading(true)
                viewModel.refreshCourses()
            }
        }

        // Create course button
        createCourseButton.setOnClickListener {
            val intent = Intent(this, QuickCreateActivity::class.java)
            startActivity(intent)
        }

        // Retry button
        retryButton.setOnClickListener {
            showLoading(true)
            viewModel.refreshCourses()
        }

        // Bottom navigation
        dashboardTab.setOnClickListener {
            finish()
        }

        coursesTab.setOnClickListener {
            // Already on courses page
            Toast.makeText(this, "Already on Courses", Toast.LENGTH_SHORT).show()
        }

        assetsTab.setOnClickListener {
            startActivity(Intent(this, AssetsActivity::class.java))
            finish()
        }

        profileTab.setOnClickListener {
            Toast.makeText(this, "Profile page coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        // Observe courses using LiveData
        viewModel.courses.observe(this, Observer { courses ->
            showLoading(false)

            if (courses == null || courses.isEmpty()) {
                showEmptyState(true)
                Toast.makeText(this, "No courses available. Create one!", Toast.LENGTH_SHORT).show()
            } else {
                showEmptyState(false)
                showErrorState(false)
                adapter.updateCourses(courses)
                Toast.makeText(this, "Loaded ${courses.size} courses", Toast.LENGTH_SHORT).show()
            }
        })

        // Observe loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading == true) {
                showLoading(true)
            } else {
                showLoading(false)
            }
        })

        // Observe error messages
        viewModel.errorMessage.observe(this, Observer { error ->
            if (error != null && error.isNotEmpty()) {
                showLoading(false)
                showErrorState(true, error)
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        })
    }

    private fun openCourseDetail(course: Course) {
        val intent = Intent(this, CourseDetailActivity::class.java).apply {
            putExtra("course_id", course.id)
        }
        startActivity(intent)
    }

    private fun enrollInCourse(course: Course) {
        Toast.makeText(this, "Enrolling in ${course.title}...", Toast.LENGTH_SHORT).show()
        viewModel.enrollInCourse(course.id)

        // Show success message
        Toast.makeText(this, "✅ Enrolled in ${course.title}!", Toast.LENGTH_SHORT).show()

        // Refresh courses to update enrollment status
        viewModel.refreshCourses()
    }

    private fun shareCourse(course: Course) {
        val shareText = """
            📚 Check out this course!
            
            ${course.title}
            ${course.description}
            
            👨‍🏫 Instructor: ${course.instructor}
            ⏱️ Duration: ${course.duration}
            🎓 ${course.lessons} Lessons
            ⭐ Rating: ${course.rating}/5.0
            
            Join ${course.students} other students learning now!
        """.trimIndent()

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Check out: ${course.title}")
        }
        startActivity(Intent.createChooser(shareIntent, "Share Course"))
    }

    private fun showLoading(show: Boolean) {
        loadingLayout.visibility = if (show) View.VISIBLE else View.GONE
        coursesRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
        emptyLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showEmptyState(show: Boolean) {
        emptyLayout.visibility = if (show) View.VISIBLE else View.GONE
        coursesRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
        loadingLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
    }

    private fun showErrorState(show: Boolean, message: String = "Failed to load courses") {
        errorLayout.visibility = if (show) View.VISIBLE else View.GONE
        errorMessage.text = message
        coursesRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
        loadingLayout.visibility = View.GONE
        emptyLayout.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        // Refresh courses when returning to this activity
        viewModel.refreshCourses()
    }
}
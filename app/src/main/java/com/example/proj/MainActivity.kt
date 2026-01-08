package com.example.proj.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.proj.R
import com.example.proj.viewmodel.CourseViewModel
import com.example.proj.viewmodel.CourseViewModelFactory
import com.example.proj.models.Course
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView

    // Create ViewModel with factory
    private val viewModel: CourseViewModel by viewModels {
        CourseViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "MainActivity started")

        drawerLayout = findViewById(R.id.drawerLayout)
        btnMenu = findViewById(R.id.btnMenu)

        setupSidebar()
        setupCards()
        setupSidebarNavigation()
        setupWelcomeText()
        setupObservers()
        setupLogoutButton()

        // Add sample data if database is empty
        addSampleDataIfNeeded()
    }

    private fun setupSidebar() {
        // Open sidebar when menu button is clicked
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
                drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            } else {
                drawerLayout.openDrawer(findViewById(R.id.sidebar))
            }
        }
    }

    private fun setupCards() {
        findViewById<CardView>(R.id.coursesCard).setOnClickListener {
            openCoursesActivity()
        }

        findViewById<CardView>(R.id.assetsCard).setOnClickListener {
            openAssetsActivity()
        }

        findViewById<CardView>(R.id.createCard).setOnClickListener {
            openQuickCreateActivity()
        }

        findViewById<TextView>(R.id.viewAllCourses).setOnClickListener {
            openCoursesActivity()
        }

        findViewById<TextView>(R.id.seeMoreButton).setOnClickListener {
            openCoursesActivity()
        }
    }

    private fun setupSidebarNavigation() {
        findViewById<CardView>(R.id.btnDashboard).setOnClickListener {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            Toast.makeText(this, "Already on Dashboard", Toast.LENGTH_SHORT).show()
        }

        findViewById<CardView>(R.id.btnCourses).setOnClickListener {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            openCoursesActivity()
        }

        findViewById<CardView>(R.id.btnAssets).setOnClickListener {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            openAssetsActivity()
        }

        findViewById<CardView>(R.id.btnQuickCreate).setOnClickListener {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar))
            openQuickCreateActivity()
        }
    }

    private fun setupLogoutButton() {
        findViewById<CardView>(R.id.btnLogout).setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes") { dialog, _ ->
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        // Clear login session using the static method from LoginActivity
        LoginActivity.clearLoginSession(this)

        // Navigate back to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun setupWelcomeText() {
        findViewById<TextView>(R.id.tvUserName).text = "Welcome, Farid!"
    }

    private fun setupObservers() {
        viewModel.courses.observe(this, Observer { courses ->
            if (courses.isNullOrEmpty()) {
                Log.d("MainActivity", "No courses found, will add sample data")
            } else {
                Log.d("MainActivity", "Found ${courses.size} courses")
                updateRecentCourses(courses)
            }
        })
    }

    private fun addSampleDataIfNeeded() {
        lifecycleScope.launch {
            try {
                val courseCount = viewModel.getCourseCount()
                if (courseCount == 0) {
                    Log.d("MainActivity", "Database is empty, adding sample courses")
                    addSampleCourses()
                } else {
                    Log.d("MainActivity", "Database has $courseCount courses")
                    viewModel.refreshCourses()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error checking course count: ${e.message}")
            }
        }
    }

    private fun addSampleCourses() {
        lifecycleScope.launch {
            try {
                val sampleCourses = listOf(
                    Course(
                        id = "course_1",
                        title = "Android Development",
                        code = "AND-101",
                        description = "Mobile app development with Kotlin",
                        instructor = "Dr. Sarah Johnson",
                        duration = "8 weeks",
                        progress = 60,
                        lessons = 24,
                        students = 150,
                        imageUrl = "",
                        category = "Programming",
                        rating = 4.8f,
                        isEnrolled = true,
                        difficulty = "Intermediate",
                        price = "Free",
                        lastUpdated = "2 days ago",
                        instructorId = "inst_1",
                        thumbnailUrl = null,
                        semester = "Spring 2024",
                        credits = 4,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
                        isActive = true,
                        enrollmentDate = System.currentTimeMillis(),
                        totalAssets = 24,
                        isFavorite = true,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ),
                    Course(
                        id = "course_2",
                        title = "Web Development",
                        code = "WEB-201",
                        description = "Full-stack web development",
                        instructor = "Prof. Michael Chen",
                        duration = "10 weeks",
                        progress = 40,
                        lessons = 30,
                        students = 200,
                        imageUrl = "",
                        category = "Web",
                        rating = 4.7f,
                        isEnrolled = true,
                        difficulty = "Intermediate",
                        price = "Free",
                        lastUpdated = "1 week ago",
                        instructorId = "inst_2",
                        thumbnailUrl = null,
                        semester = "Spring 2024",
                        credits = 4,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
                        isActive = true,
                        enrollmentDate = System.currentTimeMillis(),
                        totalAssets = 30,
                        isFavorite = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ),
                    Course(
                        id = "course_3",
                        title = "Data Science",
                        code = "DS-301",
                        description = "Data analysis and machine learning",
                        instructor = "Dr. Emily Rodriguez",
                        duration = "12 weeks",
                        progress = 75,
                        lessons = 36,
                        students = 180,
                        imageUrl = "",
                        category = "Data Science",
                        rating = 4.9f,
                        isEnrolled = true,
                        difficulty = "Advanced",
                        price = "Free",
                        lastUpdated = "3 days ago",
                        instructorId = "inst_3",
                        thumbnailUrl = null,
                        semester = "Spring 2024",
                        credits = 5,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
                        isActive = true,
                        enrollmentDate = System.currentTimeMillis(),
                        totalAssets = 36,
                        isFavorite = true,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ),
                    Course(
                        id = "course_4",
                        title = "UI/UX Design",
                        code = "DES-101",
                        description = "User interface and experience design",
                        instructor = "Alex Martinez",
                        duration = "6 weeks",
                        progress = 0,
                        lessons = 18,
                        students = 120,
                        imageUrl = "",
                        category = "Design",
                        rating = 4.6f,
                        isEnrolled = false,
                        difficulty = "Beginner",
                        price = "Free",
                        lastUpdated = "1 week ago",
                        instructorId = "inst_4",
                        thumbnailUrl = null,
                        semester = "Spring 2024",
                        credits = 3,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
                        isActive = true,
                        enrollmentDate = null,
                        totalAssets = 18,
                        isFavorite = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    ),
                    Course(
                        id = "course_5",
                        title = "Python Programming",
                        code = "PY-101",
                        description = "Learn Python from scratch",
                        instructor = "James Wilson",
                        duration = "8 weeks",
                        progress = 0,
                        lessons = 28,
                        students = 250,
                        imageUrl = "",
                        category = "Programming",
                        rating = 4.8f,
                        isEnrolled = false,
                        difficulty = "Beginner",
                        price = "Free",
                        lastUpdated = "2 weeks ago",
                        instructorId = "inst_5",
                        thumbnailUrl = null,
                        semester = "Spring 2024",
                        credits = 4,
                        startDate = System.currentTimeMillis(),
                        endDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000),
                        isActive = true,
                        enrollmentDate = null,
                        totalAssets = 28,
                        isFavorite = false,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                )

                // Insert all sample courses
                viewModel.insertAllCourses(sampleCourses)

                Log.d("MainActivity", "Sample courses added successfully")
                Toast.makeText(this@MainActivity, "✨ Sample courses loaded!", Toast.LENGTH_SHORT).show()

                // Refresh to display
                viewModel.refreshCourses()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error adding sample courses: ${e.message}", e)
                Toast.makeText(this@MainActivity, "Error loading sample data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRecentCourses(courses: List<Course>) {
        // Show the 3 most recent enrolled courses
        val recentCourses = courses.filter { it.isEnrolled }.take(3)

        try {
            if (recentCourses.isNotEmpty()) {
                // Course 1
                if (recentCourses.size > 0) {
                    findViewById<TextView>(R.id.tvCourseTitle1).text = recentCourses[0].title
                    // Only show first 50 chars of description
                    val shortDesc = recentCourses[0].description.take(50).trim()
                    findViewById<TextView>(R.id.tvCourseDesc1).text =
                        if (shortDesc.length < recentCourses[0].description.length) "$shortDesc..." else shortDesc
                    findViewById<TextView>(R.id.tvProgress1).text = "${recentCourses[0].progress}%"
                    findViewById<CardView>(R.id.courseItem1).setOnClickListener {
                        openCourseDetail(recentCourses[0])
                    }
                }

                // Course 2
                if (recentCourses.size > 1) {
                    findViewById<TextView>(R.id.tvCourseTitle2).text = recentCourses[1].title
                    val shortDesc = recentCourses[1].description.take(50).trim()
                    findViewById<TextView>(R.id.tvCourseDesc2).text =
                        if (shortDesc.length < recentCourses[1].description.length) "$shortDesc..." else shortDesc
                    findViewById<TextView>(R.id.tvProgress2).text = "${recentCourses[1].progress}%"
                    findViewById<CardView>(R.id.courseItem2).setOnClickListener {
                        openCourseDetail(recentCourses[1])
                    }
                }

                // Course 3
                if (recentCourses.size > 2) {
                    findViewById<TextView>(R.id.tvCourseTitle3).text = recentCourses[2].title
                    val shortDesc = recentCourses[2].description.take(50).trim()
                    findViewById<TextView>(R.id.tvCourseDesc3).text =
                        if (shortDesc.length < recentCourses[2].description.length) "$shortDesc..." else shortDesc
                    findViewById<TextView>(R.id.tvProgress3).text = "${recentCourses[2].progress}%"
                    findViewById<CardView>(R.id.courseItem3).setOnClickListener {
                        openCourseDetail(recentCourses[2])
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error updating recent courses: ${e.message}")
        }
    }

    private fun openCourseDetail(course: Course) {
        try {
            val intent = Intent(this, CourseDetailActivity::class.java).apply {
                putExtra("course_id", course.id)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening course", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCoursesActivity() {
        try {
            startActivity(Intent(this, CoursesActivity::class.java))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening CoursesActivity: ${e.message}", e)
            Toast.makeText(this, "Courses feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAssetsActivity() {
        try {
            startActivity(Intent(this, AssetsActivity::class.java))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening AssetsActivity: ${e.message}", e)
            Toast.makeText(this, "Assets feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openQuickCreateActivity() {
        try {
            startActivity(Intent(this, QuickCreateActivity::class.java))
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening QuickCreateActivity: ${e.message}", e)
            Toast.makeText(this, "Create feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh courses when coming back to MainActivity
        viewModel.refreshCourses()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(findViewById(R.id.sidebar))) {
            drawerLayout.closeDrawer(findViewById(R.id.sidebar))
        } else {
            super.onBackPressed()
        }
    }
}
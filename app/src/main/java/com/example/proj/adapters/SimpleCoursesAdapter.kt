package com.example.proj.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proj.R
import com.example.proj.models.Course
import com.google.android.material.button.MaterialButton

class SimpleCoursesAdapter(
    private var courses: List<Course>,
    private val onItemClick: (Course) -> Unit,
    private val onContinueClick: (Course) -> Unit,
    private val onShareClick: (Course) -> Unit
) : RecyclerView.Adapter<SimpleCoursesAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize ONLY the views that exist in your layout
        private val courseImage: ImageView = itemView.findViewById(R.id.courseImage)
        private val courseTitle: TextView = itemView.findViewById(R.id.courseTitle)
        private val courseInstructor: TextView = itemView.findViewById(R.id.courseInstructor)
        private val courseCategory: TextView = itemView.findViewById(R.id.courseCategory)
        private val courseDescription: TextView = itemView.findViewById(R.id.courseDescription)
        private val duration: TextView = itemView.findViewById(R.id.duration)
        private val progressText: TextView = itemView.findViewById(R.id.progressText)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val lessonsCount: TextView = itemView.findViewById(R.id.lessonsCount)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val studentsCount: TextView = itemView.findViewById(R.id.studentsCount)
        private val continueButton: MaterialButton = itemView.findViewById(R.id.continueButton)
        private val shareButton: MaterialButton = itemView.findViewById(R.id.shareButton)
        private val moreOptions: ImageView = itemView.findViewById(R.id.moreOptions)

        fun bind(course: Course) {
            // Set course title
            courseTitle.text = course.title

            // Set instructor
            courseInstructor.text = course.instructor

            // Set category
            courseCategory.text = course.category

            // Set description
            courseDescription.text = course.description

            // Set duration
            duration.text = course.duration

            // Set progress (both text and progress bar)
            progressText.text = "${course.progress}%"
            tvProgress.text = "${course.progress}%"
            progressBar.progress = course.progress

            // Set lessons count
            lessonsCount.text = "${course.lessons} Lessons"

            // Set students count
            studentsCount.text = "${course.students} Students"

            // Set continue button text based on enrollment
            if (course.isEnrolled) {
                continueButton.text = "Continue"
                continueButton.isEnabled = true
            } else {
                continueButton.text = "Enroll"
                continueButton.isEnabled = true
            }

            // Set image (if you have image loading, add it here)
            // For now, using default icon
            courseImage.setImageResource(R.drawable.ic_courses)

            // Set click listeners
            continueButton.setOnClickListener {
                if (course.isEnrolled) {
                    onContinueClick(course)
                } else {
                    // If not enrolled, treat continue button as enroll button
                    onContinueClick(course)
                }
            }

            shareButton.setOnClickListener {
                onShareClick(course)
            }

            moreOptions.setOnClickListener {
                // Optional: Add more options menu
                onItemClick(course)
            }

            // Whole card click
            itemView.setOnClickListener {
                onItemClick(course)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount(): Int = courses.size

    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }
}
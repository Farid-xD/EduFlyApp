package com.example.proj.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.proj.R
import com.example.proj.viewmodel.UserViewModel
import com.example.proj.viewmodel.UserViewModelFactory

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: CardView
    private lateinit var backButton: ImageView
    private lateinit var togglePassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Initialize ViewModel
        viewModel = ViewModelProvider(
            this,
            UserViewModelFactory(application)
        )[UserViewModel::class.java]

        // Initialize views
        nameEditText = findViewById(R.id.etName)
        emailEditText = findViewById(R.id.etEmail)
        passwordEditText = findViewById(R.id.etPassword)
        confirmPasswordEditText = findViewById(R.id.etConfirmPassword)
        registerButton = findViewById(R.id.btnRegister)
        backButton = findViewById(R.id.btnBack)
        togglePassword = findViewById(R.id.togglePassword)
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword)

        // Observe ViewModel LiveData
        observeViewModel()

        // Setup click listeners
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call ViewModel register function
            viewModel.register(name, email, password)
        }

        // Setup back button
        backButton.setOnClickListener {
            finish()
        }

        // Setup toggle password visibility
        togglePassword.setOnClickListener {
            togglePasswordVisibility(passwordEditText, togglePassword)
        }

        toggleConfirmPassword.setOnClickListener {
            togglePasswordVisibility(confirmPasswordEditText, toggleConfirmPassword)
        }

        // Setup sign in text
        val signInText: TextView = findViewById(R.id.tvLogin)
        signInText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun observeViewModel() {
        // Observe registration result
        viewModel.registerResult.observe(this) { result ->
            result?.let {
                if (it.first) {
                    // Registration successful
                    Toast.makeText(this, it.second, Toast.LENGTH_SHORT).show()

                    // Navigate to login activity with pre-filled email
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("email", emailEditText.text.toString().trim())
                    startActivity(intent)
                    finish()
                } else {
                    // Registration failed
                    Toast.makeText(this, it.second, Toast.LENGTH_SHORT).show()
                }
                // Clear result to prevent showing again on rotation
                viewModel.clearResults()
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(this) { isLoading ->
            registerButton.isEnabled = !isLoading
            if (isLoading) {
                // Show loading indicator
                registerButton.alpha = 0.7f
            } else {
                registerButton.alpha = 1f
            }
        }

        // Observe current user (for auto-login after registration)
        viewModel.currentUser.observe(this) { user ->
            user?.let {
                // Auto-login successful after registration
                Toast.makeText(this, "Auto-login successful!", Toast.LENGTH_SHORT).show()

                // Navigate to main activity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView) {
        val isPasswordVisible = editText.inputType == 129 // 129 = TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        if (isPasswordVisible) {
            // Hide password
            editText.inputType = 129 or 128 // TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.ic_visibility_off)
        } else {
            // Show password
            editText.inputType = 129 // TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.ic_visibility)
        }

        // Move cursor to end
        editText.setSelection(editText.text.length)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear ViewModel results to prevent memory leaks
        viewModel.clearResults()
    }
}
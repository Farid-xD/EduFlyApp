package com.example.proj.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import com.example.proj.R
import com.example.proj.viewmodel.UserViewModel
import com.example.proj.viewmodel.UserViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: CardView
    private lateinit var signUpText: TextView
    private lateinit var togglePassword: ImageView
    private lateinit var forgotPasswordText: TextView

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "LoginPrefs"
    private val KEY_EMAIL = "email"
    private val KEY_PASSWORD = "password"
    private val KEY_IS_LOGGED_IN = "isLoggedIn"

    // Create ViewModel with factory
    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if user is already logged in
        if (isUserLoggedIn()) {
            Log.d("LoginActivity", "User already logged in, navigating to MainActivity")
            navigateToMainActivity()
            return
        }

        setContentView(R.layout.activity_login)

        Log.d("LoginActivity", "LoginActivity created")

        try {
            // Initialize views
            emailEditText = findViewById(R.id.etEmail)
            passwordEditText = findViewById(R.id.etPassword)
            loginButton = findViewById(R.id.btnLogin)
            signUpText = findViewById(R.id.tvSignUp)
            togglePassword = findViewById(R.id.togglePassword)
            forgotPasswordText = findViewById(R.id.tvForgotPassword)

            Log.d("LoginActivity", "Views initialized successfully")
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error initializing views: ${e.message}", e)
            Toast.makeText(this, "UI Error: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        // Load saved credentials (optional - for convenience)
        loadSavedCredentials()

        // Observe login results
        userViewModel.loginResult.observe(this, Observer { result ->
            result?.let { (success, message) ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

                if (success) {
                    Log.d("LoginActivity", "Login successful, saving credentials")

                    // Save login credentials
                    saveLoginCredentials(
                        emailEditText.text.toString().trim(),
                        passwordEditText.text.toString().trim()
                    )

                    // Navigate to MainActivity
                    navigateToMainActivity()
                }
            }
        })

        // Observe loading state
        userViewModel.isLoading.observe(this, Observer { isLoading ->
            loginButton.isEnabled = !isLoading
        })

        // Login click
        loginButton.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked")

            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            Log.d("LoginActivity", "Email: $email, Password length: ${password.length}")

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Use ViewModel to handle login with Room database
            userViewModel.login(email, password)
        }

        // Sign up click
        signUpText.setOnClickListener {
            Log.d("LoginActivity", "Sign up clicked")
            try {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("LoginActivity", "Error opening register: ${e.message}", e)
                Toast.makeText(this, "Cannot open register: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Forgot password click
        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Toggle password visibility
        togglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        Log.d("LoginActivity", "LoginActivity setup complete")
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun saveLoginCredentials(email: String, password: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PASSWORD, password)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
        Log.d("LoginActivity", "Login credentials saved")
    }

    private fun loadSavedCredentials() {
        val savedEmail = sharedPreferences.getString(KEY_EMAIL, "")
        val savedPassword = sharedPreferences.getString(KEY_PASSWORD, "")

        if (!savedEmail.isNullOrEmpty()) {
            emailEditText.setText(savedEmail)
        }
        if (!savedPassword.isNullOrEmpty()) {
            passwordEditText.setText(savedPassword)
        }
    }

    private fun navigateToMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java)

            // Pass user email to MainActivity
            val email = sharedPreferences.getString(KEY_EMAIL, "") ?: emailEditText.text.toString().trim()
            intent.putExtra("user_email", email)

            // Add flags to clear back stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            Log.d("LoginActivity", "Starting MainActivity")
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("LoginActivity", "Error starting MainActivity: ${e.message}", e)
            tryFallback()
        }
    }

    private fun tryFallback() {
        Log.d("LoginActivity", "Trying fallback activities...")

        // Try CoursesActivity
        try {
            Log.d("LoginActivity", "Trying CoursesActivity")
            val intent = Intent(this, CoursesActivity::class.java)
            startActivity(intent)
            finish()
            return
        } catch (e: Exception) {
            Log.e("LoginActivity", "CoursesActivity failed: ${e.message}")
        }

        // Try any available activity
        try {
            Log.d("LoginActivity", "Trying to create simple test activity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("LoginActivity", "All fallbacks failed: ${e.message}")
            Toast.makeText(this, "App cannot start. Reinstall or contact support.", Toast.LENGTH_LONG).show()
        }
    }

    private fun togglePasswordVisibility() {
        val isPasswordVisible = passwordEditText.inputType == 145

        if (isPasswordVisible) {
            passwordEditText.inputType = 129
            togglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            passwordEditText.inputType = 145
            togglePassword.setImageResource(R.drawable.ic_visibility)
        }

        passwordEditText.setSelection(passwordEditText.text.length)
    }

    private fun showForgotPasswordDialog() {
        val emailInput = EditText(this)
        emailInput.hint = "Enter your email"
        emailInput.setPadding(50, 40, 50, 40)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Forgot Password")
            .setMessage("Enter your email to reset password")
            .setView(emailInput)
            .setPositiveButton("Send Reset Link") { dialog, _ ->
                val email = emailInput.text.toString().trim()
                if (email.isNotEmpty()) {
                    Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onPause() {
        super.onPause()
        userViewModel.clearResults()
    }

    companion object {
        const val PREFS_NAME = "LoginPrefs"
        const val KEY_IS_LOGGED_IN = "isLoggedIn"

        // Static method to clear login session (call this from logout)
        fun clearLoginSession(context: Context) {
            val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            Log.d("LoginActivity", "Login session cleared")
        }
    }
}
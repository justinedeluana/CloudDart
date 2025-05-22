package com.example.clouddart

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    companion object {
        const val LOGIN_REQUEST_CODE = 100
    }

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var rememberMeCheckbox: MaterialCheckBox
    private lateinit var signInButton: MaterialButton
    private lateinit var googleSignInButton: MaterialCardView
    private lateinit var facebookSignInButton: MaterialCardView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_layout)

        // Initialize views
        initializeViews()
        // Set up click listeners
        setupClickListeners()
        setupNavigation()


    }

    private fun initializeViews() {
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)
        signInButton = findViewById(R.id.signInButton)
        googleSignInButton = findViewById(R.id.googleSignInButton)
        facebookSignInButton = findViewById(R.id.facebookSignInButton)
    }

    private fun setupClickListeners() {
        signInButton.setOnClickListener {
            performLogin()
        }

        googleSignInButton.setOnClickListener {
            // Implement Google Sign In
            Toast.makeText(this, "Google Sign In clicked", Toast.LENGTH_SHORT).show()
        }

        facebookSignInButton.setOnClickListener {
            // Implement Facebook Sign In
            Toast.makeText(this, "Facebook Sign In clicked", Toast.LENGTH_SHORT).show()
        }

        // Add back button functionality
        findViewById<ImageButton>(R.id.closeButton)?.setOnClickListener {
            finish()
        }
    }

    private fun performLogin() {
        val email = emailInput.text.toString()
        val password = passwordInput.text.toString()

        if (validateInput(email, password)) {
            // Here you would typically authenticate with your backend
            // For now, we'll just simulate a successful login
            handleSuccessfulLogin(email)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            emailInput.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            passwordInput.error = "Password is required"
            return false
        }
        return true
    }

    private fun handleSuccessfulLogin(email: String) {
        // Save login state if "Remember me" is checked
        if (rememberMeCheckbox.isChecked) {
            saveLoginState(email)
        }

        // Return to MainActivity with login result
        setResult(RESULT_OK)
        finish()
    }

    private fun saveLoginState(email: String) {
        val prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("email", email)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.homeButton)?.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }

            // Book button
        findViewById<ImageView>(R.id.bookButton)?.setOnClickListener {
            startActivity(Intent(this, SearchFlightsActivity::class.java))
        }

        // Info button
        findViewById<ImageView>(R.id.infoButton)?.setOnClickListener {
        // Handle info button click
        }

        // Profile button (current screen)
        findViewById<ImageView>(R.id.profileButton)?.setOnClickListener {
        // Already on profile/login screen
        }
    }
}



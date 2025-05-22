package com.example.clouddart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import android.content.Intent
import android.widget.Button
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Toast
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize

import android.view.Gravity
import com.example.clouddart.R
import com.example.clouddart.LoginActivity
import com.example.clouddart.SearchFlightsActivity
import com.example.clouddart.LanguagePreferenceFragment
import com.example.clouddart.ChatbotActivity
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult

class MainActivity : AppCompatActivity() {

    // UI components
    private lateinit var menuButton: ImageView
    private var popupMenu: PopupWindow? = null

    // Animation duration
    private val ANIMATION_DURATION = 300L

    companion object {
        const val LOGIN_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        // Initialize menuButton
        menuButton = findViewById(R.id.menuButton)
        menuButton.setOnClickListener {
            toggleMenu()
        }
        // Initialize Firebase
        Firebase.initialize(this)

        // Initialize UI components
        setupSearchFlightsButton()
        setupNavigation()
        setupChatbotButton()

    }

    private fun setupChatbotButton() {
        val chatbotButton = findViewById<FloatingActionButton>(R.id.chatbotButton) ?: return

        chatbotButton.apply {
            // Show dialog on click with error handling
            setOnClickListener {
                try {
                    showChatDialog()
                } catch (e: Exception) {
                    // Simple error handling - you can enhance this
                    Toast.makeText(context, "Unable to open chat", Toast.LENGTH_SHORT).show()
                }
            }

            // Add bounce animation on long press
            setOnLongClickListener { view ->
                try {
                    val bounceAnim = AnimationUtils.loadAnimation(context, R.anim.bounce_animation)
                    view.startAnimation(bounceAnim)
                } catch (e: Exception) {
                    // Animation failed, but don't crash
                }
                true
            }

            // Set elevation directly
            elevation = 8f
        }
    }

    private fun showChatDialog() {
        try {
            val intent = Intent(this, ChatbotActivity::class.java)
            startActivity(intent)
            // Use safer animation call
            try {
                overridePendingTransition(R.anim.slide_up, R.anim.slide_down)
            } catch (e: Exception) {
                // Fallback to default transition if animation fails
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open chat", Toast.LENGTH_SHORT).show()
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
            startActivityForResult(
                Intent(this, LoginActivity::class.java),
                LoginActivity.LOGIN_REQUEST_CODE
            )
        }
    }

    private fun setupSearchFlightsButton() {
        findViewById<Button>(R.id.btnSearchFlights).setOnClickListener {
            startActivity(Intent(this, SearchFlightsActivity::class.java))
        }
    }


    // Handle the result from LoginActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            // Handle successful login
            // Update UI or perform necessary actions
        }
    }


    private fun toggleMenu() {
        if (isMenuOpen()) {
            closeMenu()
        } else {
            openMenu()
        }
    }

    private fun isMenuOpen(): Boolean {
        return popupMenu?.isShowing == true
    }

    private fun openMenu() {
        val inflater = LayoutInflater.from(this)
        val menuView = inflater.inflate(R.layout.menu_layout, null)

        popupMenu = PopupWindow(
            menuView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            true
        )
        setupCloseButton(menuView)
        setupLanguageOption(menuView)
        animateMenuEntry(menuView)
        popupMenu?.showAtLocation(menuButton, 0, 0, 0)
        animateMenuButton()

        findViewById<FloatingActionButton>(R.id.chatbotButton)?.hide()
        popupMenu?.showAtLocation(menuButton, 0, 0, 0)
    }

    private fun setupCloseButton(menuView: View) {
        val closeButton: ImageButton = menuView.findViewById(R.id.btnClose)
        closeButton.setOnClickListener {
            closeMenu(menuView)
        }
    }

    private fun setupLanguageOption(menuView: View) {
        val languageOption: ConstraintLayout = menuView.findViewById(R.id.languageOption)
        languageOption.setOnClickListener {
            closeMenu(menuView)
            openLanguagePreferences()
        }
    }

    private fun openLanguagePreferences() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainLayout, LanguagePreferenceFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun animateMenuEntry(menuView: View) {
        val enterAnimation = AnimationUtils.loadAnimation(this, R.anim.popup_enter)
        menuView.startAnimation(enterAnimation)
    }

    private fun animateMenuButton() {
        val buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.menu_button_animation)
        menuButton.startAnimation(buttonAnimation)
    }

    private fun closeMenu(menuView: View? = null) {
        menuView?.let {
            val exitAnimation = AnimationUtils.loadAnimation(this, R.anim.popup_exit)
            it.startAnimation(exitAnimation)
            it.postDelayed({
                dismissMenu()
            }, ANIMATION_DURATION)
        } ?: run {
            dismissMenu()
        }
    }


    private fun dismissMenu() {
        popupMenu?.dismiss()
        popupMenu = null
        findViewById<FloatingActionButton>(R.id.chatbotButton)?.show()

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            isMenuOpen() -> closeMenu()
            supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
            else -> @Suppress("DEPRECATION") super.onBackPressed()
        }
    }
}
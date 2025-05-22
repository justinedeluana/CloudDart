package com.example.clouddart

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatbotActivity : AppCompatActivity() {
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rootContainer: View
    private val messages = mutableListOf<ChatMessage>()

    private lateinit var chatbotService: ChatbotService
    private var isTyping = false
    private var isInitialized = false

    companion object {
        private const val TAG = "ChatbotActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatbot)

        initViews()
        setupUI()
        initializeChatbot()
    }

    private fun initViews() {
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        rootContainer = findViewById(R.id.rootContainer)
    }

    private fun setupUI() {
        setupTouchHandling()
        setupRecyclerView()
        setupSendButton()
        setupMessageInput()
        updateSendButtonState(false) // Disable until initialization
    }

    private fun initializeChatbot() {
        lifecycleScope.launch {
            try {
                showLoadingState(true)
                chatbotService = ChatbotService() // No argument
                chatbotService.initialize() // Call suspend function

                // Add welcome message
                addSystemMessage(
                    """
                    Welcome to CloudDart Airlines! 👋
                    I'm Airi, your virtual assistant. How can I help you today?
                    
                    You can ask me about:
                    • Flight bookings and status
                    • Check-in information
                    • Baggage policies
                    • Travel requirements
                    """.trimIndent()
                )

                isInitialized = true
                updateSendButtonState(messageInput.text.isNotEmpty())

            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                showError("Unable to connect to chat service. Please check your internet connection.")
                // Log to Firebase Analytics
                val bundle = android.os.Bundle().apply {
                    putString("error_message", e.message ?: "Unknown error")
                }
                Firebase.analytics.logEvent("chatbot_init_failed", bundle)
            } finally {
                showLoadingState(false)
            }
        }
    }

    private fun setupTouchHandling() {
        rootContainer.apply {
            isClickable = true
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        animateScale(v, 1.1f)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        animateScale(v, 1.0f)
                        hideKeyboard()
                        v.performClick()
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        animateScale(v, 1.0f)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun hideKeyboard() {
        currentFocus?.let { view ->
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setupMessageInput() {
        messageInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateSendButtonState(s?.isNotEmpty() == true && isInitialized)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatbotActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
            itemAnimator = null // Disable animations for better performance
        }
    }

    private fun setupSendButton() {
        sendButton.setOnClickListener {
            if (!isTyping && isInitialized) {
                messageInput.text.toString().trim().takeIf { it.isNotEmpty() }?.let { message ->
                    sendMessage(message)
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        addUserMessage(message)
        generateBotResponse(message)
    }

    private fun generateBotResponse(userMessage: String) {
        if (!isInitialized) {
            showError("Chat service is not initialized")
            return
        }

        isTyping = true
        updateSendButtonState(false)
        showTypingIndicator()

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    chatbotService.generateResponse(userMessage)
                }

                removeTypingIndicator()
                addBotMessage(response)

                // Log successful interaction
                val bundle = android.os.Bundle().apply {
                    putLong("message_length", userMessage.length.toLong())
                }
                Firebase.analytics.logEvent("message_exchange_success", bundle)

            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                removeTypingIndicator()
                showError("I apologize, but I'm having trouble responding right now. Please try again.")

                // Log error
                val bundle = android.os.Bundle().apply {
                    putString("error_type", e.javaClass.simpleName)
                }
                Firebase.analytics.logEvent("message_exchange_failed", bundle)

            } finally {
                isTyping = false
                updateSendButtonState(messageInput.text.isNotEmpty())
            }
        }
    }

    private fun addUserMessage(message: String) {
        messages.add(ChatMessage(
            content = message,
            isUser = true,
            timestamp = System.currentTimeMillis(),
            username = "justinedeluana"
        ))
        chatAdapter.notifyItemInserted(messages.size - 1)
        messageInput.text.clear()
        scrollToBottom()
    }

    private fun addBotMessage(message: String) {
        messages.add(ChatMessage(
            content = message,
            isUser = false,
            timestamp = System.currentTimeMillis()
        ))
        chatAdapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun addSystemMessage(message: String) {
        messages.add(ChatMessage(
            content = message,
            isUser = false,
            timestamp = System.currentTimeMillis()
        ))
        chatAdapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun showTypingIndicator() {
        messages.add(ChatMessage("Typing...", false, isTyping = true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun removeTypingIndicator() {
        val lastIndex = messages.size - 1
        if (lastIndex >= 0 && messages[lastIndex].isTyping) {
            messages.removeAt(lastIndex)
            chatAdapter.notifyItemRemoved(lastIndex)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        messages.add(ChatMessage(message, false, isError = true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        scrollToBottom()
    }

    private fun showLoadingState(loading: Boolean) {
        // Implement loading UI (progress bar, etc.)
    }

    private fun updateSendButtonState(enabled: Boolean) {
        sendButton.isEnabled = enabled && !isTyping && isInitialized
        sendButton.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                this,
                if (enabled && !isTyping && isInitialized) R.color.activeTint
                else R.color.inactiveTint
            )
        )
    }

    private fun scrollToBottom() {
        chatRecyclerView.post {
            chatRecyclerView.smoothScrollToPosition(messages.size - 1)
        }
    }

    private fun animateScale(view: View, scale: Float) {
        ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, scale),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, scale)
        ).apply {
            duration = 200
            start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::chatbotService.isInitialized) {
            chatbotService.clearHistory()
        }
    }
}


package com.example.clouddart

import android.os.Build
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.firebase.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChatbotService {
    private var client: Any? = null // Temporarily using Any until PaLM API dependencies are added
    private val messageHistory = mutableListOf<ChatMessage>()
    private val firebaseConfig = FirebaseConfig()

    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                // Fetch and activate config using FirebaseConfig
                val apiKey = firebaseConfig.getApiKey()
                if (apiKey.isEmpty()) {
                    throw Exception("API key not configured")
                }

                // Add initial system context with current time
                val currentTime = getCurrentFormattedDateTime()
                addSystemContext(
                    """
                    You are Airi, an airline virtual assistant. Current time: $currentTime
                    Current user: ${getCurrentUser()}
                    Instructions:
                    - Be concise and professional in responses
                    - Provide specific flight-related information when asked
                    - Maintain a helpful and friendly tone
                    - If unsure, acknowledge and ask for clarification
                    """.trimIndent()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                throw e
            }
        }
    }

    suspend fun generateResponse(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                checkNotNull(client) { "Chatbot service not initialized" }

                // Get configuration from FirebaseConfig
                val modelConfig = firebaseConfig.getModelConfig()

                // TODO: Implement actual PaLM API call when dependencies are available
                val botResponse = "I apologize, but I'm currently in development. Please try again later."

                // Update message history with timestamps
                val currentTimestamp = System.currentTimeMillis()
                messageHistory.add(ChatMessage(
                    content = userMessage,
                    isUser = true,
                    timestamp = currentTimestamp,
                    username = getCurrentUser()
                ))
                messageHistory.add(ChatMessage(
                    content = botResponse,
                    isUser = false,
                    timestamp = currentTimestamp + 1
                ))

                botResponse
            } catch (e: Exception) {
                Log.e(TAG, "Error generating response", e)
                val errorMessage = "I apologize, but I'm having trouble processing your request right now. Please try again later."
                messageHistory.add(ChatMessage(
                    content = errorMessage,
                    isUser = false,
                    isError = true,
                    timestamp = System.currentTimeMillis()
                ))
                throw e
            }
        }
    }

    private fun getCurrentFormattedDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    fun clearHistory() {
        messageHistory.clear()
    }

    fun addSystemContext(context: String) {
        messageHistory.add(0, ChatMessage(
            content = context,
            isUser = false,
            timestamp = System.currentTimeMillis()
        ))
    }

    private fun getCurrentUser(): String {
        return "justinedeluana" // Replace with actual user authentication
    }

    companion object {
        private const val TAG = "ChatbotService"
    }
}


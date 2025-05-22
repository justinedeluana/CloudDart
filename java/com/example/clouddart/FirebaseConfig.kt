package com.example.clouddart

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await

class FirebaseConfig {
    private val remoteConfig = Firebase.remoteConfig

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600 // 1 hour cache
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // Set default values
        remoteConfig.setDefaultsAsync(
            mapOf(
                "palm_api_key" to "",
                "api_endpoint" to "https://generativelanguage.googleapis.com/v1beta/models/chat-bison-001",
                "temperature" to 0.7,
                "top_k" to 40,
                "top_p" to 0.95
            )
        )
    }

    suspend fun getApiKey(): String {
        try {
            // Fetch and activate remote config
            remoteConfig.fetchAndActivate().await()
            return remoteConfig.getString("palm_api_key").also {
                if (it.isEmpty()) throw Exception("API key not configured")
            }
        } catch (e: Exception) {
            throw Exception("Failed to fetch API key: ${e.message}")
        }
    }

    suspend fun getModelConfig(): ModelConfig {
        return try {
            remoteConfig.fetchAndActivate().await()
            ModelConfig(
                endpoint = remoteConfig.getString("api_endpoint"),
                temperature = remoteConfig.getDouble("temperature").toFloat(),
                topK = remoteConfig.getLong("top_k").toInt(),
                topP = remoteConfig.getDouble("top_p").toFloat()
            )
        } catch (e: Exception) {
            throw Exception("Failed to fetch model config: ${e.message}")
        }
    }

    data class ModelConfig(
        val endpoint: String,
        val temperature: Float,
        val topK: Int,
        val topP: Float
    )
}
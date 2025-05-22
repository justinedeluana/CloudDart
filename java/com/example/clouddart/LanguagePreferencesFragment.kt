package com.example.clouddart

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import java.util.Locale

class LanguagePreferenceFragment : PreferenceFragmentCompat() {

    companion object {
        private const val TAG = "LanguagePreference"
        const val LANGUAGE_PREFERENCE_KEY = "app_language"
        private const val PREFERENCE_FILE_NAME = "app_preferences"

        // Helper function to get current language that can be called from anywhere
        fun getCurrentLanguage(context: Context): String {
            val preferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
            val defaultLanguage = Locale.getDefault().language
            return preferences.getString(LANGUAGE_PREFERENCE_KEY, defaultLanguage) ?: defaultLanguage
        }

        // Apply saved language to configuration
        fun applyLanguage(context: Context) {
            val languageCode = getCurrentLanguage(context)
            setLocale(context, languageCode)
        }

        private fun setLocale(context: Context, languageCode: String) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            configuration.setLocale(locale)

            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.language_preferences, rootKey)

        // Setup language preference
        val languagePreference = findPreference<ListPreference>(LANGUAGE_PREFERENCE_KEY)
        languagePreference?.let { setupLanguagePreference(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Set up a toolbar or header with back button if needed
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // Add any additional view setup here

        return view
    }

    private fun setupLanguagePreference(preference: ListPreference) {
        // Set current language as summary
        val currentLanguageCode = getCurrentLanguage(requireContext())
        val index = preference.findIndexOfValue(currentLanguageCode)
        if (index >= 0) {
            preference.setValueIndex(index)
            preference.summary = preference.entries[index]
        }

        // Handle language change
        preference.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val languageCode = newValue.toString()
                saveLanguagePreference(requireContext(), languageCode)
                restartApp()
                true
            }
    }

    private fun saveLanguagePreference(context: Context, languageCode: String) {
        // Save selected language
        val preferences = context.getSharedPreferences(PREFERENCE_FILE_NAME, Context.MODE_PRIVATE)
        preferences.edit().putString(LANGUAGE_PREFERENCE_KEY, languageCode).apply()

        // Immediately apply locale change
        setLocale(context, languageCode)

        Log.d(TAG, "Language changed to: $languageCode")
    }

    private fun restartApp() {
        // Restart the app to apply language change
        val intent = requireActivity().packageManager
            .getLaunchIntentForPackage(requireActivity().packageName)
            ?.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

        if (intent != null) {
            requireActivity().finish()
            startActivity(intent)
        } else {
            Log.e(TAG, "Failed to restart the app: Launch intent is null")
        }
    }
}
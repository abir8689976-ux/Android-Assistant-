package com.example.data.service

import android.content.Context
import com.example.BuildConfig

interface ApiKeyStorage {
    fun getCustomApiKey(): String
    fun saveCustomApiKey(key: String)
    fun clearCustomApiKey()
    fun getEffectiveApiKey(): String
    fun hasCustomApiKey(): Boolean
}

class ApiKeyStorageImpl(private val context: Context) : ApiKeyStorage {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getCustomApiKey(): String {
        return prefs.getString(KEY_CUSTOM_API_KEY, "").orEmpty().trim()
    }

    override fun saveCustomApiKey(key: String) {
        prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    override fun clearCustomApiKey() {
        prefs.edit().remove(KEY_CUSTOM_API_KEY).apply()
    }

    override fun getEffectiveApiKey(): String {
        val custom = getCustomApiKey()
        if (custom.isNotBlank()) {
            return custom
        }
        val buildConfigKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            return buildConfigKey
        }
        return ""
    }

    override fun hasCustomApiKey(): Boolean {
        return getCustomApiKey().isNotBlank()
    }

    companion object {
        private const val PREFS_NAME = "aether_api_config"
        private const val KEY_CUSTOM_API_KEY = "custom_gemini_api_key"
    }
}

package com.example.appbase.utils

import com.example.appbase.BuildConfig

/**
 * Utility class for managing API keys securely.
 * API keys are loaded from BuildConfig which reads from local.properties file.
 */
object ApiKeyManager {
    
    /**
     * Get Firebase API key
     * @return Firebase API key or empty string if not configured
     */
    fun getFirebaseApiKey(): String {
        return BuildConfig.FIREBASE_API_KEY.takeIf { it.isNotEmpty() } ?: ""
    }
    
    /**
     * Get Google Maps API key
     * @return Maps API key or empty string if not configured
     */
    fun getMapsApiKey(): String {
        return BuildConfig.MAPS_API_KEY.takeIf { it.isNotEmpty() } ?: ""
    }
    
    /**
     * Get Google Vision API key
     * @return Vision API key or empty string if not configured
     */
    fun getVisionApiKey(): String {
        return BuildConfig.VISION_API_KEY.takeIf { it.isNotEmpty() } ?: ""
    }
    
    /**
     * Check if all required API keys are configured
     * @return true if all keys are present, false otherwise
     */
    fun areApiKeysConfigured(): Boolean {
        return getFirebaseApiKey().isNotEmpty() && 
               getMapsApiKey().isNotEmpty() && 
               getVisionApiKey().isNotEmpty()
    }
    
    /**
     * Get missing API keys for debugging purposes
     * @return List of missing API key names
     */
    fun getMissingApiKeys(): List<String> {
        val missing = mutableListOf<String>()
        if (getFirebaseApiKey().isEmpty()) missing.add("FIREBASE_API_KEY")
        if (getMapsApiKey().isEmpty()) missing.add("MAPS_API_KEY")
        if (getVisionApiKey().isEmpty()) missing.add("VISION_API_KEY")
        return missing
    }
}

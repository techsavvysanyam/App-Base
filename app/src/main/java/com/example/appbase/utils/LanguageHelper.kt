package com.example.appbase.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LanguageHelper {
    
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
    
    fun getCurrentLanguage(context: Context): String {
        val config = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            config.locale.language
        }
    }
    
    fun getLanguageDisplayName(languageCode: String): String {
        val locale = Locale(languageCode)
        return locale.getDisplayName(locale)
    }
}


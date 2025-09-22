@file:Suppress("DEPRECATION")

package com.example.appbase.utils

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LanguageHelper {
    
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

}


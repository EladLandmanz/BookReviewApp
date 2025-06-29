package com.example.bookreviewapp.utils

import android.app.Application
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // This class will have a single instance throughout the app
class LangProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isAppLanguageHebrew(): Boolean {
        val locale = context.resources.configuration.locales[0]
        return locale.language == "iw" || locale.language == "he"
    }
}
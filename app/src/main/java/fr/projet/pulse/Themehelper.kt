package com.pulseo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeHelper {
    private const val THEME_PREFS = "theme_prefs"
    private const val THEME_KEY = "is_dark_mode"

    fun isDarkMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(THEME_KEY, true) // Default to dark mode
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        val prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(THEME_KEY, isDark).apply()

        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun toggleTheme(context: Context) {
        setDarkMode(context, !isDarkMode(context))
    }
}
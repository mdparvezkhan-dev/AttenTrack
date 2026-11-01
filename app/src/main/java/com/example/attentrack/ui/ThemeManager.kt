package com.example.attentrack.ui

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.example.attentrack.R

/** Settings er Theme row: Light / Dark / System default. Choice phone e save thake. */
object ThemeManager {

    private const val PREFS = "ui_prefs"
    private const val KEY_MODE = "theme_mode"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun saved(context: Context): Int =
        prefs(context).getInt(KEY_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

    fun applySaved(context: Context) {
        AppCompatDelegate.setDefaultNightMode(saved(context))
    }

    fun save(context: Context, mode: Int) {
        prefs(context).edit().putInt(KEY_MODE, mode).apply()
        AppCompatDelegate.setDefaultNightMode(mode)   // activity nijei recreate hoy
    }

    fun label(context: Context, mode: Int): String = context.getString(
        when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> R.string.theme_light
            AppCompatDelegate.MODE_NIGHT_YES -> R.string.theme_dark
            else -> R.string.theme_system
        },
    )
}

package com.example.attentrack

import android.app.Application
import com.example.attentrack.data.AppContainer
import com.example.attentrack.ui.ThemeManager

class AttendanceApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        ThemeManager.applySaved(this)   // Light / Dark / System default (Settings theke)
        container = AppContainer(this)
    }
}

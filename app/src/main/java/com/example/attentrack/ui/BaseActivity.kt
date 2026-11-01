package com.example.attentrack.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.attentrack.AttendanceApp
import com.example.attentrack.data.AppContainer

abstract class BaseActivity : AppCompatActivity() {

    protected val container: AppContainer
        get() = (application as AttendanceApp).container

    override fun onCreate(savedInstanceState: Bundle?) {
        // Header gulo dark (wine), tai status bar icon shada
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
    }

    fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

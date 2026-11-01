package com.example.attentrack.ui

import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.attentrack.AttendanceApp
import com.example.attentrack.BuildConfig
import com.example.attentrack.MainActivity
import com.example.attentrack.R
import com.example.attentrack.data.AppContainer
import com.example.attentrack.databinding.ViewPageHeaderBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

abstract class BaseFragment : Fragment() {

    protected val app: AppContainer
        get() = (requireActivity().application as AttendanceApp).container

    protected val host: MainActivity
        get() = requireActivity() as MainActivity

    protected fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected fun sessionExpired() = host.onSessionExpired()

    /** Header er gear icon: teacher / student er nijer Settings screen. */
    protected fun openSettings() {
        findNavController().navigate(if (host.isTeacher) R.id.settingsFragment2 else R.id.settingsFragment)
    }

    /** Teacher ar Student er Settings screen er shared kaj (Theme, Change password, Log out). */
    protected fun bindSettings(
        header: ViewPageHeaderBinding,
        rowTheme: View,
        tvThemeValue: TextView,
        rowPassword: View,
        rowLogout: View,
        tvVersion: TextView,
        changePasswordDestination: Int,
    ) {
        header.appBar.applyInsets(top = true)
        header.toolbar.title = "Settings"
        header.tvMeta.visibility = View.GONE
        header.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        val context = requireContext()
        tvThemeValue.text = ThemeManager.label(context, ThemeManager.saved(context))
        rowTheme.setOnClickListener {
            val modes = intArrayOf(
                AppCompatDelegate.MODE_NIGHT_NO,
                AppCompatDelegate.MODE_NIGHT_YES,
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            )
            val labels = modes.map { ThemeManager.label(context, it) }.toTypedArray()
            val checked = modes.indexOf(ThemeManager.saved(context)).coerceAtLeast(0)
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.choose_theme)
                .setSingleChoiceItems(labels, checked) { dialog, which ->
                    tvThemeValue.text = labels[which]
                    ThemeManager.save(context, modes[which])
                    dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        rowPassword.setOnClickListener { findNavController().navigate(changePasswordDestination) }
        rowLogout.setOnClickListener { host.confirmLogout() }
        tvVersion.text = "Attendance \u00B7 version ${BuildConfig.VERSION_NAME}"
    }
}

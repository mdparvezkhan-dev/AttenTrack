package com.example.attentrack.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attentrack.R
import com.example.attentrack.databinding.FragmentSettings2Binding
import com.example.attentrack.ui.BaseFragment

/** Settings: Theme (Light / Dark / System default), Change password, Log out. */
class SettingsFragment : BaseFragment() {

    private var _b: FragmentSettings2Binding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentSettings2Binding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindSettings(b.header, b.rowTheme, b.tvThemeValue, b.rowPassword, b.rowLogout, b.tvVersion, R.id.changePasswordFragment2)
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

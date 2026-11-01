package com.example.attentrack.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attentrack.R
import com.example.attentrack.databinding.FragmentSettingsBinding
import com.example.attentrack.ui.BaseFragment

class SettingsFragment : BaseFragment() {

    private var _b: FragmentSettingsBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentSettingsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        bindSettings(b.header, b.rowTheme, b.tvThemeValue, b.rowPassword, b.rowLogout, b.tvVersion, R.id.changePasswordFragment)
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

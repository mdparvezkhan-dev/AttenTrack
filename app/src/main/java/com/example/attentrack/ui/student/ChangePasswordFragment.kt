package com.example.attentrack.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attentrack.databinding.FragmentChangePasswordBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.auth.ChangePasswordBinder

class ChangePasswordFragment : BaseFragment() {

    private var _b: FragmentChangePasswordBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ChangePasswordBinder(this, app, b.header, b.etCurrent, b.etNew, b.etConfirm, b.tvError, b.progress, b.btnSave).bind()
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

package com.example.attentrack.ui.auth

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.example.attentrack.data.AppContainer
import com.example.attentrack.databinding.ViewPageHeaderBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.applyInsets

/** Student ar Teacher, duto Change password screen er shared logic. */
class ChangePasswordBinder(
    private val fragment: BaseFragment,
    private val container: AppContainer,
    private val header: ViewPageHeaderBinding,
    private val etCurrent: EditText,
    private val etNew: EditText,
    private val etConfirm: EditText,
    private val tvError: TextView,
    private val progress: View,
    private val btnSave: Button,
) {
    private var pendingNewPassword = ""

    fun bind() {
        header.appBar.applyInsets(top = true)
        header.toolbar.title = "Change password"
        header.tvMeta.visibility = View.GONE
        header.toolbar.setNavigationOnClickListener { fragment.findNavController().navigateUp() }

        val vm = ViewModelProvider(
            fragment,
            viewModelFactory { initializer { ChangePasswordViewModel(container.authRepository) } },
        )[ChangePasswordViewModel::class.java]

        btnSave.setOnClickListener { submit(vm) }
        etConfirm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit(vm)
                true
            } else {
                false
            }
        }

        vm.state.observe(fragment.viewLifecycleOwner) { st ->
            progress.visibility = if (st.loading) View.VISIBLE else View.INVISIBLE
            btnSave.isEnabled = !st.loading
            tvError.text = st.error
            tvError.visibility = if (st.error != null) View.VISIBLE else View.GONE
        }
        vm.done.observe(fragment.viewLifecycleOwner) { event ->
            if (event.getIfNotHandled() != null) {
                container.credentialStore.updatePasswordIfSaved(pendingNewPassword)
                android.widget.Toast.makeText(fragment.requireContext(), "Password changed.", android.widget.Toast.LENGTH_SHORT).show()
                fragment.findNavController().navigateUp()
            }
        }
    }

    private fun submit(vm: ChangePasswordViewModel) {
        pendingNewPassword = etNew.text.toString()
        vm.submit(etCurrent.text.toString(), pendingNewPassword, etConfirm.text.toString())
    }
}

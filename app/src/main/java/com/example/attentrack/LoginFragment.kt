package com.example.attentrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.attentrack.databinding.FragmentLoginBinding
import com.example.attentrack.ui.applyInsets
import com.example.attentrack.ui.auth.LoginViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _b: FragmentLoginBinding? = null
    private val b get() = _b!!

    private val container get() = (requireActivity().application as AttendanceApp).container
    private var pendingId = ""
    private var pendingPassword = ""

    private val vm: LoginViewModel by viewModels {
        viewModelFactory { initializer { LoginViewModel(container.authRepository) } }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentLoginBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.header.applyInsets(top = true)

        // Ager bar save kora ID/password thakle auto bosiye dao
        val creds = container.credentialStore
        b.cbRemember.isChecked = creds.isRememberEnabled()
        creds.load()?.let { (id, password) ->
            b.etUserId.setText(id)
            b.etPassword.setText(password)
        }

        b.btnLogin.setOnClickListener {
            pendingId = b.etUserId.text.toString().trim()
            pendingPassword = b.etPassword.text.toString()
            vm.login(pendingId, pendingPassword)
        }
        b.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                b.btnLogin.performClick()
                true
            } else {
                false
            }
        }

        vm.state.observe(viewLifecycleOwner) { st ->
            b.progress.visibility = if (st.loading) View.VISIBLE else View.INVISIBLE
            b.btnLogin.isEnabled = !st.loading
            b.etUserId.isEnabled = !st.loading
            b.etPassword.isEnabled = !st.loading
            b.tvError.text = st.error
            b.tvError.visibility = if (st.error != null) View.VISIBLE else View.GONE
        }

        vm.success.observe(viewLifecycleOwner) { event ->
            if (event.getIfNotHandled() != null) {
                if (b.cbRemember.isChecked) {
                    container.credentialStore.save(pendingId, pendingPassword)
                } else {
                    container.credentialStore.clear()
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    val session = container.sessionStore.session.first()
                    if (session != null) (requireActivity() as MainActivity).goHome(session.role)
                }
            }
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

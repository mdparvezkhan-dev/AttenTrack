package com.example.attentrack.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.attentrack.data.TeacherDashboardData
import com.example.attentrack.databinding.FragmentTeacherProfileBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.LoadViewModel
import com.example.attentrack.ui.UiState
import com.example.attentrack.ui.applyInsets
import com.example.attentrack.ui.formatShortDate
import com.example.attentrack.ui.initials
import com.example.attentrack.ui.loader
import com.example.attentrack.ui.setInfoRows
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TeacherProfileFragment : BaseFragment() {

    private var _b: FragmentTeacherProfileBinding? = null
    private val b get() = _b!!
    private lateinit var vm: LoadViewModel<TeacherDashboardData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentTeacherProfileBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // dashboard er sathe ek-i data (activity scope)
        val repo = app.teacherRepository
        vm = requireActivity().loader("teacher_dashboard") { sem -> repo.dashboard(sem) }

        b.appBar.applyInsets(top = true)
        b.btnSettings.setOnClickListener { openSettings() }
        b.btnLogout.setOnClickListener { host.confirmLogout() }

        viewLifecycleOwner.lifecycleScope.launch {
            val s = app.sessionStore.session.first() ?: return@launch
            val binding = _b ?: return@launch
            binding.tvAvatar.text = initials(s.name)
            binding.tvName.text = s.name
            binding.tvSubtitle.text = listOfNotNull("Teacher", s.dept).joinToString(" \u00B7 ")
            binding.llInfo.setInfoRows(
                layoutInflater,
                listOfNotNull(
                    "Teacher ID" to s.userId.toString(),
                    s.dept?.let { "Department" to it },
                    s.phone?.let { "Phone" to it },
                ),
            )
        }

        b.tvStat1.text = "-"
        b.tvStat2.text = "-"
        b.tvStat3.text = "-"
        vm.state.observe(viewLifecycleOwner) { state ->
            if (state is UiState.Success) {
                val d = state.data
                b.tvStat1.text = d.summary.courses.toString()
                b.tvStat2.text = d.summary.students.toString()
                b.tvStat3.text = d.summary.classesHeld.toString()
            } else if (state is UiState.Error && state.unauthorized) {
                sessionExpired()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (vm.state.value !is UiState.Success) vm.load(showLoading = false)
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

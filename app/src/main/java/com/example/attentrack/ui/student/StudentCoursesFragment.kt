package com.example.attentrack.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attentrack.R
import com.example.attentrack.data.StudentSummaryData
import com.example.attentrack.databinding.FragmentStudentCoursesBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.LoadViewModel
import com.example.attentrack.ui.UiState
import com.example.attentrack.ui.applyInsets
import com.example.attentrack.ui.hide
import com.example.attentrack.ui.loader
import com.example.attentrack.ui.showEmpty
import com.example.attentrack.ui.showError
import com.example.attentrack.ui.showLoading
import com.example.attentrack.ui.showSemesterMenu

class StudentCoursesFragment : BaseFragment() {

    private var _b: FragmentStudentCoursesBinding? = null
    private val b get() = _b!!
    private lateinit var vm: LoadViewModel<StudentSummaryData>

    private val adapter = StudentCourseAdapter { c ->
        val semesterId = (vm.state.value as? UiState.Success)?.data?.semesterId ?: 0
        findNavController().navigate(
            R.id.studentCourseFragment,
            bundleOf("course_id" to c.courseId, "semester_id" to semesterId, "code" to c.code, "title" to c.title),
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentStudentCoursesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = app.studentRepository
        vm = requireActivity().loader("student_summary") { sem -> repo.summary(sem) }

        b.header.appBar.applyInsets(top = true)
        b.header.btnSettings.setOnClickListener { openSettings() }
        b.header.tvGreeting.text = "Enrolled"
        b.header.tvName.text = "My courses"

        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter
        b.swipe.setOnRefreshListener { vm.load(showLoading = false) }
        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    override fun onResume() {
        super.onResume()
        vm.load(showLoading = vm.state.value !is UiState.Success)
    }

    private fun render(state: UiState<StudentSummaryData>) {
        b.swipe.isRefreshing = false
        when (state) {
            UiState.Loading -> b.state.showLoading(b.swipe)
            is UiState.Error -> {
                if (state.unauthorized) {
                    sessionExpired()
                } else {
                    b.state.showError(state.message, b.swipe) { vm.load() }
                }
            }
            is UiState.Success -> {
                val d = state.data
                val chip = b.header.tvSemester
                chip.visibility = if (d.semesters.isEmpty()) View.GONE else View.VISIBLE
                chip.text = d.semesters.firstOrNull { it.id == d.semesterId }?.name.orEmpty()
                chip.setOnClickListener { showSemesterMenu(chip, d.semesters) { vm.select(it.id) } }

                adapter.submit(d.courses)
                if (d.courses.isEmpty()) {
                    b.state.showEmpty("You are not enrolled in any course in this semester.", b.swipe)
                } else {
                    b.state.hide(b.swipe)
                }
            }
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

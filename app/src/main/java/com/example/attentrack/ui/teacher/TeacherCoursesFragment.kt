package com.example.attentrack.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attentrack.R
import com.example.attentrack.data.TeacherCoursesData
import com.example.attentrack.databinding.FragmentTeacherCoursesBinding
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

class TeacherCoursesFragment : BaseFragment() {

    private var _b: FragmentTeacherCoursesBinding? = null
    private val b get() = _b!!
    private lateinit var vm: LoadViewModel<TeacherCoursesData>
    private var semesterName = ""

    private val adapter = TeacherCourseAdapter { c ->
        findNavController().navigate(
            R.id.courseHubFragment,
            CourseHubFragment.args(
                assignmentId = c.assignmentId,
                code = c.code,
                title = c.title,
                meta = "$semesterName \u00B7 ${c.batch} \u00B7 Section ${c.section}",
            ),
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentTeacherCoursesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = app.teacherRepository
        vm = requireActivity().loader("teacher_courses") { sem -> repo.courses(sem) }

        b.header.appBar.applyInsets(top = true)
        b.header.btnSettings.setOnClickListener { openSettings() }
        b.header.tvGreeting.text = "Teaching"
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

    private fun render(state: UiState<TeacherCoursesData>) {
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
                semesterName = d.semesters.firstOrNull { it.id == d.semesterId }?.name.orEmpty()
                val chip = b.header.tvSemester
                chip.visibility = if (d.semesters.isEmpty()) View.GONE else View.VISIBLE
                chip.text = semesterName
                chip.setOnClickListener { showSemesterMenu(chip, d.semesters) { vm.select(it.id) } }

                adapter.submit(d.courses)
                if (d.courses.isEmpty()) {
                    b.state.showEmpty("No courses are assigned to you in this semester.", b.swipe)
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

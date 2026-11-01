package com.example.attentrack.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.attentrack.R
import com.example.attentrack.data.DashCourseDto
import com.example.attentrack.data.TeacherDashboardData
import com.example.attentrack.databinding.FragmentTeacherDashboardBinding
import com.example.attentrack.databinding.ItemDashCourseBinding
import com.example.attentrack.databinding.ItemLowStudentBinding
import com.example.attentrack.databinding.ItemRecentSessionBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.LoadViewModel
import com.example.attentrack.ui.UiState
import com.example.attentrack.ui.addDivider
import com.example.attentrack.ui.applyInsets
import com.example.attentrack.ui.color
import com.example.attentrack.ui.formatDate
import com.example.attentrack.ui.formatShortDate
import com.example.attentrack.ui.greeting
import com.example.attentrack.ui.hide
import com.example.attentrack.ui.initials
import com.example.attentrack.ui.loader
import com.example.attentrack.ui.pctColorRes
import com.example.attentrack.ui.pctText
import com.example.attentrack.ui.setBadge
import com.example.attentrack.ui.showError
import com.example.attentrack.ui.showLoading
import com.example.attentrack.ui.showSemesterMenu
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Teacher dashboard: summary, course wise %, trend chart, low attendance students, recent attendance. */
class TeacherDashboardFragment : BaseFragment() {

    private var _b: FragmentTeacherDashboardBinding? = null
    private val b get() = _b!!
    private lateinit var vm: LoadViewModel<TeacherDashboardData>
    private var semesterName = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentTeacherDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = app.teacherRepository
        vm = requireActivity().loader("teacher_dashboard") { sem -> repo.dashboard(sem) }

        b.header.appBar.applyInsets(top = true)
        b.header.btnSettings.setOnClickListener { openSettings() }
        b.header.tvGreeting.text = greeting()
        viewLifecycleOwner.lifecycleScope.launch {
            app.sessionStore.session.first()?.let { _b?.header?.tvName?.text = it.name }
        }

        b.swipe.setOnRefreshListener { vm.load(showLoading = false) }
        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    override fun onResume() {
        super.onResume()
        vm.load(showLoading = vm.state.value !is UiState.Success)
    }

    private fun render(state: UiState<TeacherDashboardData>) {
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
                b.state.hide(b.swipe)
                bind(state.data)
            }
        }
    }

    private fun meta(batch: String, section: String) = "$semesterName \u00B7 $batch \u00B7 Section $section"

    private fun openHub(c: DashCourseDto, tab: String, date: String? = null) {
        findNavController().navigate(
            R.id.courseHubFragment,
            CourseHubFragment.args(c.assignmentId, c.code, c.title, meta(c.batch, c.section), tab, date),
        )
    }

    private fun openHub(assignmentId: Int, tab: String, date: String? = null) {
        val course = vm.state.value.let { (it as? UiState.Success)?.data?.courses }?.firstOrNull { it.assignmentId == assignmentId }
            ?: return
        openHub(course, tab, date)
    }

    private fun bind(d: TeacherDashboardData) {
        semesterName = d.semesters.firstOrNull { it.id == d.semesterId }?.name.orEmpty()

        // header semester chip
        val chip = b.header.tvSemester
        chip.visibility = if (d.semesters.isEmpty()) View.GONE else View.VISIBLE
        chip.text = semesterName
        chip.setOnClickListener { showSemesterMenu(chip, d.semesters) { vm.select(it.id) } }

        // summary
        b.tvStatCourses.text = d.summary.courses.toString()
        b.tvStatStudents.text = d.summary.students.toString()
        b.tvStatClasses.text = d.summary.classesHeld.toString()
        b.tvStatAvg.text = pctText(d.summary.percentage)
        b.tvStatAvg.setTextColor(
            requireContext().color(pctColorRes(d.summary.percentage, (d.summary.percentage ?: 100.0) < d.lowAttendanceBelow)),
        )

        // quick attendance
        b.btnQuickAttendance.setOnClickListener {
            when (d.courses.size) {
                0 -> toast("No courses are assigned to you in this semester.")
                1 -> openHub(d.courses[0], CourseHubFragment.TAB_ATTENDANCE)
                else -> {
                    val popup = PopupMenu(requireContext(), b.btnQuickAttendance)
                    d.courses.forEachIndexed { i, c -> popup.menu.add(0, i, i, "${c.code} \u00B7 ${c.batch} \u00B7 ${c.section}") }
                    popup.setOnMenuItemClickListener { item ->
                        openHub(d.courses[item.itemId], CourseHubFragment.TAB_ATTENDANCE)
                        true
                    }
                    popup.show()
                }
            }
        }

        // courses
        b.llCourses.removeAllViews()
        d.courses.forEachIndexed { index, c ->
            val row = ItemDashCourseBinding.inflate(layoutInflater, b.llCourses, false)
            val color = requireContext().color(pctColorRes(c.percentage, (c.percentage ?: 100.0) < d.lowAttendanceBelow))
            row.tvCode.text = c.code
            row.tvTitle.text = c.title
            row.tvMeta.text = "${c.batch} \u00B7 Section ${c.section} \u00B7 ${c.studentCount} students \u00B7 ${c.classesHeld} classes"
            row.tvPercent.text = pctText(c.percentage)
            row.tvPercent.setTextColor(color)
            row.progress.setIndicatorColor(color)
            row.progress.setProgressCompat((c.percentage ?: 0.0).roundToInt(), false)
            row.row.setOnClickListener { openHub(c, CourseHubFragment.TAB_ATTENDANCE) }
            b.llCourses.addView(row.root)
            if (index < d.courses.size - 1) b.llCourses.addDivider()
        }
        b.tvCoursesEmpty.visibility = if (d.courses.isEmpty()) View.VISIBLE else View.GONE


        // low attendance students
        b.llLow.removeAllViews()
        d.lowStudents.forEachIndexed { index, s ->
            val row = ItemLowStudentBinding.inflate(layoutInflater, b.llLow, false)
            row.tvAvatar.text = initials(s.name)
            row.tvName.text = s.name
            row.tvMeta.text = "${s.code} \u00B7 attended ${s.attended} of ${s.totalClasses} classes"
            row.tvPercent.text = pctText(s.percentage)
            row.row.setOnClickListener { openHub(s.assignmentId, CourseHubFragment.TAB_REPORT) }
            b.llLow.addView(row.root)
            if (index < d.lowStudents.size - 1) b.llLow.addDivider()
        }
        if (d.lowStudentsCount > 0) {
            b.tvLowCount.visibility = View.VISIBLE
            b.tvLowCount.text = "${d.lowStudentsCount} students"
            b.tvLowEmpty.visibility = View.GONE
        } else {
            b.tvLowCount.visibility = View.GONE
            b.tvLowEmpty.visibility = View.VISIBLE
            b.tvLowEmpty.text = if (d.summary.classesHeld == 0) {
                "No attendance has been taken yet."
            } else {
                "Great! Every student is at or above ${d.lowAttendanceBelow}%."
            }
        }

        // recent attendance
        b.llRecent.removeAllViews()
        d.recentSessions.forEachIndexed { index, s ->
            val row = ItemRecentSessionBinding.inflate(layoutInflater, b.llRecent, false)
            row.tvTitle.text = "${s.code} \u00B7 ${s.batch} \u00B7 ${s.section}"
            row.tvDate.text = formatDate(s.date)
            row.tvCounts.text = "Present ${s.present} \u00B7 Late ${s.late} \u00B7 Absent ${s.absent}"
            if (s.canEdit) {
                row.tvBadge.setBadge("Editable", R.color.brand_on_primary_container, R.color.brand_primary_container)
            } else {
                row.tvBadge.setBadge("Locked", R.color.text_secondary, R.color.divider)
            }
            row.row.setOnClickListener { openHub(s.assignmentId, CourseHubFragment.TAB_ATTENDANCE, s.date) }
            b.llRecent.addView(row.root)
            if (index < d.recentSessions.size - 1) b.llRecent.addDivider()
        }
        b.tvRecentEmpty.visibility = if (d.recentSessions.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

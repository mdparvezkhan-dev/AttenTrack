package com.example.attentrack.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.attentrack.R
import com.example.attentrack.data.StatsDto
import com.example.attentrack.data.StudentCourseDto
import com.example.attentrack.data.StudentSummaryData
import com.example.attentrack.databinding.FragmentStudentDashboardBinding
import com.example.attentrack.databinding.ItemDashCourseBinding
import com.example.attentrack.databinding.ItemRecentActivityBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.LoadViewModel
import com.example.attentrack.ui.UiState
import com.example.attentrack.ui.addDivider
import com.example.attentrack.ui.applyInsets
import com.example.attentrack.ui.color
import com.example.attentrack.ui.formatDate
import com.example.attentrack.ui.greeting
import com.example.attentrack.ui.hide
import com.example.attentrack.ui.loader
import com.example.attentrack.ui.pctColorRes
import com.example.attentrack.ui.pctText
import com.example.attentrack.ui.setStatusBadge
import com.example.attentrack.ui.showError
import com.example.attentrack.ui.showLoading
import com.example.attentrack.ui.showSemesterMenu
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Student home: overall percentage (circular), low attendance courses, recent classes. */
class StudentDashboardFragment : BaseFragment() {

    private var _b: FragmentStudentDashboardBinding? = null
    private val b get() = _b!!
    private lateinit var vm: LoadViewModel<StudentSummaryData>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentStudentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val repo = app.studentRepository
        vm = requireActivity().loader("student_summary") { sem -> repo.summary(sem) }

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
                b.state.hide(b.swipe)
                bind(state.data)
            }
        }
    }

    private fun openCourse(c: StudentCourseDto, semesterId: Int) {
        findNavController().navigate(
            R.id.studentCourseFragment,
            bundleOf(
                "course_id" to c.courseId,
                "semester_id" to semesterId,
                "code" to c.code,
                "title" to c.title,
            ),
        )
    }

    private fun bind(d: StudentSummaryData) {
        val chip = b.header.tvSemester
        chip.visibility = if (d.semesters.isEmpty()) View.GONE else View.VISIBLE
        chip.text = d.semesters.firstOrNull { it.id == d.semesterId }?.name.orEmpty()
        chip.setOnClickListener { showSemesterMenu(chip, d.semesters) { vm.select(it.id) } }

        bindOverall(d.overall, d.lowAttendanceBelow)

        // courses below the minimum
        val low = d.courses.filter { it.lowAttendance }
        b.llAttention.removeAllViews()
        low.forEachIndexed { index, c ->
            val row = ItemDashCourseBinding.inflate(layoutInflater, b.llAttention, false)
            val color = requireContext().color(pctColorRes(c.percentage, true))
            row.tvCode.text = c.code
            row.tvTitle.text = c.title
            row.tvMeta.text = "Attended ${c.attended} of ${c.totalClasses} classes"
            row.tvPercent.text = pctText(c.percentage)
            row.tvPercent.setTextColor(color)
            row.progress.setIndicatorColor(color)
            row.progress.setProgressCompat((c.percentage ?: 0.0).roundToInt(), false)
            row.row.setOnClickListener { openCourse(c, d.semesterId) }
            b.llAttention.addView(row.root)
            if (index < low.size - 1) b.llAttention.addDivider()
        }
        b.tvAttentionEmpty.visibility = if (low.isEmpty()) View.VISIBLE else View.GONE
        b.tvAttentionEmpty.text = if (d.overall.totalClasses == 0) {
            "No classes have been recorded yet."
        } else {
            "Great! All your courses are at or above ${d.lowAttendanceBelow}%."
        }

        // recent classes
        b.llRecent.removeAllViews()
        d.recent.forEachIndexed { index, r ->
            val row = ItemRecentActivityBinding.inflate(layoutInflater, b.llRecent, false)
            row.tvCode.text = r.code
            row.tvDate.text = formatDate(r.date)
            row.tvStatus.setStatusBadge(r.status)
            b.llRecent.addView(row.root)
            if (index < d.recent.size - 1) b.llRecent.addDivider()
        }
        b.tvRecentEmpty.visibility = if (d.recent.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun bindOverall(s: StatsDto, lowBelow: Int) {
        val color = requireContext().color(pctColorRes(s.percentage, s.lowAttendance))
        b.tvOverallPct.text = pctText(s.percentage)
        b.tvOverallPct.setTextColor(color)
        b.progressOverall.setIndicatorColor(color)
        b.progressOverall.setProgressCompat((s.percentage ?: 0.0).roundToInt(), true)

        b.tvStatTotal.text = "${s.totalClasses}\nClasses"
        b.tvStatAttended.text = "${s.attended}\nAttended"
        b.tvStatLate.text = "${s.late}\nLate"
        b.tvStatAbsent.text = "${s.absent}\nAbsent"

        if (s.lowAttendance) {
            b.tvWarning.text = "Your attendance is below $lowBelow%. Try not to miss the upcoming classes."
            b.tvWarning.visibility = View.VISIBLE
        } else {
            b.tvWarning.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

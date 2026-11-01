package com.example.attentrack.ui.student

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attentrack.data.CourseDetailData
import com.example.attentrack.databinding.FragmentStudentCourseBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.LoadViewModel
import com.example.attentrack.ui.UiState
import com.example.attentrack.ui.applyInsets
import com.example.attentrack.ui.color
import com.example.attentrack.ui.formatDate
import com.example.attentrack.ui.hide
import com.example.attentrack.ui.loader
import com.example.attentrack.ui.pctColorRes
import com.example.attentrack.ui.pctText
import com.example.attentrack.ui.showError
import com.example.attentrack.ui.showLoading
import kotlin.math.roundToInt

/** Ekta course er date wise present/late/absent ar late er dates. */
class StudentCourseFragment : BaseFragment() {

    private var _b: FragmentStudentCourseBinding? = null
    private val b get() = _b!!
    private lateinit var vm: LoadViewModel<CourseDetailData>
    private val adapter = RecordAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentStudentCourseBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        val courseId = args.getInt("course_id")
        val semesterId = args.getInt("semester_id")
        val repo = app.studentRepository
        vm = loader("course") { sem -> repo.course(courseId, sem) }
        vm.setInitialOption(if (semesterId > 0) semesterId else null)

        b.header.appBar.applyInsets(top = true)
        b.header.toolbar.title = args.getString("code").orEmpty()
        b.header.toolbar.subtitle = args.getString("title").orEmpty()
        b.header.tvMeta.visibility = View.GONE
        b.header.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter
        b.swipe.setOnRefreshListener { vm.load(showLoading = false) }
        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    override fun onResume() {
        super.onResume()
        vm.load(showLoading = vm.state.value !is UiState.Success)
    }

    private fun render(state: UiState<CourseDetailData>) {
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
                val s = d.stats
                val color = requireContext().color(pctColorRes(s.percentage, s.lowAttendance))

                b.tvPct.text = pctText(s.percentage)
                b.tvPct.setTextColor(color)
                b.progressPct.setIndicatorColor(color)
                b.progressPct.setProgressCompat((s.percentage ?: 0.0).roundToInt(), true)
                b.tvCounts.text = if (s.totalClasses == 0) {
                    "No classes have been held yet."
                } else {
                    "${s.totalClasses} classes held\nPresent ${s.present} \u00B7 Late ${s.late} \u00B7 Absent ${s.absent}"
                }

                if (s.lowAttendance) {
                    b.tvWarning.text = "Your attendance in this course is low. Try not to miss the upcoming classes."
                    b.tvWarning.visibility = View.VISIBLE
                } else {
                    b.tvWarning.visibility = View.GONE
                }

                if (d.lateDates.isEmpty()) {
                    b.cardLate.visibility = View.GONE
                } else {
                    b.cardLate.visibility = View.VISIBLE
                    b.tvLateTitle.text = "You were late ${d.lateDates.size} time" + (if (d.lateDates.size == 1) "" else "s")
                    b.tvLateDates.text = d.lateDates.joinToString("\n") { "\u2022 ${formatDate(it)}" }
                }

                b.tvRecordsLabel.text = "Date-wise attendance (${d.records.size})"
                adapter.submit(d.records)
                b.state.hide(b.swipe)
            }
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

package com.example.attentrack.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.attentrack.R
import com.example.attentrack.databinding.FragmentCourseHubBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.applyInsets

/** Hub er shob tab (Attendance, History, Report) ei ViewModel share kore. */
class HubViewModel : ViewModel() {
    var assignmentId: Int = 0
    var pendingDate: String? = null

    fun takePendingDate(): String? {
        val d = pendingDate
        pendingDate = null
        return d
    }
}

/** Ekta course er screen: upore Attendance / History / Report pill tab. */
class CourseHubFragment : BaseFragment() {

    companion object {
        const val TAB_ATTENDANCE = "attendance"
        const val TAB_HISTORY = "history"
        const val TAB_REPORT = "report"

        private const val ARG_ID = "assignment_id"
        private const val ARG_CODE = "code"
        private const val ARG_TITLE = "title"
        private const val ARG_META = "meta"
        private const val ARG_TAB = "tab"
        private const val ARG_DATE = "date"

        fun args(
            assignmentId: Int,
            code: String,
            title: String,
            meta: String,
            tab: String = TAB_ATTENDANCE,
            date: String? = null,
        ): Bundle = bundleOf(
            ARG_ID to assignmentId,
            ARG_CODE to code,
            ARG_TITLE to title,
            ARG_META to meta,
            ARG_TAB to tab,
            ARG_DATE to date,
        )
    }

    private var _b: FragmentCourseHubBinding? = null
    private val b get() = _b!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentCourseHubBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        val hub = ViewModelProvider(this)[HubViewModel::class.java]

        b.header.appBar.applyInsets(top = true)
        b.header.toolbar.title = args.getString(ARG_CODE).orEmpty()
        b.header.toolbar.subtitle = args.getString(ARG_TITLE).orEmpty()
        b.header.tvMeta.text = args.getString(ARG_META).orEmpty()
        b.header.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        b.toggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) showTab(checkedId)
        }

        if (savedInstanceState == null) {
            hub.assignmentId = args.getInt(ARG_ID)
            hub.pendingDate = args.getString(ARG_DATE)
            val first = when (args.getString(ARG_TAB)) {
                TAB_HISTORY -> R.id.btnTabHistory
                TAB_REPORT -> R.id.btnTabReport
                else -> R.id.btnTabAttendance
            }
            b.toggle.check(first)
        }
    }

    private fun showTab(buttonId: Int) {
        val fragment: Fragment = when (buttonId) {
            R.id.btnTabHistory -> HistoryFragment()
            R.id.btnTabReport -> ReportFragment()
            else -> TakeAttendanceFragment()
        }
        childFragmentManager.beginTransaction().replace(R.id.hubContent, fragment).commit()
    }

    /** History theke kono date e click korle Attendance tab e oi date khule. */
    fun openAttendance(date: String) {
        ViewModelProvider(this)[HubViewModel::class.java].pendingDate = date
        b.toggle.check(R.id.btnTabAttendance)
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

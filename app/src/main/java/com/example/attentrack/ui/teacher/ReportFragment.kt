package com.example.attentrack.ui.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attentrack.data.ReportData
import com.example.attentrack.databinding.FragmentReportBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.LoadViewModel
import com.example.attentrack.ui.UiState
import com.example.attentrack.ui.hide
import com.example.attentrack.ui.loader
import com.example.attentrack.ui.showEmpty
import com.example.attentrack.ui.showError
import com.example.attentrack.ui.showLoading

class ReportFragment : BaseFragment() {

    private var _b: FragmentReportBinding? = null
    private val b get() = _b!!
    private lateinit var vm: LoadViewModel<ReportData>
    private val adapter = ReportAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentReportBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parent = requireParentFragment()
        val assignmentId = ViewModelProvider(parent)[HubViewModel::class.java].assignmentId
        val repo = app.teacherRepository
        vm = parent.loader("report") { _ -> repo.report(assignmentId) }

        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter
        b.swipe.setOnRefreshListener { vm.load(showLoading = false) }
        vm.state.observe(viewLifecycleOwner) { render(it) }
    }

    override fun onResume() {
        super.onResume()
        vm.load(showLoading = vm.state.value !is UiState.Success)
    }

    private fun render(state: UiState<ReportData>) {
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
                b.tvInfo.text = "${d.classesHeld} classes held \u00B7 below ${d.lowAttendanceBelow}% is low"
                adapter.submit(d.students)
                if (d.students.isEmpty()) {
                    b.state.showEmpty("No students are registered in this course.", b.swipe)
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

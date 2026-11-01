package com.example.attentrack.ui.teacher

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.example.attentrack.databinding.FragmentTakeAttendanceBinding
import com.example.attentrack.ui.BaseFragment
import com.example.attentrack.ui.UiState
import com.example.attentrack.ui.applyInsets
import com.example.attentrack.ui.formatDate
import com.example.attentrack.ui.hide
import com.example.attentrack.ui.parseIso
import com.example.attentrack.ui.showEmpty
import com.example.attentrack.ui.showError
import com.example.attentrack.ui.showLoading
import com.example.attentrack.ui.todayIso
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Attendance neya: Present / Late checkbox (Present tick na thakle Absent). Neyar 24 ghontar moddhe edit kora jay. */
class TakeAttendanceFragment : BaseFragment() {

    private var _b: FragmentTakeAttendanceBinding? = null
    private val b get() = _b!!

    private lateinit var vm: TakeAttendanceViewModel
    private var rows: List<AttendanceRow> = emptyList()
    private var submitLabel = "Submit attendance"
    private val adapter = AttendanceAdapter { updateSummary() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _b = FragmentTakeAttendanceBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val parent = requireParentFragment()
        val hub = ViewModelProvider(parent)[HubViewModel::class.java]
        val repo = app.teacherRepository
        val assignmentId = hub.assignmentId
        vm = ViewModelProvider(
            parent,
            viewModelFactory { initializer { TakeAttendanceViewModel(repo, assignmentId, todayIso()) } },
        )["attendance", TakeAttendanceViewModel::class.java]
        hub.takePendingDate()?.let { vm.setDate(it) }

        b.rv.layoutManager = LinearLayoutManager(requireContext())
        b.rv.adapter = adapter
        b.rv.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        b.btnDate.setOnClickListener { pickDate() }
        b.btnSelectAll.setOnClickListener {
            rows.forEach { it.present = true; it.late = false }
            adapter.refreshAll()
            updateSummary()
        }
        b.btnMarkAllAbsent.setOnClickListener {
            rows.forEach { it.present = false; it.late = false }
            adapter.refreshAll()
            updateSummary()
        }
        b.etSearch.doOnTextChanged { text, _, _, _ -> adapter.filter(text?.toString().orEmpty()) }
        b.btnSubmit.setOnClickListener { confirmSubmit() }

        vm.screen.observe(viewLifecycleOwner) { renderScreen(it) }
        vm.saving.observe(viewLifecycleOwner) { saving ->
            b.btnSubmit.isEnabled = !saving
            b.btnSubmit.text = if (saving) "Saving..." else submitLabel
        }
        vm.events.observe(viewLifecycleOwner) { event ->
            when (val e = event.getIfNotHandled()) {
                is SaveEvent.Saved -> {
                    toast(e.message)
                    vm.load()   // reload: screen now shows this date as already taken (edit mode)
                }
                is SaveEvent.Failed -> {
                    if (e.unauthorized) {
                        sessionExpired()
                    } else {
                        Snackbar.make(b.root, e.message, Snackbar.LENGTH_LONG).show()
                    }
                }
                null -> Unit
            }
        }
    }

    private fun renderScreen(state: UiState<AttendanceScreen>) {
        b.btnDate.text = formatDate(vm.date)
        when (state) {
            UiState.Loading -> {
                b.state.showLoading(b.content)
                b.bottomBar.visibility = View.GONE
            }
            is UiState.Error -> {
                b.bottomBar.visibility = View.GONE
                if (state.unauthorized) {
                    sessionExpired()
                } else {
                    b.state.showError(state.message, b.content) { vm.load() }
                }
            }
            is UiState.Success -> {
                val s = state.data
                rows = s.rows
                val readOnly = s.taken && !s.canEdit
                adapter.readOnly = readOnly
                adapter.submit(s.rows)
                updateSummary()

                when {
                    s.taken && s.canEdit -> {
                        b.tvBanner.text = "Attendance for this date has been taken. You can edit it within 24 hours of taking it."
                        b.tvBanner.visibility = View.VISIBLE
                    }
                    s.taken -> {
                        b.tvBanner.text = "The 24-hour edit window for this date has ended. You can only view it."
                        b.tvBanner.visibility = View.VISIBLE
                    }
                    else -> b.tvBanner.visibility = View.GONE
                }

                b.btnSelectAll.isEnabled = !readOnly
                b.btnMarkAllAbsent.isEnabled = !readOnly
                submitLabel = if (s.taken) "Update attendance" else "Submit attendance"
                b.btnSubmit.text = submitLabel

                if (s.rows.isEmpty()) {
                    b.state.showEmpty("No students are registered in this course. Ask the admin to enroll students.", b.content)
                    b.bottomBar.visibility = View.GONE
                } else {
                    b.state.hide(b.content)
                    b.bottomBar.visibility = if (readOnly) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun updateSummary() {
        val late = rows.count { it.present && it.late }
        val present = rows.count { it.present && !it.late }
        val absent = rows.count { !it.present }
        b.tvPresent.text = "Present $present"
        b.tvLate.text = "Late $late"
        b.tvAbsent.text = "Absent $absent"
    }

    private fun confirmSubmit() {
        val late = rows.count { it.present && it.late }
        val present = rows.count { it.present && !it.late }
        val absent = rows.count { !it.present }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Save attendance?")
            .setMessage("Date: ${formatDate(vm.date)}\n\nPresent: $present\nLate: $late\nAbsent: $absent\nTotal: ${rows.size}")
            .setPositiveButton("Save") { _, _ -> vm.submit(rows) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun pickDate() {
        val cal = Calendar.getInstance().apply { time = parseIso(vm.date) ?: Date() }
        val dialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day -> vm.setDate(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day)) },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}

package com.example.attentrack.ui.teacher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attentrack.data.ApiResult
import com.example.attentrack.data.AttendanceRecordRequest
import com.example.attentrack.data.StudentsData
import com.example.attentrack.data.TeacherRepository
import com.example.attentrack.ui.Event
import com.example.attentrack.ui.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class AttendanceScreen(
    val rows: List<AttendanceRow>,
    val sessionId: Int?,
    val canEdit: Boolean,
    val taken: Boolean,
)

sealed interface SaveEvent {
    data class Saved(val message: String) : SaveEvent
    data class Failed(val message: String, val unauthorized: Boolean) : SaveEvent
}

class TakeAttendanceViewModel(
    private val repo: TeacherRepository,
    private val assignmentId: Int,
    initialDate: String,
) : ViewModel() {

    var date: String = initialDate
        private set

    private val _screen = MutableLiveData<UiState<AttendanceScreen>>(UiState.Loading)
    val screen: LiveData<UiState<AttendanceScreen>> = _screen

    private val _saving = MutableLiveData(false)
    val saving: LiveData<Boolean> = _saving

    private val _events = MutableLiveData<Event<SaveEvent>>()
    val events: LiveData<Event<SaveEvent>> = _events

    private var loadJob: Job? = null

    init {
        load()
    }

    fun setDate(newDate: String) {
        if (newDate == date) return
        date = newDate
        load()
    }

    fun load() {
        loadJob?.cancel()
        _screen.value = UiState.Loading
        loadJob = viewModelScope.launch {
            _screen.value = when (val r = repo.students(assignmentId, date)) {
                is ApiResult.Failure -> UiState.Error(r.message, r.code == 401)
                is ApiResult.Success -> {
                    val d = r.data
                    if (d == null) {
                        UiState.Error("The server sent an unexpected response.")
                    } else {
                        UiState.Success(toScreen(d))
                    }
                }
            }
        }
    }

    private fun toScreen(d: StudentsData): AttendanceScreen {
        // status null = ekhono attendance nei (ba pore enroll hoyeche), default present
        val rows = d.students.map {
            AttendanceRow(it.id, it.name, it.dept, present = it.status != "absent", late = it.status == "late")
        }
        return AttendanceScreen(
            rows = rows,
            sessionId = d.session?.sessionId,
            canEdit = d.session?.canEdit ?: true,
            taken = d.attendanceTaken,
        )
    }

    fun submit(rows: List<AttendanceRow>) {
        if (_saving.value == true) return
        val screen = (_screen.value as? UiState.Success)?.data ?: return
        if (rows.isEmpty()) return
        val records = rows.map { AttendanceRecordRequest(it.id, it.status) }
        _saving.value = true
        viewModelScope.launch {
            val sessionId = screen.sessionId
            val result = if (screen.taken && sessionId != null) {
                when (val r = repo.update(sessionId, records)) {
                    is ApiResult.Success -> ApiResult.Success(Unit, r.message)
                    is ApiResult.Failure -> r
                }
            } else {
                when (val r = repo.save(assignmentId, date, records)) {
                    is ApiResult.Success -> ApiResult.Success(Unit, r.message)
                    is ApiResult.Failure -> r
                }
            }
            _saving.value = false
            when (result) {
                is ApiResult.Success -> _events.value = Event(SaveEvent.Saved(result.message ?: "Attendance saved."))
                is ApiResult.Failure -> {
                    _events.value = Event(SaveEvent.Failed(result.message, result.code == 401))
                    // 409 = this date already has attendance, reload so the screen switches to edit mode
                    if (result.code == 409) load()
                }
            }
        }
    }
}

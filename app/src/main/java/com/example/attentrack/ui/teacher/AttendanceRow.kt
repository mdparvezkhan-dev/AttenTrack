package com.example.attentrack.ui.teacher

/** Ekta student er attendance row. present = false hole absent; present + late = late. */
class AttendanceRow(
    val id: Long,
    val name: String,
    val dept: String?,
    var present: Boolean,
    var late: Boolean,
) {
    val status: String
        get() = when {
            !present -> "absent"
            late -> "late"
            else -> "present"
        }
}

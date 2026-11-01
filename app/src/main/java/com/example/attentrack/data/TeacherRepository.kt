package com.example.attentrack.data

import com.google.gson.Gson

class TeacherRepository(private val api: ApiService, private val gson: Gson) {

    suspend fun dashboard(semesterId: Int?) = safeApi(gson) { api.teacherDashboard(semesterId) }

    suspend fun courses(semesterId: Int?) = safeApi(gson) { api.teacherCourses(semesterId) }

    suspend fun students(assignmentId: Int, date: String?) =
        safeApi(gson) { api.teacherStudents(assignmentId, date) }

    suspend fun save(assignmentId: Int, date: String, records: List<AttendanceRecordRequest>) =
        safeApi(gson) { api.saveAttendance(SaveAttendanceRequest(assignmentId, date, records)) }

    suspend fun update(sessionId: Int, records: List<AttendanceRecordRequest>) =
        safeApi(gson) { api.updateAttendance(UpdateAttendanceRequest(sessionId, records)) }

    suspend fun history(assignmentId: Int) = safeApi(gson) { api.teacherHistory(assignmentId) }

    suspend fun report(assignmentId: Int) = safeApi(gson) { api.teacherReport(assignmentId) }
}

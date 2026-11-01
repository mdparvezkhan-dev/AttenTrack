package com.example.attentrack.data

// Gson LOWER_CASE_WITH_UNDERSCORES: userId <-> user_id, semesterId <-> semester_id (AppContainer e set kora).
// Shob field e default value ache, jate Gson no-arg constructor pay.

data class ApiEnvelope<T>(
    val success: Boolean = false,
    val message: String? = null,
    val data: T? = null,
)

// ---------- auth ----------
data class LoginRequest(val userId: String, val password: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)

data class ApiUser(
    val id: Long = 0,
    val name: String = "",
    val phone: String? = null,
    val role: String = "",
    val dept: String? = null,
    val batch: String? = null,
    val section: String? = null,
)

data class LoginData(
    val token: String = "",
    val expiresAt: String? = null,
    val user: ApiUser = ApiUser(),
    val mustChangePassword: Boolean = false,
)

// ---------- shared ----------
data class SemesterDto(val id: Int = 0, val name: String = "")

data class AssignmentInfo(
    val assignmentId: Int = 0,
    val code: String = "",
    val title: String = "",
    val semester: String = "",
    val batch: String = "",
    val section: String = "",
)

// total/present/late/absent + percentage (late = present hishebe gona, late alada ashe)
data class StatsDto(
    val totalClasses: Int = 0,
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val attended: Int = 0,
    val percentage: Double? = null,
    val lowAttendance: Boolean = false,
)

// ---------- teacher ----------
data class TeacherCourseDto(
    val assignmentId: Int = 0,
    val semesterId: Int = 0,
    val semester: String = "",
    val courseId: Int = 0,
    val code: String = "",
    val title: String = "",
    val batch: String = "",
    val section: String = "",
    val studentCount: Int = 0,
    val classesHeld: Int = 0,
    val lastClassDate: String? = null,
)

data class TeacherCoursesData(
    val semesterId: Int = 0,
    val semesters: List<SemesterDto> = emptyList(),
    val courses: List<TeacherCourseDto> = emptyList(),
)

data class StudentRowDto(
    val id: Long = 0,
    val name: String = "",
    val dept: String? = null,
    val status: String? = null,
)

data class SessionInfo(val sessionId: Int = 0, val canEdit: Boolean = false)

data class StudentsData(
    val assignment: AssignmentInfo = AssignmentInfo(),
    val date: String? = null,
    val attendanceTaken: Boolean = false,
    val session: SessionInfo? = null,
    val students: List<StudentRowDto> = emptyList(),
)

data class AttendanceRecordRequest(val studentId: Long, val status: String)
data class SaveAttendanceRequest(val assignmentId: Int, val date: String, val records: List<AttendanceRecordRequest>)
data class UpdateAttendanceRequest(val sessionId: Int, val records: List<AttendanceRecordRequest>)

data class SaveResult(
    val sessionId: Int = 0,
    val date: String? = null,
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
)

data class UpdateResult(val sessionId: Int = 0, val updated: Int = 0)

data class HistorySessionDto(
    val sessionId: Int = 0,
    val date: String = "",
    val takenAt: String? = null,
    val total: Int = 0,
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val canEdit: Boolean = false,
)

data class HistoryData(
    val assignment: AssignmentInfo = AssignmentInfo(),
    val sessions: List<HistorySessionDto> = emptyList(),
)

data class ReportStudentDto(
    val studentId: Long = 0,
    val name: String = "",
    val totalClasses: Int = 0,
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val attended: Int = 0,
    val percentage: Double? = null,
    val lowAttendance: Boolean = false,
)

data class ReportData(
    val assignment: AssignmentInfo = AssignmentInfo(),
    val classesHeld: Int = 0,
    val lowAttendanceBelow: Int = 75,
    val students: List<ReportStudentDto> = emptyList(),
)

// ---------- student ----------
data class StudentCourseDto(
    val courseId: Int = 0,
    val code: String = "",
    val title: String = "",
    val credit: Double? = null,
    val teachers: String? = null,
    val totalClasses: Int = 0,
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val attended: Int = 0,
    val percentage: Double? = null,
    val lowAttendance: Boolean = false,
)

data class StudentSummaryData(
    val semesterId: Int = 0,
    val semesters: List<SemesterDto> = emptyList(),
    val lowAttendanceBelow: Int = 75,
    val overall: StatsDto = StatsDto(),
    val courses: List<StudentCourseDto> = emptyList(),
    val trend: List<TrendPoint> = emptyList(),
    val recent: List<RecentRecordDto> = emptyList(),
)

data class CourseInfo(
    val courseId: Int = 0,
    val code: String = "",
    val title: String = "",
    val semester: String = "",
)

data class RecordDto(val date: String = "", val status: String = "")

data class CourseDetailData(
    val course: CourseInfo = CourseInfo(),
    val semesterId: Int = 0,
    val stats: StatsDto = StatsDto(),
    val lateDates: List<String> = emptyList(),
    val records: List<RecordDto> = emptyList(),
)

// ---------- teacher dashboard ----------
data class DashSummary(
    val courses: Int = 0,
    val students: Int = 0,
    val classesHeld: Int = 0,
    val percentage: Double? = null,
)

data class DashCourseDto(
    val assignmentId: Int = 0,
    val code: String = "",
    val title: String = "",
    val batch: String = "",
    val section: String = "",
    val studentCount: Int = 0,
    val classesHeld: Int = 0,
    val percentage: Double? = null,
)

data class RecentSessionDto(
    val sessionId: Int = 0,
    val assignmentId: Int = 0,
    val code: String = "",
    val title: String = "",
    val batch: String = "",
    val section: String = "",
    val date: String = "",
    val total: Int = 0,
    val present: Int = 0,
    val late: Int = 0,
    val absent: Int = 0,
    val percentage: Double? = null,
    val canEdit: Boolean = false,
)

data class TrendPoint(
    val date: String = "",
    val code: String? = null,
    val percentage: Double? = null,
)

data class LowStudentDto(
    val studentId: Long = 0,
    val name: String = "",
    val assignmentId: Int = 0,
    val code: String = "",
    val totalClasses: Int = 0,
    val attended: Int = 0,
    val percentage: Double? = null,
)

data class TeacherDashboardData(
    val semesterId: Int = 0,
    val semesters: List<SemesterDto> = emptyList(),
    val lowAttendanceBelow: Int = 75,
    val summary: DashSummary = DashSummary(),
    val courses: List<DashCourseDto> = emptyList(),
    val recentSessions: List<RecentSessionDto> = emptyList(),
    val trend: List<TrendPoint> = emptyList(),
    val lowStudents: List<LowStudentDto> = emptyList(),
    val lowStudentsCount: Int = 0,
)

// ---------- student extras ----------
data class RecentRecordDto(
    val date: String = "",
    val code: String = "",
    val status: String = "",
)

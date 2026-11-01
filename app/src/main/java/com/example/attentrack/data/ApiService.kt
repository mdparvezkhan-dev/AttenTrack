package com.example.attentrack.data

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    // ---------- auth ----------
    @POST("login.php")
    suspend fun login(@Body body: LoginRequest): Response<ApiEnvelope<LoginData>>

    @POST("logout.php")
    suspend fun logout(): Response<ApiEnvelope<JsonElement>>

    @POST("change_password.php")
    suspend fun changePassword(@Body body: ChangePasswordRequest): Response<ApiEnvelope<JsonElement>>

    // ---------- teacher ----------
    @GET("teacher/dashboard.php")
    suspend fun teacherDashboard(@Query("semester_id") semesterId: Int?): Response<ApiEnvelope<TeacherDashboardData>>

    @GET("teacher/courses.php")
    suspend fun teacherCourses(@Query("semester_id") semesterId: Int?): Response<ApiEnvelope<TeacherCoursesData>>

    @GET("teacher/students.php")
    suspend fun teacherStudents(
        @Query("assignment_id") assignmentId: Int,
        @Query("date") date: String?,
    ): Response<ApiEnvelope<StudentsData>>

    @POST("teacher/attendance.php")
    suspend fun saveAttendance(@Body body: SaveAttendanceRequest): Response<ApiEnvelope<SaveResult>>

    @POST("teacher/attendance_update.php")
    suspend fun updateAttendance(@Body body: UpdateAttendanceRequest): Response<ApiEnvelope<UpdateResult>>

    @GET("teacher/history.php")
    suspend fun teacherHistory(@Query("assignment_id") assignmentId: Int): Response<ApiEnvelope<HistoryData>>

    @GET("teacher/report.php")
    suspend fun teacherReport(@Query("assignment_id") assignmentId: Int): Response<ApiEnvelope<ReportData>>

    // ---------- student ----------
    @GET("student/summary.php")
    suspend fun studentSummary(@Query("semester_id") semesterId: Int?): Response<ApiEnvelope<StudentSummaryData>>

    @GET("student/course.php")
    suspend fun studentCourse(
        @Query("course_id") courseId: Int,
        @Query("semester_id") semesterId: Int?,
    ): Response<ApiEnvelope<CourseDetailData>>
}

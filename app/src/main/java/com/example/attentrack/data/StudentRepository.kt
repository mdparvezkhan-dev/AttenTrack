package com.example.attentrack.data

import com.google.gson.Gson

class StudentRepository(private val api: ApiService, private val gson: Gson) {

    suspend fun summary(semesterId: Int?) = safeApi(gson) { api.studentSummary(semesterId) }

    suspend fun course(courseId: Int, semesterId: Int?) =
        safeApi(gson) { api.studentCourse(courseId, semesterId) }
}

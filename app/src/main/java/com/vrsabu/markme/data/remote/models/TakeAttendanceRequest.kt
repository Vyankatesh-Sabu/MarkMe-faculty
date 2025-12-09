package com.vrsabu.markme.data.remote.models



data class TakeAttendanceRequest(
    val facultyId: Long,
    val courseId: Long,
    val classes: List<Class>,
    val sessionDate: String,
)

data class Class(
    val branch: String,
    val section: String?,
)
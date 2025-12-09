package com.vrsabu.markme.data.remote.models

data class TakeAttendanceReponse(
    val statusCode: Long,
    val data: Dat,
    val message: String,
    val success: Boolean,
)

data class Dat(
    val createdSession: CreatedSession,
)

data class CreatedSession(
    val id: Long,
    val facultyId: Long,
    val courseId: Long,
    val sessionDate: String,
    val room: String,
    val classes: Classes,
    val supabaseImage: String,
    val mlStatus: String,
    val mlResult: Any?,
    val createdAt: String,
    val updatedAt: String,
)

data class Classes(
    val classes: List<Clas>,
)

data class Clas(
    val branch: String,
    val section: String?,
)

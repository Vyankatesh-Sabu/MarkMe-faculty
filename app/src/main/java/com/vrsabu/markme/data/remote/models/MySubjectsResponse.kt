package com.vrsabu.markme.data.remote.models

data class MySubjectsResponse(
    val statusCode: Long,
    val data: List<Daum>,
    val message: String,
    val success: Boolean,
)

data class Daum(
    val assignmentId: Long,
    val academicYear: String,
    val isEvenSemester: Boolean,
    val facultyId: Long,
    val courseId: Long,
    val createdAt: String,
    val updatedAt: String,
    val faculty: Faculty,
    val course: Course,
)


data class Course(
    val id: Long,
    val courseName: String,
    val credits: Long,
    val description: String,
)


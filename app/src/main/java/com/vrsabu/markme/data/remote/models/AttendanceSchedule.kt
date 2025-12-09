package com.vrsabu.markme.data.remote.models

data class AttendanceSchedule(
    val id: Long? = null,
    val facultyId: Long,
    val courseId: Long,
    val startTime: String,
    val endTime: String,
    val room: String,
    val day: DaySchedule
)

data class DaySchedule(
    val monday: Boolean = false,
    val tuesday: Boolean = false,
    val wednesday: Boolean = false,
    val thursday: Boolean = false,
    val friday: Boolean = false,
    val saturday: Boolean = false,
    val sunday: Boolean = false
)

data class ScheduleRequest(
    val facultyId: Long,
    val courseId: Long,
    val startTime: String,
    val endTime: String,
    val room: String,
    val day: DaySchedule
)

data class ScheduleResponse(
    val id: Long,
    val facultyId: Long,
    val courseId: Long,
    val courseName: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val day: DaySchedule
)

data class ScheduleListResponse(
    val success: Boolean,
    val message: String,
    val data: List<ScheduleResponse>
)


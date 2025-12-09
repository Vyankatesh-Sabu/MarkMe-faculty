package com.vrsabu.markme.data.remote.models

// Top-level response
data class AttendanceResponse(
    val success: Boolean,
    val message: String?,
    val data: AttendanceData?
)

data class AttendanceData(
    val course: Course?,
    val faculty: Faculty?,
    val attendanceByDate: Map<String, AttendanceDateEntry>?,
    val studentStats: List<StudentStatResponse>?,
    val overallStats: OverallStatsResponse?
)

// attendance by date entry (object keyed by arbitrary keys)
data class AttendanceDateEntry(
    val date: String?,
    val session: AttendanceSession?,
    val faculty: Any?,
    val students: List<StudentAttendance>?
)

data class AttendanceSession(
    val id: Long?,
    val sessionDate: String?,
    val room: String?,
    val classes: Map<String, Any>?,
    val mlStatus: String?
)

data class StudentAttendance(
    val student: StudentInfo?,
    val isPresent: Boolean?
)

// Simplified student model used elsewhere
data class StudentInfo(
    val id: Long?,
    val firstName: String?,
    val lastName: String?,
    val rollNumber: String?,
    val branch: String?,
    val section: String?,
    val subsection: String?
)

// student stat item in response
data class StudentStatResponse(
    val student: StudentInfo?,
    val totalClasses: Long?,
    val presentCount: Long?,
    val attendancePercentage: String?
)

// overall stats in response
data class OverallStatsResponse(
    val totalSessions: Int?,
    val totalAttendanceRecords: Int?,
    val totalPresent: Int?,
    val overallAttendancePercentage: Float?
)


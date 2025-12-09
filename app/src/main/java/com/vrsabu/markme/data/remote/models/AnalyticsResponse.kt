package com.vrsabu.markme.data.remote.models

data class AnalyticsResponse(
    val statusCode: Int,
    val data: List<AttendanceRecord>,
    val message: String,
    val success: Boolean
)

data class AttendanceRecord(
    val attendanceId: Long,
    val sessionId: Long,
    val attendanceDate: String,
    val isPresent: Boolean,
    val facultyId: Long,
    val studentId: Long,
    val courseId: Long,
    val createdAt: String,
    val updatedAt: String,
    val session: Session,
    val faculty: FacultyInfo,
    val course: Course,
    val student: StudentDetails
)

data class Session(
    val id: Long,
    val sessionDate: String,
    val room: String,
    val classes: ClassesInfo,
    val mlStatus: String,
    val createdAt: String,
    val updatedAt: String
)

data class ClassesInfo(
    val classes: List<ClassDetail>
)

data class ClassDetail(
    val branch: String,
    val section: String
)

data class FacultyInfo(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val legalName: String,
    val department: String,
    val user: UserInfo
)

data class UserInfo(
    val email: String
)

data class StudentDetails(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val rollNumber: String,
    val branch: String,
    val section: String,
    val subsection: String
)

// Analytics data classes for processing
data class AttendanceAnalytics(
    val totalSessions: Int,
    val totalStudents: Int,
    val averageAttendance: Float,
    val attendanceTrend: List<TrendPoint>,
    val courseWiseStats: List<CourseStats>,
    val recentSessions: List<SessionSummary>
)

data class TrendPoint(
    val date: String,
    val attendanceRate: Float
)

data class CourseStats(
    val courseName: String,
    val totalSessions: Int,
    val averageAttendance: Float,
    val color: Long
)

data class SessionSummary(
    val sessionDate: String,
    val room: String,
    val presentCount: Int,
    val totalCount: Int,
    val courseName: String
)


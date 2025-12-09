package com.vrsabu.markme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.models.AttendanceAnalytics
import com.vrsabu.markme.data.remote.models.AttendanceRecord
import com.vrsabu.markme.data.remote.models.Course
import com.vrsabu.markme.data.remote.models.CourseStats
import com.vrsabu.markme.data.remote.models.SessionSummary
import com.vrsabu.markme.data.remote.models.TrendPoint
import com.vrsabu.markme.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _mySubjectsResponse = MutableStateFlow<MutableList<Course>>(mutableListOf())
    val mySubjectsResponse : StateFlow<MutableList<Course>> = _mySubjectsResponse

    private val _analyticsData = MutableStateFlow<AttendanceAnalytics?>(null)
    val analyticsData: StateFlow<AttendanceAnalytics?> = _analyticsData

    private val _isLoadingAnalytics = MutableStateFlow(false)
    val isLoadingAnalytics: StateFlow<Boolean> = _isLoadingAnalytics

    fun load(){
        viewModelScope.launch(Dispatchers.IO){
            _mySubjectsResponse.value = authRepository.getFacultySubjects().getOrElse { mutableListOf() }
            loadAnalytics()
        }
    }

    private suspend fun loadAnalytics() {
        _isLoadingAnalytics.value = true
        try {
            val result = authRepository.getAnalyticsData()
            result.onSuccess { response ->
                val analytics = processAnalyticsData(response.data)
                _analyticsData.value = analytics
            }.onFailure {
                _analyticsData.value = null
            }
        } catch (e: Exception) {
            _analyticsData.value = null
        } finally {
            _isLoadingAnalytics.value = false
        }
    }

    private fun processAnalyticsData(records: List<AttendanceRecord>): AttendanceAnalytics {
        if (records.isEmpty()) {
            return AttendanceAnalytics(
                totalSessions = 0,
                totalStudents = 0,
                averageAttendance = 0f,
                attendanceTrend = emptyList(),
                courseWiseStats = emptyList(),
                recentSessions = emptyList()
            )
        }

        // Get unique sessions and students
        val uniqueSessions = records.map { it.sessionId }.distinct().size
        val uniqueStudents = records.map { it.studentId }.distinct().size

        // Calculate average attendance
        val presentCount = records.count { it.isPresent }
        val averageAttendance = (presentCount.toFloat() / records.size) * 100

        // Group by date for trend
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val groupedByDate = records.groupBy { record ->
            try {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(record.attendanceDate)
                dateFormat.format(date ?: Date())
            } catch (e: Exception) {
                record.attendanceDate.substring(0, 10)
            }
        }

        val trendPoints = groupedByDate.map { (date, recordsForDate) ->
            val presentInDate = recordsForDate.count { it.isPresent }
            val attendanceRate = (presentInDate.toFloat() / recordsForDate.size) * 100
            TrendPoint(date, attendanceRate)
        }.sortedBy { it.date }.takeLast(7) // Last 7 days

        // Group by course
        val groupedByCourse = records.groupBy { it.course.courseName }
        val courseColors = listOf(0xFF6366F1, 0xFF3B82F6, 0xFFEC4899, 0xFF10B981, 0xFFF59E0B)
        
        val courseStats = groupedByCourse.entries.mapIndexed { index, entry ->
            val courseName = entry.key
            val courseRecords = entry.value
            val courseSessions = courseRecords.map { it.sessionId }.distinct().size
            val coursePresent = courseRecords.count { it.isPresent }
            val courseAvg = (coursePresent.toFloat() / courseRecords.size) * 100
            CourseStats(
                courseName = courseName,
                totalSessions = courseSessions,
                averageAttendance = courseAvg,
                color = courseColors[index % courseColors.size]
            )
        }

        // Recent sessions
        val groupedBySession = records.groupBy { it.sessionId }
        val recentSessions = groupedBySession.map { (_, sessionRecords) ->
            val firstRecord = sessionRecords.first()
            val presentInSession = sessionRecords.count { it.isPresent }
            SessionSummary(
                sessionDate = firstRecord.session.sessionDate,
                room = firstRecord.session.room,
                presentCount = presentInSession,
                totalCount = sessionRecords.size,
                courseName = firstRecord.course.courseName
            )
        }.sortedByDescending { it.sessionDate }.take(5)

        return AttendanceAnalytics(
            totalSessions = uniqueSessions,
            totalStudents = uniqueStudents,
            averageAttendance = averageAttendance,
            attendanceTrend = trendPoints,
            courseWiseStats = courseStats,
            recentSessions = recentSessions
        )
    }
}
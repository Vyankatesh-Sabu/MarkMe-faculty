package com.vrsabu.markme.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.remote.models.Course
import com.vrsabu.markme.data.remote.models.DaySchedule
import com.vrsabu.markme.data.remote.models.ScheduleRequest
import com.vrsabu.markme.data.remote.models.ScheduleResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    private val _schedules = MutableStateFlow<List<ScheduleResponse>>(emptyList())
    val schedules: StateFlow<List<ScheduleResponse>> = _schedules

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _facultyId = MutableStateFlow<Long?>(null)
    val facultyId: StateFlow<Long?> = _facultyId

    init {
        loadCourses()
        loadFacultyProfile()
    }

    private fun loadFacultyProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.FacultyProfile()
                if (response.isSuccessful) {
                    _facultyId.value = response.body()?.data?.faculty?.id
                    loadSchedules()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load profile: ${e.message}"
            }
        }
    }

    private fun loadCourses() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.FacultySubjects()
                if (response.isSuccessful) {
                    _courses.value = response.body()?.data?.map { it.course } ?: emptyList()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load courses: ${e.message}"
            }
        }
    }

    fun loadSchedules(
        facultyId: Long? = null,
        courseId: Long? = null,
        room: String? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.api.getSchedules(
                    facultyId = facultyId,
                    courseId = courseId,
                    room = room
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        _schedules.value = body.data
                    } else {
                        _errorMessage.value = body?.message ?: "Failed to load schedules"
                    }
                } else {
                    _errorMessage.value = "Failed to load schedules: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createSchedule(
        courseId: Long,
        startTime: String,
        endTime: String,
        room: String,
        daySchedule: DaySchedule
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val facultyIdValue = _facultyId.value
                if (facultyIdValue == null) {
                    _errorMessage.value = "Faculty ID not available"
                    return@launch
                }

                val request = ScheduleRequest(
                    facultyId = facultyIdValue,
                    courseId = courseId,
                    startTime = startTime,
                    endTime = endTime,
                    room = room,
                    day = daySchedule
                )

                val response = RetrofitInstance.api.createSchedule(request)
                if (response.isSuccessful) {
                    _successMessage.value = "Schedule created successfully!"
                    loadSchedules() // Reload schedules
                } else {
                    _errorMessage.value = "Failed to create schedule: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}


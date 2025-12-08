package com.vrsabu.markme.ui.screens.attendanceScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.remote.models.AttendanceResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AttendanceUiState {
    object Idle : AttendanceUiState()
    object Loading : AttendanceUiState()
    data class Success(val data: AttendanceResponse) : AttendanceUiState()
    data class Error(val message: String) : AttendanceUiState()
}

class AttendanceViewModel : ViewModel() {

    private val _state = MutableStateFlow<AttendanceUiState>(AttendanceUiState.Idle)
    val state: StateFlow<AttendanceUiState> = _state.asStateFlow()

    fun loadAttendance(courseId: Long) {
        if (courseId <= 0) {
            _state.value = AttendanceUiState.Error("Invalid course id")
            return
        }

        viewModelScope.launch {
            _state.value = AttendanceUiState.Loading
            try {
                val response = RetrofitInstance.api.FacultyAttendance(courseId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        _state.value = AttendanceUiState.Success(body)
                    } else {
                        _state.value = AttendanceUiState.Error(body?.message ?: "Empty response")
                    }
                } else {
                    _state.value = AttendanceUiState.Error("Network error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _state.value = AttendanceUiState.Error(e.message ?: "Unexpected error")
            }
        }
    }
}


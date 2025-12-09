package com.vrsabu.markme.ui.screens.attendanceSelection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.remote.models.TakeAttendanceRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SubmissionState {
    object Idle : SubmissionState()
    object Loading : SubmissionState()
    data class Success(val message: String) : SubmissionState()
    data class Error(val message: String) : SubmissionState()
}

class AttendanceSelectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SubmissionState>(SubmissionState.Idle)
    val uiState: StateFlow<SubmissionState> = _uiState.asStateFlow()

    /**
     * Call the API to take attendance. Updates uiState with Loading/Success/Error.
     */
    fun takeAttendance(takeAttendanceRequest: TakeAttendanceRequest) {
        viewModelScope.launch {
            _uiState.value = SubmissionState.Loading
            try {
                val response = RetrofitInstance.api.TakeAttendance(takeAttendanceRequest)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        _uiState.value = SubmissionState.Success(body.message)
                    } else {
                        _uiState.value = SubmissionState.Error(body?.message ?: "Empty response from server")
                    }
                } else {
                    _uiState.value = SubmissionState.Error("Network error: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = SubmissionState.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun reset() {
        _uiState.value = SubmissionState.Idle
    }
}
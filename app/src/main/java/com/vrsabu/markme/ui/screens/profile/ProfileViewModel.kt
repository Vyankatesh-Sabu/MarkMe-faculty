package com.vrsabu.markme.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.models.Faculty
import com.vrsabu.markme.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Idle : ProfileUiState
    object Loading : ProfileUiState
    data class Success(val faculty: Faculty) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val state: StateFlow<ProfileUiState> = _state

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = ProfileUiState.Loading

            val res = authRepository.fetchFacultyProfile()
            if (res.isSuccess) {
                val faculty = res.getOrNull()
                if (faculty != null) {
                    _state.value = ProfileUiState.Success(faculty)
                } else {
                    _state.value = ProfileUiState.Error("Empty profile")
                }
            } else {
                val err = res.exceptionOrNull()?.message ?: "Unknown error"
                _state.value = ProfileUiState.Error(err)
            }
        }
    }
}

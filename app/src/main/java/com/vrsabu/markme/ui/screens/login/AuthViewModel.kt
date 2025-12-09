package com.vrsabu.markme.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.remote.models.LoginRequest
import com.vrsabu.markme.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<Result<Unit>?>(null)
    val authState: StateFlow<Result<Unit>?> = _authState

    fun isLoggedIn() : Boolean {
        return authRepository.getAccessToken()?.isNotEmpty() ?: false
    }
    fun login(email: String, password: String) {
        _authState.value = null // Reset previous state

        viewModelScope.launch {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = RetrofitInstance.api.Facultylogin(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success) {
                        val accessToken = body.data?.accessToken
                        val refreshToken = body.data?.refreshToken
                        val user = body.data?.user
                        val id = user?.id


                        try {
                            authRepository.saveAuthData(accessToken, refreshToken, user, user?.id, user?.role)
                        } catch (_: Exception) {
                            // Non-fatal
                        }

                        _authState.value = Result.success(Unit)
                    } else {
                        // Body present but indicates failure
                        val message = body?.message ?: "Login failed"
                        _authState.value = Result.failure(Exception(message))
                    }
                } else {
                    // Try to parse error body for a message
                    val errMsg = try {
                        response.errorBody()?.string()?.let { errStr ->
                            try {
                                val json = JSONObject(errStr)
                                json.optString("message", response.message())
                            } catch (_: Exception) {
                                response.message()
                            }
                        } ?: response.message()
                    } catch (_: Exception) {
                        response.message()
                    }

                    _authState.value = Result.failure(Exception(errMsg))
                }

            } catch (_: Exception) {
                _authState.value = Result.failure(Exception("Unexpected error"))
            }
        }
    }

    /**
     * Logout: clear stored auth data and reset auth state so UI observing authState
     * (for example LoginPage) won't automatically navigate back to Home.
     */
    fun logout() {
        try {
            authRepository.saveAuthData(null, null, null, null, null)
        } catch (_: Exception) {
            // ignore
        }
        _authState.value = null
    }
}

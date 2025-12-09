package com.vrsabu.markme.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.remote.models.Course
import com.vrsabu.markme.data.remote.models.Faculty
import com.vrsabu.markme.data.remote.models.User
import org.json.JSONObject

class AuthRepository {

    // Simple in-memory storage for tokens/user as a placeholder.
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var currentUser: User? = null

    fun login(email: String, password: String) {
        // Example: simple validation
        if (email.isBlank() || password.isBlank()) {
            throw Exception("Fields cannot be empty")
        }

        // Example: Replace with API call
        if (email == "test@gmail.com" && password == "123456") {
            return  // Success
        } else {
            throw Exception("Invalid credentials")
        }
    }

    fun saveAuthData(accessToken: String?, refreshToken: String?, user: User?, id : Int?, role : String?) {
        val p = prefs
        if (p != null) {
            val editor = p.edit()
            editor.putString(KEY_ACCESS_TOKEN, accessToken)
            editor.putString(KEY_REFRESH_TOKEN, refreshToken)
            editor.putString(ID, id.toString())
            editor.putString(ROLE, role)
            if (user != null) {
                val userJson = JSONObject().apply {
                    put("id", user.id)
                    put("email", user.email)
                    put("role", user.role)
                    put("status", user.status)
                }
                editor.putString(KEY_USER, userJson.toString())
            } else {
                editor.remove(KEY_USER)
            }

            editor.apply()
        } else {
            // Fallback to in-memory if prefs not initialized
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            this.currentUser = user
        }
    }

    fun getAccessToken(): String? = prefs?.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs?.getString(KEY_REFRESH_TOKEN, null)

    fun getCurrentUser(): User? = prefs?.getString(KEY_USER, null)?.let { str ->
        try {
            val json = JSONObject(str)
            User(
                id = json.optInt("id", -1),
                email = json.optString("email", null),
                role = json.optString("role", null),
                status = json.optString("status", null)
            )
        } catch (e: Exception) {
            null
        }
    } ?: currentUser

    /**
     * Fetch faculty profile from remote API.
     * Returns Result.success(Faculty) on success or Result.failure(Exception) on error.
     */
    suspend fun fetchFacultyProfile(): Result<Faculty> {
        return try {
            val response = RetrofitInstance.api.FacultyProfile()
            if (response.isSuccessful) {
                val body = response.body()
                val faculty = body?.data?.faculty
                if (faculty != null) {
                    Result.success(faculty)
                } else {
                    Result.failure(Exception(body?.message ?: "Empty profile response"))
                }
            } else {
                val err = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFacultySubjects(): Result<MutableList<Course>> {
        return try {
            val courses = mutableListOf<Course>()

            val response = RetrofitInstance.api.FacultySubjects()

            if (response.isSuccessful) {
                val body = response.body()
                val subjects = body?.data

                subjects?.forEach {
                    courses.add(it.course)
                }

                Result.success(courses)
            } else {
                Result.failure(Exception("Error fetching subjects"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnalyticsData(): Result<com.vrsabu.markme.data.remote.models.AnalyticsResponse> {
        return try {
            val response = RetrofitInstance.api.getAnalyticsData()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty analytics response"))
                }
            } else {
                val err = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val PREFS_NAME = "markme_auth"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user"

        private const val ID = "id"

        private const val ROLE = "role"

        private var prefs: SharedPreferences? = null

        /**
         * Initialize SharedPreferences for the repository. Call this from Application.onCreate().
         */
        fun init(context: Context) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        }
    }
}

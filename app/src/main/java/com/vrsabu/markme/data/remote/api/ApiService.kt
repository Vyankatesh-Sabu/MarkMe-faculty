package com.vrsabu.markme.data.remote.api

import com.vrsabu.markme.data.remote.models.AttendanceResponse
import com.vrsabu.markme.data.remote.models.FacultyProfileResponse
import com.vrsabu.markme.data.remote.models.LoginRequest
import com.vrsabu.markme.data.remote.models.LoginResponse
import com.vrsabu.markme.data.remote.models.MySubjectsResponse
import com.vrsabu.markme.data.remote.models.TakeAttendanceReponse
import com.vrsabu.markme.data.remote.models.TakeAttendanceRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/v1/auth/login")
    suspend fun Facultylogin(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/v1/faculty/me")
    suspend fun FacultyProfile() : Response<FacultyProfileResponse>

    @GET("api/v1/faculty/me/course")
    suspend fun FacultySubjects() : Response<MySubjectsResponse>

    // Fetch faculty attendance overview for a given course id
    @GET("api/v1/faculty/attendance")
    suspend fun FacultyAttendance(@Query("courseId") courseId: Long): Response<AttendanceResponse>

    @POST("api/v1/attendancesessions")
    suspend fun TakeAttendance(@Body takeAttendanceReponse: TakeAttendanceRequest) : Response<TakeAttendanceReponse>
}
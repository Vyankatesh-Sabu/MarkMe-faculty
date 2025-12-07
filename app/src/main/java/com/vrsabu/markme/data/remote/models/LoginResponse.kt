package com.vrsabu.markme.data.remote.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val data: LoginData?
)

data class LoginData(
    val user: User?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?
)


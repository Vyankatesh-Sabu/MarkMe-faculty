package com.vrsabu.markme.data.remote.models


data class FacultyProfileResponse(
    val success: Boolean,
    val message: String,
    val data: Data?,
)

data class Data(
    val faculty: Faculty,
)

data class Faculty(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val legalName: String,
    val contactNumber: String,
    val dateOfBirth: String,
    val dateOfJoining: String,
    val department: String,
    val createdAt: String,
    val updatedAt: String,
)

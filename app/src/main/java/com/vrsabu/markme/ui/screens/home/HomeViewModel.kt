package com.vrsabu.markme.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrsabu.markme.data.remote.models.Course
import com.vrsabu.markme.data.remote.models.FacultyProfileResponse
import com.vrsabu.markme.data.remote.models.MySubjectsResponse
import com.vrsabu.markme.data.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.collections.listOf

class HomeViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _mySubjectsResponse = MutableStateFlow<MutableList<Course>>(mutableListOf())
    val mySubjectsResponse : StateFlow<MutableList<Course>> = _mySubjectsResponse

    fun load(){
        viewModelScope.launch(Dispatchers.IO){
            _mySubjectsResponse.value = authRepository.getFacultySubjects().getOrElse { mutableListOf() }
        }
    }
}
package com.vrsabu.markme.ui.screens.attendanceScreen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

/**
 * Displays the list of students for a given session date (ISO yyyy-MM-dd) for a course.
 * Reads data from AttendanceViewModel which is expected to already have loaded attendance data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(courseId: Long, dateIso: String, navController: NavHostController, attendanceViewModel: AttendanceViewModel? = null) {
    // Prefer ViewModel injected from parameter for testability; otherwise obtain one.
    val vm: AttendanceViewModel = attendanceViewModel ?: viewModel()
    val state by vm.state.collectAsState()

    // Try to read students passed from previous back stack entry immediately (so we can render without waiting for ViewModel)
    val passedRaw = navController.previousBackStackEntry?.savedStateHandle?.get<Any>("students") as? ArrayList<*>
    val passedStudents: List<StudentAttendance>? = passedRaw?.mapNotNull { it as? StudentAttendance }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "Students: ${dateIso}") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Box(modifier = Modifier
            .padding(inner)
            .fillMaxSize()) {

            // If students were passed in navigation savedStateHandle, render them immediately
            if (passedStudents != null && passedStudents.isNotEmpty()) {
                LazyColumn(modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(passedStudents) { s ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = listOfNotNull(s.firstName, s.lastName).joinToString(" "), fontWeight = FontWeight.Bold)
                                    Text(text = "Roll: ${s.rollNumber ?: "-"}", color = Color.Gray)
                                }
                                Text(text = if (s.isPresent) "Present" else "Absent",
                                    color = if (s.isPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                when (state) {
                    is AttendanceUiState.Loading, is AttendanceUiState.Idle -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is AttendanceUiState.Error -> {
                        val msg = (state as AttendanceUiState.Error).message
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "Error: $msg")
                        }
                    }
                    is AttendanceUiState.Success -> {
                        // Fallback - read from ViewModel's loaded data (if available)
                        val data = (state as AttendanceUiState.Success).data.data
                        val entry = data?.attendanceByDate?.values?.firstOrNull { it.date?.take(10) == dateIso }

                        val students = entry?.students ?: emptyList()

                        if (students.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = "No student attendance data for $dateIso", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(students) { s ->
                                    val stud = s.student
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(modifier = Modifier
                                            .padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(text = listOfNotNull(stud?.firstName, stud?.lastName).joinToString(" "), fontWeight = FontWeight.Bold)
                                                Text(text = "Roll: ${stud?.rollNumber ?: "-"}", color = Color.Gray)
                                            }
                                            Text(text = if (s.isPresent == true) "Present" else "Absent",
                                                color = if (s.isPresent == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

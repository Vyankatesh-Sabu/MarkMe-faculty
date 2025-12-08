package com.vrsabu.markme.ui.screens.attendanceScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentStatisticsScreen(courseId: Long, navController: NavHostController, viewModel: AttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    // Ensure data is loaded
    LaunchedEffect(courseId) {
        viewModel.loadAttendance(courseId)
    }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Student Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
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
                val data = (state as AttendanceUiState.Success).data.data
                val studentStats = data?.studentStats?.mapNotNull { s ->
                    val stud = s.student
                    val name = listOfNotNull(stud?.firstName, stud?.lastName).joinToString(" ").ifEmpty { "Unknown" }
                    val roll = stud?.rollNumber ?: ""
                    val present = s.presentCount ?: 0
                    val total = s.totalClasses ?: 0
                    val perc = s.attendancePercentage ?: if (total > 0) String.format(java.util.Locale.getDefault(), "%.0f", (present.toDouble() / total * 100)) else "0"
                    StudentStat(name = name, rollNo = roll, present = present, total = total, percentage = perc)
                } ?: emptyList()

                LazyColumn(modifier = Modifier.padding(inner).padding(16.dp)) {
                    items(studentStats) { stat ->
                        StudentStatRow(stat)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

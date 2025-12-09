package com.vrsabu.markme.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.vrsabu.markme.data.remote.models.DaySchedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    navController: NavHostController,
    viewModel: ScheduleViewModel = viewModel()
) {
    val courses by viewModel.courses.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val facultyId by viewModel.facultyId.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var filterCourseId by remember { mutableStateOf<Long?>(null) }
    var filterRoom by remember { mutableStateOf("") }

    // Show snackbar for messages
    LaunchedEffect(errorMessage, successMessage) {
        if (errorMessage != null || successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Attendance Schedule",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAFBFD)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Schedule")
            }
        },
        containerColor = Color(0xFFFAFBFD)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Filter Section
                FilterSection(
                    courses = courses,
                    showFilters = showFilters,
                    filterCourseId = filterCourseId,
                    filterRoom = filterRoom,
                    onToggleFilters = { showFilters = !showFilters },
                    onCourseFilterChange = { filterCourseId = it },
                    onRoomFilterChange = { filterRoom = it },
                    onApplyFilters = {
                        viewModel.loadSchedules(
                            facultyId = null,
                            courseId = filterCourseId,
                            room = filterRoom.ifBlank { null }
                        )
                    },
                    onClearFilters = {
                        filterCourseId = null
                        filterRoom = ""
                        viewModel.loadSchedules()
                    }
                )

                Spacer(Modifier.height(16.dp))

                if (isLoading && schedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (schedules.isEmpty()) {
                    EmptyScheduleState()
                } else {
                    schedules.forEach { schedule ->
                        ScheduleCard(schedule)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            // Messages
            errorMessage?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(it, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            successMessage?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFF10B981)
                ) {
                    Text(it, color = Color.White)
                }
            }
        }

        if (showCreateDialog) {
            CreateScheduleDialog(
                courses = courses,
                onDismiss = { showCreateDialog = false },
                onConfirm = { courseId, startTime, endTime, room, daySchedule ->
                    viewModel.createSchedule(courseId, startTime, endTime, room, daySchedule)
                    showCreateDialog = false
                }
            )
        }
    }
}

@Composable
fun EmptyScheduleState() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(48.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📅", fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Schedules Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Create your first attendance schedule",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ScheduleCard(schedule: com.vrsabu.markme.data.remote.models.ScheduleResponse) {
    val daysActive = listOfNotNull(
        if (schedule.day.monday) "Mon" else null,
        if (schedule.day.tuesday) "Tue" else null,
        if (schedule.day.wednesday) "Wed" else null,
        if (schedule.day.thursday) "Thu" else null,
        if (schedule.day.friday) "Fri" else null,
        if (schedule.day.saturday) "Sat" else null,
        if (schedule.day.sunday) "Sun" else null
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.courseName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🕐 ",
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${schedule.startTime} - ${schedule.endTime}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📍 ",
                            fontSize = 16.sp
                        )
                        Text(
                            text = schedule.room,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Days chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                daysActive.forEach { day ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    courses: List<com.vrsabu.markme.data.remote.models.Course>,
    showFilters: Boolean,
    filterCourseId: Long?,
    filterRoom: String,
    onToggleFilters: () -> Unit,
    onCourseFilterChange: (Long?) -> Unit,
    onRoomFilterChange: (String) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit
) {
    var expandedCourse by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleFilters() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍 Filter Schedules",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (showFilters) "▲" else "▼",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showFilters) {
                Spacer(Modifier.height(16.dp))

                // Course Filter
                Text(
                    text = "Filter by Course",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCourse,
                    onExpandedChange = { expandedCourse = it }
                ) {
                    OutlinedTextField(
                        value = courses.find { it.id == filterCourseId }?.courseName ?: "All Courses",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCourse,
                        onDismissRequest = { expandedCourse = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Courses") },
                            onClick = {
                                onCourseFilterChange(null)
                                expandedCourse = false
                            }
                        )
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.courseName) },
                                onClick = {
                                    onCourseFilterChange(course.id)
                                    expandedCourse = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Room Filter
                Text(
                    text = "Filter by Room",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = filterRoom,
                    onValueChange = onRoomFilterChange,
                    placeholder = { Text("Enter room number") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearFilters,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Clear")
                    }

                    Button(
                        onClick = onApplyFilters,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleDialog(
    courses: List<com.vrsabu.markme.data.remote.models.Course>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String, DaySchedule) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf<Long?>(null) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var room by remember { mutableStateOf("") }
    var expandedCourse by remember { mutableStateOf(false) }

    var monday by remember { mutableStateOf(false) }
    var tuesday by remember { mutableStateOf(false) }
    var wednesday by remember { mutableStateOf(false) }
    var thursday by remember { mutableStateOf(false) }
    var friday by remember { mutableStateOf(false) }
    var saturday by remember { mutableStateOf(false) }
    var sunday by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Schedule",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Course Selection
                Text(
                    text = "Select Course",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCourse,
                    onExpandedChange = { expandedCourse = it }
                ) {
                    OutlinedTextField(
                        value = courses.find { it.id == selectedCourseId }?.courseName ?: "Select Course",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCourse,
                        onDismissRequest = { expandedCourse = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.courseName) },
                                onClick = {
                                    selectedCourseId = course.id
                                    expandedCourse = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Time Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Time",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            placeholder = { Text("09:00") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "End Time",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            placeholder = { Text("10:30") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Room
                Text(
                    text = "Room",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    placeholder = { Text("Room 101") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                // Days Selection
                Text(
                    text = "Select Days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayCheckbox("Monday", monday) { monday = it }
                    DayCheckbox("Tuesday", tuesday) { tuesday = it }
                    DayCheckbox("Wednesday", wednesday) { wednesday = it }
                    DayCheckbox("Thursday", thursday) { thursday = it }
                    DayCheckbox("Friday", friday) { friday = it }
                    DayCheckbox("Saturday", saturday) { saturday = it }
                    DayCheckbox("Sunday", sunday) { sunday = it }
                }

                Spacer(Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            selectedCourseId?.let { courseId ->
                                val daySchedule = DaySchedule(
                                    monday = monday,
                                    tuesday = tuesday,
                                    wednesday = wednesday,
                                    thursday = thursday,
                                    friday = friday,
                                    saturday = saturday,
                                    sunday = sunday
                                )
                                onConfirm(courseId, startTime, endTime, room, daySchedule)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedCourseId != null && room.isNotBlank() &&
                                (monday || tuesday || wednesday || thursday || friday || saturday || sunday),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun DayCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (checked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}


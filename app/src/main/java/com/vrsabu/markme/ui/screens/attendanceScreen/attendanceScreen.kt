package com.vrsabu.markme.ui.screens.attendanceScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.vrsabu.markme.ui.components.PercentageDateLineChart
import java.io.Serializable
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import kotlin.math.roundToInt

// ---------------------------------
// MODELS (Minimal for UI Display)
// ---------------------------------

data class Course(val courseName: String, val description: String)
// Added facultyId (optional) to SessionInfo so we can filter by faculty
data class SessionInfo(val date: String, val room: String, val mlStatus: String, val presentCount: Int, val totalStudents: Int, val facultyId: String? = null)
// Lightweight serializable student attendance DTO used to pass data via Nav savedStateHandle
data class StudentAttendance(val firstName: String?, val lastName: String?, val rollNumber: String?, val isPresent: Boolean) : Serializable
data class StudentStat(val name: String, val rollNo: String, val present: Int, val total: Int, val percentage: String)
data class OverallStats(val totalSessions: Int, val totalPresent: Int, val overallAttendancePercentage: Any)

enum class DateStatus { PROCESSED, PENDING, UNKNOWN }

// ---------------------------------
// MAIN SCREEN - connector to ViewModel
// ---------------------------------

@Composable
fun AttendanceScreen(courseId: Long, navController: NavHostController) {
    val viewModel: AttendanceViewModel = viewModel()

    // Trigger load when composed
    LaunchedEffect(courseId) {
        viewModel.loadAttendance(courseId)
    }

    val state by viewModel.state.collectAsState()

    when (state) {
        is AttendanceUiState.Idle, is AttendanceUiState.Loading -> {
            // simple loading UI
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

            // Map remote models to UI models with safe defaults
            val uiCourse = data?.course?.let { Course(it.courseName, it.description) }
                ?: Course(courseName = "Unknown Course", description = "")

            // Read faculty id from SharedPreferences. Assumptions:
            // - preferences file: "markme_prefs"
            // - primary key: "faculty_id" (fallback to "facultyId")
            // If not present, we show all data (no filtering).
            val context = LocalContext.current
            val facultyIdPref = remember(context) {
                val prefs = context.getSharedPreferences("markme_prefs", Context.MODE_PRIVATE)
                // Prefer long stored as string or string id
                val f1 = prefs.getString("faculty_id", null)
                val f2 = prefs.getString("facultyId", null)
                (f1 ?: f2 ?: "").trim()
            }

            // full list of sessions from API; include optional facultyId when available
            val allSessions = data?.attendanceByDate?.values?.toList()?.mapNotNull { entry ->
                val date = entry.date ?: entry.session?.sessionDate ?: ""
                val room = entry.session?.room ?: ""
                val ml = entry.session?.mlStatus ?: ""
                val students = entry.students ?: emptyList()
                val presentCount = students.count { studentAtt -> studentAtt.isPresent == true }
                val total = students.size
                // Safely extract faculty id from the entry.faculty (Any?) if present. Fallback to top-level data.faculty id.
                val facultyIdFromEntry = when (val f = entry.faculty) {
                    is Number -> f.toString()
                    is String -> f
                    is Map<*, *> -> {
                        (f["id"] ?: f["_id"] ?: f["Id"])?.toString()
                    }
                    else -> null
                } ?: data?.faculty?.id?.toString()
                SessionInfo(date = date, room = room, mlStatus = ml, presentCount = presentCount, totalStudents = total, facultyId = facultyIdFromEntry)
            }?.sortedByDescending { it.date } ?: emptyList()

            // Filter sessions by facultyIdPref if provided, otherwise show all
            val filteredSessions = remember(allSessions, facultyIdPref) {
                if (facultyIdPref.isBlank()) allSessions
                else allSessions.filter { it.facultyId?.trim()?.let { fid -> fid == facultyIdPref } ?: false }
            }

            // derive studentStats/UI overall same as before (we keep studentStats unchanged if API provides aggregate stats only)
            val studentStats = data?.studentStats?.toList()?.mapNotNull { s ->
                val stud = s.student
                val name = listOfNotNull(stud?.firstName, stud?.lastName).joinToString(" ").ifEmpty { "Unknown" }
                val roll = stud?.rollNumber ?: ""
                // API uses Long for counts, convert safely to Int for the UI model
                val presentLong = s.presentCount ?: 0L
                val totalLong = s.totalClasses ?: 0L
                val present = presentLong.toInt()
                val total = totalLong.toInt()
                val perc = s.attendancePercentage ?: if (totalLong > 0L) String.format(Locale.getDefault(), "%.0f", (presentLong.toDouble() / totalLong.toDouble() * 100)) else "0"
                StudentStat(name = name, rollNo = roll, present = present, total = total, percentage = perc)
            } ?: emptyList()

            // Recompute overall stats based on filteredSessions so analytics reflect the faculty selection
            val totalSessionsFiltered = filteredSessions.size
            val totalPresentFiltered = filteredSessions.sumOf { it.presentCount }
            val totalStudentsAcrossSessions = filteredSessions.sumOf { it.totalStudents }
            val overallPercentFiltered = if (totalStudentsAcrossSessions > 0) {
                ((totalPresentFiltered.toDouble() / totalStudentsAcrossSessions.toDouble()) * 100).roundToInt()
            } else 0

            val uiOverall = OverallStats(
                totalSessions = totalSessionsFiltered,
                totalPresent = totalPresentFiltered,
                overallAttendancePercentage = overallPercentFiltered
            )

            // Build per-student percentage list (present / total) to pass into charts if needed
            val studentPercentages: List<Float> = studentStats.map { s ->
                if (s.total > 0) (s.present.toFloat() / s.total.toFloat() * 100f) else 0f
            }

            // --- Date picker state derived from filteredSessions ---
            // Use only dates returned by the API for the selected faculty. Do NOT inject today's date or other client-side fake values.
            val isoDates = filteredSessions.map { it.date.take(10) }.distinct().sortedDescending()
            val allDates = isoDates

            // If the API returned no dates, use an empty selection.
            var selectedDateIso by remember { mutableStateOf(isoDates.firstOrNull() ?: "") }

            // Prepare a map of statuses for quick lookup (based on filteredSessions)
            val dateStatusMap = remember(filteredSessions) {
                allDates.associateWith { iso ->
                    if (isoDates.contains(iso)) {
                        val s = filteredSessions.firstOrNull { it.date.take(10) == iso }
                        if (s != null && (s.mlStatus.equals("processed", true) || s.presentCount > 0)) DateStatus.PROCESSED else DateStatus.PENDING
                    } else DateStatus.UNKNOWN
                }
            }

            // Map for quick present count badge on chips (based on filteredSessions)
            val dateCountMap = remember(filteredSessions) {
                allDates.associateWith { iso ->
                    filteredSessions.firstOrNull { it.date.take(10) == iso }?.presentCount ?: 0
                }
            }

            // Build a map of dateIso -> list of StudentAttendance (serializable) so we can pass students when navigating
            val dateStudentsMap: Map<String, List<StudentAttendance>> = remember(filteredSessions) {
                data?.attendanceByDate?.values?.mapNotNull { entry ->
                    val iso = entry.date?.take(10) ?: entry.session?.sessionDate?.take(10) ?: return@mapNotNull null
                    // Safely extract faculty id (entry.faculty may be a primitive, string or map)
                    val facultyIdFromEntry = when (val f = entry.faculty) {
                        is Number -> f.toString()
                        is String -> f
                        is Map<*, *> -> {
                            (f["id"] ?: f["_id"] ?: f["Id"])?.toString()
                        }
                        else -> null
                    } ?: data?.faculty?.id?.toString()
                    // if faculty filter is set, skip entries that don't match
                    if (facultyIdPref.isNotBlank() && (facultyIdFromEntry?.trim() ?: "") != facultyIdPref) return@mapNotNull null

                    val studs = entry.students?.map { s ->
                        StudentAttendance(
                            firstName = s.student?.firstName,
                            lastName = s.student?.lastName,
                            rollNumber = s.student?.rollNumber,
                            isPresent = s.isPresent == true
                        )
                    } ?: emptyList()
                    iso to studs
                }?.toMap() ?: emptyMap()
            }

            // Render a single overview that includes top app bar, actions, date picker and filtered sessions
            AttendanceOverviewScreen(
                navController = navController,
                course = uiCourse,
                courseId = courseId,
                overallStats = uiOverall,
                sessions = filteredSessions,
                allDates = allDates,
                selectedDateIso = selectedDateIso,
                dateStatusMap = dateStatusMap,
                dateCountMap = dateCountMap,
                dateStudentsMap = dateStudentsMap,
                studentPercentages = studentPercentages,
                onDateSelected = { selectedDateIso = it },
                onViewStudents = {
                    // navigate to student statistics screen for this course
                    navController.navigate("attendance/${courseId}/students")
                },
                onTakeAttendance = {
                    // navigate to attendance taking screen
                    navController.navigate("attendance/${courseId}/take")
                }
            )
        }
    }
}

// ---------------------------------
// existing UI composables
// ---------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceOverviewScreen(
    navController: NavHostController,
    course: Course,
    courseId: Long,
    overallStats: OverallStats,
    sessions: List<SessionInfo>,
    allDates: List<String>,
    selectedDateIso: String,
    dateStatusMap: Map<String, DateStatus>,
    dateCountMap: Map<String, Int>,
    dateStudentsMap: Map<String, List<StudentAttendance>>,
    // NEW: optional arrays derived from student stats so callers can plot student present/total
    studentPercentages: List<Float> = emptyList(),
    onDateSelected: (String) -> Unit,
    onViewStudents: () -> Unit,
    onTakeAttendance: () -> Unit // new callback for taking attendance
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(course.courseName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->

        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            // Show a compact header (course subtitle) and overall stats below the top bar
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    Text(text = course.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(6.dp))
                    OverallStatsCard(overallStats)
                    Spacer(modifier = Modifier.height(8.dp))

                    // percentage by date line chart (new)
                    // If `studentPercentages` is provided we prefer that as the data source for the chart
                    // Build simple epoch-based x values (strings) so the chart's date formatter can render labels.
                    // We create one x-value per student percentage to ensure the lists match size.
                    val studentChartDates = remember(studentPercentages) {
                        val today = System.currentTimeMillis()
                        val dayMs = 24L * 60L * 60L * 1000L
                        studentPercentages.indices.map { i -> (today + i * dayMs).toString() }
                    }

                    PercentageDateLineChart(dates = studentChartDates, percentages = studentPercentages, modifier = Modifier.fillMaxWidth())

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Row with actions
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onTakeAttendance,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Take Attendance")
                    }

                    OutlinedButton(
                        onClick = onViewStudents,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View Student Statistics")
                    }
                }
            }

            // Date picker row integrated in the same scroll list
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select Date:", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(allDates) { iso ->
                            val dateDisplay = try {
                                LocalDate.parse(iso).format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                            } catch (_: Exception) { iso }

                            val status = dateStatusMap[iso] ?: DateStatus.UNKNOWN

                            val (bgColor, contentColor) = when (status) {
                                DateStatus.PROCESSED -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                                DateStatus.PENDING -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onError)
                                DateStatus.UNKNOWN -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Chip with subtle border when not selected and filled when selected (slimmer)
                            Card(
                                modifier = Modifier
                                    .height(36.dp)
                                    .padding(end = 6.dp)
                                    .clickable { onDateSelected(iso) },
                                colors = CardDefaults.cardColors(containerColor = if (selectedDateIso == iso) bgColor else bgColor.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp).fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                                    // status dot
                                    Box(modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (status == DateStatus.PROCESSED) MaterialTheme.colorScheme.tertiary else if (status == DateStatus.PENDING) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dateDisplay,
                                        fontSize = 13.sp,
                                        color = if (selectedDateIso == iso) contentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // present count badge
                                    val cnt = dateCountMap[iso] ?: 0
                                    if (cnt > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (selectedDateIso == iso) contentColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                            tonalElevation = 0.dp
                                        ) {
                                            Text(
                                                text = cnt.toString(),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selectedDateIso == iso) contentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Text(
                    "Attendance by Date",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // show only sessions matching selected date
            if (selectedDateIso.isEmpty()) {
                // API returned no dates — show a clear message
                item {
                    Text(
                        "No session dates returned by the API for this course.",
                        color = Color.Gray
                    )
                }
            } else {
                val filteredSessions = sessions.filter { it.date.take(10) == selectedDateIso }
                if (filteredSessions.isEmpty()) {
                    item {
                        Text(
                            "No attendance recorded for ${try { LocalDate.parse(selectedDateIso).format(DateTimeFormatter.ofPattern("dd MMM yyyy")) } catch (_: Exception) { selectedDateIso }}",
                            color = Color.Gray
                        )
                    }
                } else {
                    items(filteredSessions) { session ->
                        val sessIso = session.date.take(10)
                        val studentsForSession = dateStudentsMap[sessIso] ?: emptyList()
                        SessionCard(session, navController = navController, courseId = courseId, students = studentsForSession)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// ---------------------------------
// OVERALL STATS CARD
// ---------------------------------

@Composable
fun OverallStatsCard(stats: OverallStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            "Overall Attendance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            StatColumn("Sessions", stats.totalSessions.toString())
            StatColumn("Present", stats.totalPresent.toString())
            StatColumn("Overall %", "${stats.overallAttendancePercentage}%")
        }
    }
}

@Composable
fun StatColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 14.sp, color = Color.Gray)
    }
}

// ---------------------------------
// SESSION CARD
// ---------------------------------

@Composable
fun SessionCard(session: SessionInfo, navController: NavHostController, courseId: Long, students: List<StudentAttendance>) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // status indicator
            val statusColor = when {
                session.mlStatus.equals("processed", true) || session.presentCount > 0 -> MaterialTheme.colorScheme.primary
                session.presentCount == 0 -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Box(modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(statusColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Show date and a clear status label instead of time
                val displayDate = try {
                    val iso = session.date
                    val local = LocalDate.parse(iso.take(10))
                    local.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                } catch (_: Exception) { session.date.take(10) }

                // Derive a user-friendly status: Prefer explicit mlStatus when available, else infer from presentCount
                val statusLabel = when {
                    session.mlStatus.equals("processed", true) -> "Processed"
                    session.mlStatus.equals("failed", true) -> "Failed"
                    session.presentCount > 0 -> "Processed"
                    else -> "Pending"
                }

                // Colors for the status label
                val statusColors = when (statusLabel) {
                    "Processed" -> Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
                    "Failed" -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onError)
                    else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Text(text = displayDate, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusColors.first
                    ) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            color = statusColors.second,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // secondary info: room and mlStatus (if present)
                    Column {
                        Text("Room: ${session.room}", color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (session.mlStatus.isNotBlank()) {
                            Text("ML: ${session.mlStatus}", color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${session.presentCount}/${session.totalStudents}", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                // Only show Details button for processed sessions
                if (session.mlStatus.equals("processed", true)) {
                    OutlinedButton(onClick = {
                        // put the students into the previous backstack's savedStateHandle so destination can read them
                        try {
                            val list = ArrayList(students)
                            navController.currentBackStackEntry?.savedStateHandle?.set("students", list)
                        } catch (_: Exception) {
                        }
                        val dateIso = session.date.take(10)
                        navController.navigate("attendance/session/${courseId}/${dateIso}")
                    }, shape = RoundedCornerShape(10.dp)) {
                        Text("Details")
                    }
                }
             }
        }
    }
}

// ---------------------------------
// STUDENT STAT ROW
// ---------------------------------

@Composable
fun StudentStatRow(stat: StudentStat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column {
            Text(stat.name, fontWeight = FontWeight.Bold)
            Text("Roll: ${stat.rollNo}", color = Color.Gray)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${stat.percentage}%", fontWeight = FontWeight.Bold)
            Text("${stat.present}/${stat.total}", color = Color.Gray)
        }
    }
}

package com.vrsabu.markme.ui.screens.attendanceScreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlin.math.roundToInt

// ---------- Muted palette ----------
private val SoftGreen = Color(0xFF6BAF8A)
private val SoftYellow = Color(0xFFD9B65D)
private val SoftRed = Color(0xFFD67A72)
private val SoftTrack = Color(0xFFE7E7E7)
private val GlassTint = Color(0x18FFFFFF)

// ---------- Sort options ----------
private enum class SortOption { PERCENT_DESC, PERCENT_ASC, NAME_ASC }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StudentStatisticsScreen(
    courseId: Long,
    navController: NavHostController,
    viewModel: AttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    LaunchedEffect(courseId) { viewModel.loadAttendance(courseId) }
    val state by viewModel.state.collectAsState()

    // UI states
    var query by remember { mutableStateOf("") }
    var sortOpt by remember { mutableStateOf(SortOption.PERCENT_DESC) }
    val listState = rememberLazyListState()
    var showStats by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Student Statistics") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showStats = !showStats }) {
                        Text(if (showStats) "Hide" else "Show", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { paddingValues ->

        when (state) {
            is AttendanceUiState.Loading, is AttendanceUiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is AttendanceUiState.Error -> {
                val msg = (state as AttendanceUiState.Error).message
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Error: $msg")
                }
            }

            is AttendanceUiState.Success -> {
                val raw = (state as AttendanceUiState.Success).data.data

                val studentStats = remember(raw) {
                    raw?.studentStats?.mapNotNull { s ->
                        val stud = s.student
                        val name = listOfNotNull(
                            stud?.firstName,
                            stud?.lastName
                        ).joinToString(" ").ifEmpty { "Unknown" }

                        val present = s.presentCount?.toInt() ?: 0
                        val total = s.totalClasses?.toInt() ?: 0
                        val perc = s.attendancePercentage
                            ?: if (total > 0) ((present.toDouble() / total) * 100).roundToInt().toString()
                            else "0"

                        StudentStat(
                            name = name,
                            rollNo = stud?.rollNumber ?: "",
                            present = present,
                            total = total,
                            percentage = perc
                        )
                    } ?: emptyList()
                }

                // Apply search + sort
                val filtered = remember(studentStats, query, sortOpt) {
                    studentStats
                        .filter {
                            val q = query.trim().lowercase()
                            if (q.isEmpty()) true
                            else it.name.lowercase().contains(q) || it.rollNo.lowercase().contains(q)
                        }
                        .let { list ->
                            when (sortOpt) {
                                SortOption.PERCENT_DESC ->
                                    list.sortedByDescending { it.percentage.trim().removeSuffix("%").toDoubleOrNull() ?: 0.0 }

                                SortOption.PERCENT_ASC ->
                                    list.sortedBy { it.percentage.trim().removeSuffix("%").toDoubleOrNull() ?: 0.0 }

                                SortOption.NAME_ASC ->
                                    list.sortedBy { it.name.lowercase() }
                            }
                        }
                }

                val avg = filtered.map {
                    it.percentage.trim().removeSuffix("%").toDoubleOrNull() ?: 0.0
                }.average()

                val max = filtered.maxOfOrNull {
                    it.percentage.trim().removeSuffix("%").toDoubleOrNull() ?: 0.0
                } ?: 0.0

                val min = filtered.minOfOrNull {
                    it.percentage.trim().removeSuffix("%").toDoubleOrNull() ?: 0.0
                } ?: 0.0

                val grouped = remember(filtered) {
                    filtered.groupBy {
                        it.name.firstOrNull()?.uppercaseChar() ?: '#'
                    }.toSortedMap(compareBy { it })
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    SearchAndSortRow(
                        query = query,
                        onQueryChanged = { query = it },
                        sortOpt = sortOpt,
                        onSortChange = { sortOpt = it }
                    )

                    Spacer(Modifier.height(12.dp))

                    AnimatedVisibility(
                        visible = showStats,
                        enter = fadeIn(animationSpec = tween(durationMillis = 220)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 180))
                    ) {
                        SummaryGlassCard(
                            avg = avg,
                            total = filtered.size,
                            max = max,
                            min = min
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Sticky headers
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        grouped.forEach { (char, list) ->
                            stickyHeader {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(char.toString(), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            items(list) { stat ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 260)),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 200))
                                ) {
                                    StudentStatCardAdvanced(stat)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- UI Components ----------

@Composable
private fun SearchAndSortRow(
    query: String,
    onQueryChanged: (String) -> Unit,
    sortOpt: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            placeholder = { Text("Search by name or roll") },
            modifier = Modifier.weight(1f).height(52.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
        )

        Spacer(Modifier.width(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onSortChange(SortOption.PERCENT_DESC) }) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = "Highest",
                    tint = if (sortOpt == SortOption.PERCENT_DESC)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onSortChange(SortOption.PERCENT_ASC) }) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Lowest",
                    tint = if (sortOpt == SortOption.PERCENT_ASC)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onSortChange(SortOption.NAME_ASC) }) {
                Icon(
                    Icons.Default.SortByAlpha,
                    contentDescription = "A-Z",
                    tint = if (sortOpt == SortOption.NAME_ASC)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SummaryGlassCard(avg: Double, total: Int, max: Double, min: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(6.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(GlassTint, Color.Transparent),
                        start = Offset.Zero,
                        end = Offset(300f, 300f)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Summary", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(Modifier.height(6.dp))
                    Text("Average Attendance", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${avg.roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Students", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "$total",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Top / Low", style = MaterialTheme.typography.bodySmall)
                    Text("${max.roundToInt()}% / ${min.roundToInt()}%", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StudentStatCardAdvanced(stat: StudentStat) {
    val perc = stat.percentage.trim().removeSuffix("%").toDoubleOrNull()?.roundToInt() ?: 0
    val color = when {
        perc >= 75 -> SoftGreen
        perc >= 50 -> SoftYellow
        else -> SoftRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarInitials(name = stat.name)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(stat.name, fontWeight = FontWeight.SemiBold)
                if (stat.rollNo.isNotEmpty()) {
                    Text(
                        "Roll: ${stat.rollNo}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AttendanceBadge(perc = perc, color = color)
                Spacer(Modifier.height(8.dp))
                AttendanceProgressBarAnimated(value = perc, barColor = color)
            }
        }
    }
}

@Composable
private fun AvatarInitials(name: String) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Text(initial, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun AttendanceBadge(perc: Int, color: Color) {
    val label = when {
        perc >= 90 -> "Excellent"
        perc >= 75 -> "Good"
        perc >= 50 -> "Warning"
        else -> "At Risk"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = Modifier.heightIn(min = 28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun AttendanceProgressBarAnimated(value: Int, barColor: Color) {
    val animated = animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = tween(durationMillis = 800)
    )

    LinearProgressIndicator(
        progress = animated.value,
        modifier = Modifier
            .width(110.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(50)),
        color = barColor,
        trackColor = SoftTrack
    )
}

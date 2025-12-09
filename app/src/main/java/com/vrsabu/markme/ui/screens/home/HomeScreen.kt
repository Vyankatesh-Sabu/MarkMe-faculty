package com.vrsabu.markme.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.vrsabu.markme.data.remote.models.AttendanceAnalytics
import com.vrsabu.markme.data.remote.models.Course
import com.vrsabu.markme.navigation.Screen
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkMeHomeScreen(navController: NavHostController, homeViewModel: HomeViewModel) {

    LaunchedEffect(Unit) { homeViewModel.load() }

    val subjects by homeViewModel.mySubjectsResponse.collectAsState()
    val analyticsData by homeViewModel.analyticsData.collectAsState()
    val isLoadingAnalytics by homeViewModel.isLoadingAnalytics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MarkMe",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(42.dp)
                            .shadow(4.dp, CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                                    )
                                ),
                                CircleShape
                            )
                            .clickable { navController.navigate(Screen.Profile.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAFBFD)
                )
            )
        },
        containerColor = Color(0xFFFAFBFD)
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {

            GreetingSection()

            // Quick Actions Card
            QuickActionsCard(navController)

            // Analytics Section
            if (isLoadingAnalytics) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (analyticsData != null) {
                AnalyticsSection(analyticsData!!)
            }

            SubjectsSection(
                subjects = subjects,
                navController = navController
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun GreetingSection() {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        else -> "Good Evening"
    }
    val emoji = when {
        currentHour < 12 -> "☀️"
        currentHour < 17 -> "👋"
        else -> "🌙"
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 20.dp)
    ) {
        Text(
            text = "$greeting $emoji",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = "Ready to make teaching smarter today?",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun QuickActionsCard(navController: NavHostController) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Quick Actions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Schedule Management Button
                ActionButton(
                    icon = "📅",
                    title = "Schedule",
                    subtitle = "Manage Classes",
                    color = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.Schedule.route) }
                )

                // Placeholder for future actions
                ActionButton(
                    icon = "📊",
                    title = "Reports",
                    subtitle = "View Analytics",
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f),
                    onClick = { /* Future feature */ }
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: String,
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SubjectsSection(subjects: List<Course>, navController: NavHostController) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "My Subjects",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-0.3).sp
                )
                if (subjects.isNotEmpty()) {
                    Text(
                        text = "${subjects.size} ${if (subjects.size == 1) "course" else "courses"}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedVisibility(
            visible = subjects.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyStateCard()
        }

        subjects.forEach { course ->
            CourseCard(
                courseName = course.courseName,
                credits = course.credits,
                description = course.description,
                onClick = {
                    navController.navigate(Screen.Attendance(course.id).route)
                }
            )
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📚",
                fontSize = 48.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "No subjects assigned yet",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Your courses will appear here once assigned",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CourseCard(
    courseName: String,
    credits: Long,
    description: String,
    onClick: () -> Unit
) {
    val cardColors = listOf(
        Color(0xFF6366F1) to Color(0xFF8B5CF6),
        Color(0xFF3B82F6) to Color(0xFF06B6D4),
        Color(0xFFEC4899) to Color(0xFFF43F5E),
        Color(0xFF10B981) to Color(0xFF14B8A6),
        Color(0xFFF59E0B) to Color(0xFFEF4444)
    )

    val colorPair = remember(courseName) {
        cardColors[courseName.hashCode().mod(cardColors.size).let { if (it < 0) it + cardColors.size else it }]
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = courseName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 26.sp,
                        letterSpacing = (-0.3).sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(colorPair.first, colorPair.second)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = credits.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "$credits Credits",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = "View Attendance",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                    )
                    Icon(
                        Icons.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun AnalyticsSection(analytics: AttendanceAnalytics) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 20.dp)
    ) {
        Text(
            text = "Analytics Overview",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Stats Cards Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Sessions",
                value = analytics.totalSessions.toString(),
                icon = "📅",
                color = Color(0xFF6366F1),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Students",
                value = analytics.totalStudents.toString(),
                icon = "👥",
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Avg. Attendance",
                value = "${String.format(Locale.getDefault(), "%.1f", analytics.averageAttendance)}%",
                icon = "📊",
                color = Color(0xFFEC4899),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Attendance Trend Chart
        if (analytics.attendanceTrend.isNotEmpty()) {
            AttendanceTrendChart(analytics.attendanceTrend)
            Spacer(Modifier.height(20.dp))
        }

        // Course-wise Stats
        if (analytics.courseWiseStats.isNotEmpty()) {
            CourseStatsSection(analytics.courseWiseStats)
            Spacer(Modifier.height(20.dp))
        }

        // Recent Sessions
        if (analytics.recentSessions.isNotEmpty()) {
            RecentSessionsSection(analytics.recentSessions)
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 20.sp)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun AttendanceTrendChart(trendPoints: List<com.vrsabu.markme.data.remote.models.TrendPoint>) {
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
            Text(
                text = "Attendance Trend",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Last ${trendPoints.size} days",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val modelProducer = remember { CartesianChartModelProducer() }

            LaunchedEffect(trendPoints) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(
                            x = trendPoints.indices.map { it.toDouble() },
                            y = trendPoints.map { it.attendanceRate.toDouble() }
                        )
                    }
                }
            }

            // Chart
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Date labels row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (trendPoints.isNotEmpty()) {
                    Text(
                        text = trendPoints.first().date,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (trendPoints.size > 1) {
                        Text(
                            text = trendPoints[trendPoints.size / 2].date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = trendPoints.last().date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Data points display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendPoints.take(5).forEach { point ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", point.attendanceRate)}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = point.date.take(5),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseStatsSection(courseStats: List<com.vrsabu.markme.data.remote.models.CourseStats>) {
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
            Text(
                text = "Course-wise Statistics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            courseStats.forEach { stat ->
                CourseStatItem(stat)
                if (stat != courseStats.last()) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CourseStatItem(stat: com.vrsabu.markme.data.remote.models.CourseStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(stat.color), CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stat.courseName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${stat.totalSessions} sessions",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${String.format(Locale.getDefault(), "%.1f", stat.averageAttendance)}%",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(stat.color)
        )
    }
}

@Composable
fun RecentSessionsSection(sessions: List<com.vrsabu.markme.data.remote.models.SessionSummary>) {
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
            Text(
                text = "Recent Sessions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            sessions.forEach { session ->
                RecentSessionItem(session)
                if (session != sessions.last()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun RecentSessionItem(session: com.vrsabu.markme.data.remote.models.SessionSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📍",
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.courseName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Room: ${session.room}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val attendanceRate = (session.presentCount.toFloat() / session.totalCount * 100).toInt()
            Text(
                text = "${session.presentCount}/${session.totalCount}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$attendanceRate%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

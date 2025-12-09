package com.vrsabu.markme.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.vrsabu.markme.data.remote.models.Course
import com.vrsabu.markme.navigation.Screen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkMeHomeScreen(navController: NavHostController, homeViewModel: HomeViewModel) {

    // Prevent infinite recompositions
    LaunchedEffect(Unit) {
        homeViewModel.load()
    }

    val subjects by homeViewModel.mySubjectsResponse.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MarkMe",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7A73FF)
                    )
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .background(Color.LightGray.copy(alpha = 0.4f), CircleShape)
                            .clickable { navController.navigate(Screen.Profile.route) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = "")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF7F9FC))
        ) {

            GreetingSection()
            SubjectsSection(subjects = subjects, navController = navController)

            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
fun GreetingSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Good Morning 👋",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1A1C1E)
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Ready to make teaching smarter today?",
            fontSize = 15.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun SubjectsSection(subjects: List<Course>, navController: NavHostController) {
    Column {
        Text(
            text = "My Subjects",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(12.dp))

        if (subjects.isEmpty()) {
            Text(
                text = "No subjects assigned yet.",
                modifier = Modifier.padding(16.dp),
                color = Color.Gray
            )
        } else {
            subjects.forEach { course ->
                CourseCard(
                    courseName = course.courseName,
                    credits = course.credits,
                    description = course.description,
                    onClick = { navController.navigate(Screen.Attendance(course.id).route) }
                )
            }
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
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = courseName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Credits: $credits",
                fontSize = 13.sp,
                color = Color(0xFF7A73FF),
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "View Attendance →",
                    color = Color(0xFF7A73FF),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
fun OverviewItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = value, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(6.dp))

        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}




data class FeatureItem(val title: String, val desc: String)

package com.vrsabu.markme.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Jetpack Compose UI skeleton for the provided MarkMe screen
// Note: Replace icons, colors, and typography with your design system.
// This is a structural template covering the full layout.

//private val Icons.Filled.AccessTime: Any

@Preview
@Composable
fun MarkMeHomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF7F9FC))
            .padding(bottom = 32.dp)
    ) {
        TopHeaderSection()
        GreetingSection()
        SubjectsSection()
        SmartConnectCard()
        TodayOverviewSection()
        FeaturesGrid()
    }
}

@Composable
fun TopHeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Menu, contentDescription = null)

        Text(
            text = "MarkMe",
            fontSize = 32.sp,
            color = Color(0xFF7A73FF),
            fontWeight = FontWeight.Bold
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.LightGray, CircleShape)
        )
    }
}

@Composable
fun GreetingSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Good Morning Faculty!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E)
        )
        Text(
            text = "Ready to make teaching smarter today?",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun SubjectsSection() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "My Subjects",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SubjectChip("Digital Systems", selected = true)
            SubjectChip("OS")
        }

        Spacer(Modifier.height(12.dp))

        SubjectChip("Data Structures")
    }
}

@Composable
fun SubjectChip(title: String, selected: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF0D1B4C) else Color.White)
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else Color.Black
        )
    }
}

@Composable
fun SmartConnectCard() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
//        colors = Color(0xFFFFF7EB)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFF9900), RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("SMART Connect", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Disconnected", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Seamlessly capture attendance and classroom moments via Smart Connect.",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun TodayOverviewSection() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Today's Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//                Icon(Icons.Default.AccessTime, contentDescription = null)
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                OverviewItem("85", "Attendance %", Color(0xFF00A676))
                OverviewItem("32", "Students", Color(0xFF2962FF))
                OverviewItem("High Energy", label = "High Energy", color = Color(0xFFFF8C00))
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

@Composable
fun FeaturesGrid() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val items = listOf(
            FeatureItem("Assignments", "Track and mark assignment submissions."),
            FeatureItem("Analytics", "View and analyze performance data."),
            FeatureItem("Scores", "Manage student scores and grades."),
            FeatureItem("Lecture Plan", "Track syllabus and progress.")
        )

        items.chunked(2).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { item -> FeatureCard(item) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun FeatureCard(item: FeatureItem) {
    Card(
        shape = RoundedCornerShape(20.dp),
//        backgroundColor = Color.White,
//        modifier = Modifier.weight(1f)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp))
            ) {}
            Spacer(Modifier.height(12.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(item.desc, fontSize = 13.sp, color = Color.Gray)
        }
    }
}


data class FeatureItem(val title: String, val desc: String)
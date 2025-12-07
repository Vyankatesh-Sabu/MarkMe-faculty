package com.vrsabu.markme.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Body(modifier = Modifier.padding(paddingValues))
    }
}

@Composable
private fun Body(modifier: Modifier) {

    val legalName = "Dr. Rajesh Kumar"
    val contactNumber = "9876543210"
    val dateOfBirth = "1975-05-15"
    val dateOfJoining = "2010-08-20"
    val firstName = "Rajesh"
    val lastName = "Kumar"

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        // Avatar Section
        InitialAvatar(name = firstName, size = 90.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$firstName $lastName",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Department: CSE",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION HEADER
        Text(
            text = "Personal Information",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProfileInfoCard(label = "Legal Name", value = legalName)
        ProfileInfoCard(label = "Contact Number", value = "+91 $contactNumber")
        ProfileInfoCard(label = "Date of Birth", value = dateOfBirth)
        ProfileInfoCard(label = "Date of Joining", value = dateOfJoining)

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ProfileInfoCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large
            )
            .padding(18.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


@Composable
fun InitialAvatar(name: String, size: Dp = 60.dp) {
    val firstLetter = name.firstOrNull()?.uppercase() ?: "?"

    val randomColor = remember {
        Color(
            red = (90..200).random(),
            green = (90..200).random(),
            blue = (90..200).random()
        )
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(randomColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = firstLetter,
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

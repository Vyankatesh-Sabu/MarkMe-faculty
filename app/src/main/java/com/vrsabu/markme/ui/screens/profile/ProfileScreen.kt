package com.vrsabu.markme.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vrsabu.markme.data.repository.AuthRepository
import java.text.SimpleDateFormat
import java.util.*

private val SoftRed = Color(0xFFD32F2F) // more visible red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: (() -> Unit)? = null) {
    val authRepo = AuthRepository() // replace with app repository if available

    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(authRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    val viewModel: ProfileViewModel = viewModel(factory = factory)
    viewModel.loadProfile()

    val state by viewModel.state.collectAsState()
    var showConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Profile") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---------- Avatar ----------
            val displayName = (state as? ProfileUiState.Success)?.faculty?.firstName ?: "Rajesh"
            AvatarInitials(name = displayName, size = 90.dp)
            Spacer(modifier = Modifier.height(12.dp))

            val fullName = (state as? ProfileUiState.Success)?.faculty?.let { "${it.firstName} ${it.lastName}" } ?: "Rajesh Kumar"
            Text(fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            val department = (state as? ProfileUiState.Success)?.faculty?.department ?: "CSE"
            Text("Department: $department", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Personal Information ----------
            Text("Personal Information", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))

            val faculty = (state as? ProfileUiState.Success)?.faculty
            val infoList = listOf(
                "Legal Name" to (faculty?.legalName ?: "Dr. Rajesh Kumar"),
                "Contact Number" to (faculty?.contactNumber ?: "+91 9876543210"),
                "Date of Birth" to (faculty?.dateOfBirth?.toFriendlyDate() ?: "1975-05-15"),
                "Date of Joining" to (faculty?.dateOfJoining?.toFriendlyDate() ?: "2010-08-20")
            )

            infoList.forEach { (label, value) ->
                ProfileInfoCard(label, value)
            }

            Spacer(modifier = Modifier.height(30.dp))

            // ---------- Logout Button ----------
            Button(
                onClick = { showConfirm = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftRed)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                Spacer(Modifier.width(6.dp))
                Text("Logout", color = Color.White, fontSize = 16.sp)
            }
        }

        // ---------- Logout Confirmation ----------
        if (showConfirm) {
            AlertDialog(
                onDismissRequest = { showConfirm = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(onClick = {
                        authRepo.saveAuthData(null, null, null, null, null)
                        showConfirm = false
                        onLogout?.invoke()
                    }) { Text("Logout") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun ProfileInfoCard(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(2.dp, shape = MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .padding(18.dp)
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AvatarInitials(name: String, size: Dp = 60.dp) {
    val firstLetter = name.firstOrNull()?.uppercase() ?: "?"
    val randomColor = remember { Color((100..180).random(), (100..180).random(), (100..180).random()) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(randomColor),
        contentAlignment = Alignment.Center
    ) {
        Text(firstLetter, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

fun String.toFriendlyDate(): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(this)
        val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        formatter.format(date ?: Date())
    } catch (e: Exception) {
        this.substringBefore("T")
    }
}

package com.vrsabu.markme.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue as getState
import androidx.compose.runtime.collectAsState
import com.vrsabu.markme.data.repository.AuthRepository

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLogout: (() -> Unit)? = null) {
    // Obtain AuthRepository from application
    val context = LocalContext.current
    val app = context.applicationContext as? com.vrsabu.markme.MarkMeApp
    val authRepo = app?.authRepository ?: AuthRepository()

    // Create factory
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
        BodyWithViewModel(modifier = Modifier.padding(paddingValues),
            viewModel = viewModel,
            onLogout = {
                // Default navigation behavior if navToLogin provided
                onLogout?.invoke()
            }
        )
    }
}

@Composable
private fun BodyWithViewModel(modifier: Modifier, viewModel: ProfileViewModel, onLogout: () -> Unit) {

    var showConfirm by rememberSaveable { mutableStateOf(false) }

    // State from view model
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(12.dp))

        // Avatar Section (will show first letter from fetched data if available)
        val displayName = when (state) {
            is ProfileUiState.Success -> (state as ProfileUiState.Success).faculty.firstName
            else -> "Rajesh"
        }

        InitialAvatar(name = displayName, size = 90.dp)

        Spacer(modifier = Modifier.height(12.dp))

        val fullName = when (state) {
            is ProfileUiState.Success -> {
                val f = (state as ProfileUiState.Success).faculty
                "${f.firstName} ${f.lastName}"
            }
            else -> "Rajesh Kumar"
        }

        Text(
            text = fullName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        val department = when (state) {
            is ProfileUiState.Success -> (state as ProfileUiState.Success).faculty.department
            else -> "CSE"
        }

        Text(
            text = "Department: $department",
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

        // Make the ProfileInfoCards clickable: tapping triggers fetchProfile()
        val firstVal = when (state) {
            is ProfileUiState.Success -> (state as ProfileUiState.Success).faculty.legalName
            else -> "Dr. Rajesh Kumar"
        }
        ProfileInfoCard(label = "Legal Name", value = firstVal)

        val contactVal = when (state) {
            is ProfileUiState.Success -> (state as ProfileUiState.Success).faculty.contactNumber
            else -> "+91 9876543210"
        }
        ProfileInfoCard(label = "Contact Number", value = contactVal)

        val dobVal = when (state) {
            is ProfileUiState.Success -> (state as ProfileUiState.Success).faculty.dateOfBirth
            else -> "1975-05-15"
        }
        ProfileInfoCard(label = "Date of Birth", value = dobVal)

        val dojVal = when (state) {
            is ProfileUiState.Success -> (state as ProfileUiState.Success).faculty.dateOfJoining
            else -> "2010-08-20"
        }
        ProfileInfoCard(label = "Date of Joining", value = dojVal)

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button placed below personal information
        Button(
            onClick = {
                // Show confirmation dialog before logout
                showConfirm = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = "Logout",
                tint = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Text(text = "Logout", color = Color.White, modifier = Modifier.padding(start = 8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Confirmation dialog
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text(text = "Confirm Logout") },
            text = { Text(text = "Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    // Perform logout: clear stored auth data via MarkMeApp's repository (if available)
                    try {
                        val app = context.applicationContext as? com.vrsabu.markme.MarkMeApp
                        app?.authRepository?.saveAuthData(null, null, null)
                    } catch (_: Exception) {
                        // ignore
                    }
                    showConfirm = false
                    onLogout()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
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

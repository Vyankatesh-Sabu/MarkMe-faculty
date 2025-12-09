package com.vrsabu.markme.ui.screens.attendanceSelection

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.vrsabu.markme.data.remote.models.TakeAttendanceRequest
import com.vrsabu.markme.data.remote.models.Class as ApiClass
import com.vrsabu.markme.data.repository.AuthRepository
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

// -------------------------------
// DATA CLASS
// -------------------------------
data class ClassItem(
    val branch: String,
    val section: String,
    val isSelected: Boolean = false
)

// -------------------------------
// MAIN SCREEN
// -------------------------------
@Composable
fun AttendanceSelectionScreen(
    navController: NavHostController? = null,
    courseId: Long? = null,
    authRepository: AuthRepository? = null,
    vm: AttendanceSelectionViewModel = viewModel()
) {

    var selectedDate by remember { mutableStateOf(getCurrentFormattedDateTime()) }
    var room by remember { mutableStateOf("") }

    val classItems = remember {
        mutableStateOf(
            listOf(
                ClassItem("CSE", "A"),
                ClassItem("CSE", "B"),
                ClassItem("ECE", "A"),
                ClassItem("ECE", "B"),
                ClassItem("AIML", "A"),
                ClassItem("AI", "B"),
                ClassItem("IOT", "A")
            )
        )
    }

    // read current user id from authRepository when available
    val facultyId: Long? = authRepository?.getCurrentUser()?.id?.toLong()

    // Snackbar host state to show submission messages
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Collect UI state from ViewModel
    val uiState by vm.uiState.collectAsState()

    // react to success/error to show snackbar
    LaunchedEffect(uiState) {
        when (uiState) {
            is SubmissionState.Success -> {
                val msg = (uiState as SubmissionState.Success).message
                snackbarHostState.showSnackbar(msg)
                // navigate back after success if navController provided
                navController?.navigateUp()
            }
            is SubmissionState.Error -> {
                val msg = (uiState as SubmissionState.Error).message
                snackbarHostState.showSnackbar(msg)
            }
            else -> {
                // no-op for Idle/Loading
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {

            // TITLE
            Text(
                text = if (courseId != null) "Session Details - Course ${courseId}" else "Session Details",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // DATE SECTION
            SectionCard(title = "Date & Time") {
                DateSelector(
                    selectedText = selectedDate,
                    onSelect = { selectedDate = it }
                )
            }

            // ROOM SECTION
            SectionCard(title = "Room Number") {
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Enter room e.g. R-204") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // CLASS SELECTION
            SectionCard(title = "Select Class (Branch + Section)") {
                ClassSelector(
                    classItems = classItems.value,
                    onSelectionChanged = { classItems.value = it }
                )
            }

            // SUBMIT BUTTON
            Button(
                onClick = {

                    val selectedClasses = classItems.value.filter { it.isSelected }
                        .map { ApiClass(branch = it.branch, section = it.section) }

                    // build request
                    val request = TakeAttendanceRequest(
                        facultyId = facultyId ?: -1,
                        courseId = courseId ?: -1,
                        classes = selectedClasses,
                        sessionDate = selectedDate
                    )

                    // call viewModel to submit
                    coroutineScope.launch {
                        vm.takeAttendance(request)
                    }

                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (uiState is SubmissionState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting...", fontSize = 16.sp)
                } else {
                    Text("Submit", fontSize = 18.sp)
                }
            }

            // optionally show latest status text below the button
            when (uiState) {
                is SubmissionState.Success -> Text((uiState as SubmissionState.Success).message, color = MaterialTheme.colorScheme.primary)
                is SubmissionState.Error -> Text((uiState as SubmissionState.Error).message, color = MaterialTheme.colorScheme.error)
                else -> {}
            }
        }
    }
}

// -------------------------------
// SECTION CARD (for nice UI)
// -------------------------------
@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(10.dp))

        content()
    }
}

// -------------------------------
// CLASS SELECTOR
// -------------------------------
@Composable
fun ClassSelector(
    classItems: List<ClassItem>,
    onSelectionChanged: (List<ClassItem>) -> Unit
) {
    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(top = 4.dp)
    ) {
        classItems.forEachIndexed { index, item ->

            ElevatedFilterChip(
                selected = item.isSelected,
                onClick = {
                    val updatedList = classItems.mapIndexed { i, data ->
                        if (i == index) data.copy(isSelected = !data.isSelected)
                        else data
                    }
                    onSelectionChanged(updatedList)
                },
                label = {
                    Text("${item.branch}-${item.section}")
                },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

// -------------------------------
// DATE SELECTOR (simple, replace with real picker later)
// -------------------------------
@Composable
fun DateSelector(
    selectedText: String,
    onSelect: (String) -> Unit
) {
    Button(
        onClick = {
            onSelect(getCurrentFormattedDateTime())
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(selectedText)
    }
}

fun getCurrentFormattedDateTime(): String {
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", Locale.getDefault())
    return ZonedDateTime.now().format(formatter)
}



@Preview(showBackground = true)
@Composable
fun AttendanceSelectionPreview() {
    AttendanceSelectionScreen(authRepository = null)
}

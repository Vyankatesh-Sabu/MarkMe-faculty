package com.vrsabu.markme.ui.screens.attendanceSelection

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class ChipItem(
    val label: String,
    var isSelected: Boolean = false
)

@Preview
@Composable
fun AttendanceSelectionScreen() {

    val classItems = remember {
        mutableStateOf(listOf("CSE","ECE","Mech","Chemical","Bio","Civil").map { ChipItem(it) })
    }

    val semesterItems = remember {
        mutableStateOf(listOf("I","II","III","IV","V","VI","VII","VIII").map { ChipItem(it) })
    }

    val subjectItems = remember {
        mutableStateOf(listOf("Maths","DBMS","AI","ML").map { ChipItem(it) })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) {innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            // SINGLE SELECT (ONLY ONE DEPT)
            ChipSection(
                title = "Department",
                items = classItems.value,
                singleSelection = true
            ) { updatedList -> classItems.value = updatedList }

            Spacer(Modifier.padding(12.dp))

            // SINGLE SELECT
            ChipSection(
                title = "Semester",
                items = semesterItems.value,
                singleSelection = true
            ) { updatedList -> semesterItems.value = updatedList }

            Spacer(Modifier.padding(12.dp))

            // MULTIPLE SELECT
            ChipSection(
                title = "Subjects",
                items = subjectItems.value,
                singleSelection = false
            ) { updatedList -> subjectItems.value = updatedList }

            Spacer(Modifier.padding(20.dp))

            SubmitButton(
                classItems.value,
                semesterItems.value,
                subjectItems.value
            )
        }
    }
}


@Composable
fun ChipSection(
    title: String,
    items: List<ChipItem>,
    singleSelection: Boolean = false,
    onSelectionChanged: (List<ChipItem>) -> Unit
) {
    var chipList by remember { mutableStateOf(items) }

    Text(title, fontSize = 20.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onPrimary)

    Row(Modifier.horizontalScroll(rememberScrollState())) {

        chipList.forEachIndexed { index, chip ->

            ElevatedFilterChip(
                selected = chip.isSelected,
                onClick = {
                    chipList = chipList.mapIndexed { i, item ->
                        when {
                            singleSelection && i != index -> item.copy(isSelected = false)
                            i == index -> item.copy(isSelected = !item.isSelected)
                            else -> item
                        }
                    }

                    onSelectionChanged(chipList)
                },
                label = { Text(chip.label) },
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun SubmitButton(
    classList: List<ChipItem>,
    semesterList: List<ChipItem>,
    subjectList: List<ChipItem>
) {

    Button(onClick = {
        val selectedDept = classList.firstOrNull { it.isSelected }?.label
        val selectedSem = semesterList.firstOrNull { it.isSelected }?.label
        val selectedSubjects = subjectList.filter { it.isSelected }.map { it.label }

        // 🚀 MAKE API CALL WITH THESE
        println("Dept: $selectedDept")
        println("Sem: $selectedSem")
        println("Subjects: $selectedSubjects")
    }) {
        Text("Submit")
    }
}



@Composable
fun chips(
    label : String
){
    var selected by remember { mutableStateOf(false) }
    ElevatedFilterChip(
        selected = selected,
        onClick = {
            selected = !selected
        },
        label = { Text(label, fontSize = 16.sp) },
        modifier = Modifier.padding(end = 8.dp, start = 2.dp),
        elevation = FilterChipDefaults.filterChipElevation(elevation = 8.dp, focusedElevation = 16.dp),
        shape = RectangleShape
    )
}
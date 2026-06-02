package com.example.workhourcounter.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.workhourcounter.viewModel.HomeViewModel
import com.example.workhourcounter.viewModel.WorkplaceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(homeViewModel: HomeViewModel, workplaceViewModel: WorkplaceViewModel) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Form tracking values
    var logDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedShiftType by remember { mutableStateOf("FULL_DAY") }
    var manualBaseHours by remember { mutableStateOf("8") }
    var manualOtHours by remember { mutableStateOf("0") }

    var activeWpDropdownExpanded by remember { mutableStateOf(false) }
    val activeWorkplaces = workplaceViewModel.workplaces
    var targetWorkplace by remember { mutableStateOf(activeWorkplaces.firstOrNull()) }

    // Synchronize selector targets if database lists change
    LaunchedEffect(activeWorkplaces.size) {
        if (targetWorkplace == null && activeWorkplaces.isNotEmpty()) {
            targetWorkplace = activeWorkplaces.first()
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.loadMonthSummary()
        workplaceViewModel.loadWorkplaces()
    }

    val datePicker = DatePickerDialog(
        context, { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d, 0, 0, 0)
            logDate = cal
        },
        logDate.get(Calendar.YEAR), logDate.get(Calendar.MONTH), logDate.get(Calendar.DAY_OF_MONTH)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = "Overview", style = MaterialTheme.typography.headlineMedium)
        }

        // --- SECTION 1: MONTHLY AGGREGATES CARDS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Hours This Month",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${homeViewModel.currentMonthHours}h",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Est. Month Salary",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "\$${
                                String.format(
                                    Locale.getDefault(),
                                    "%.2f",
                                    homeViewModel.currentMonthSalary
                                )
                            }",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // --- SECTION 2: ADD LOG OVERLAY SHEET FORM ---
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add Working Record", style = MaterialTheme.typography.titleMedium)

                    if (activeWorkplaces.isEmpty()) {
                        Text("Please add a workplace in Management tab first.", color = Color.Red)
                    } else {
                        // Workplace Target Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { activeWpDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Workplace: ${targetWorkplace?.name ?: "Select Target"}")
                            }
                            DropdownMenu(
                                expanded = activeWpDropdownExpanded,
                                onDismissRequest = { activeWpDropdownExpanded = false }
                            ) {
                                activeWorkplaces.forEach { wp ->
                                    DropdownMenuItem(
                                        text = { Text(wp.name) },
                                        onClick = {
                                            targetWorkplace = wp
                                            activeWpDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Date Button
                        OutlinedButton(
                            onClick = { datePicker.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Date: ${dateFormat.format(logDate.time)}")
                        }

                        // Shift Type Selector
                        Text("Shift Presets", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "FULL_DAY" to "Full",
                                "HALF_DAY" to "Half",
                                "CUSTOM" to "Custom"
                            ).forEach { preset ->
                                FilterChip(
                                    selected = selectedShiftType == preset.first,
                                    onClick = {
                                        selectedShiftType = preset.first
                                        when (preset.first) {
                                            "FULL_DAY" -> {
                                                manualBaseHours = "8"; manualOtHours = "0"
                                            }

                                            "HALF_DAY" -> {
                                                manualBaseHours = "4"; manualOtHours = "0"
                                            }

                                            "CUSTOM" -> {
                                                manualBaseHours = ""; manualOtHours = "0"
                                            }
                                        }
                                    },
                                    label = { Text(preset.second) }
                                )
                            }
                        }

                        // Hours Entry Form Subfields
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = manualBaseHours,
                                onValueChange = {
                                    if (selectedShiftType == "CUSTOM") manualBaseHours = it
                                },
                                label = { Text("Base Hours") },
                                enabled = selectedShiftType == "CUSTOM",
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = manualOtHours,
                                onValueChange = { manualOtHours = it },
                                label = { Text("OT Hours") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = {
                                val wpId = targetWorkplace?.id
                                val base = manualBaseHours.toFloatOrNull() ?: 0f
                                val ot = manualOtHours.toFloatOrNull() ?: 0f

                                if (wpId != null && base >= 0f) {
                                    homeViewModel.logShift(
                                        wpId,
                                        logDate.timeInMillis,
                                        selectedShiftType,
                                        base,
                                        ot
                                    )
                                    // Reset counters
                                    if (selectedShiftType == "CUSTOM") manualBaseHours = ""
                                    manualOtHours = "0"
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Log Shift")
                        }
                    }
                }
            }
        }
    }
}
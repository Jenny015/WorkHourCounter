package com.example.workhourcounter.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workhourcounter.data.WorkRecord
import com.example.workhourcounter.viewModel.HomeViewModel
import com.example.workhourcounter.viewModel.WorkplaceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(homeViewModel: HomeViewModel, workplaceViewModel: WorkplaceViewModel) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    // Calendar view state controller
    var calendarViewDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Form selections
    var logDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedShiftType by remember { mutableStateOf("FULL_DAY") }
    var manualBaseHours by remember { mutableStateOf("8") }
    var manualOtHours by remember { mutableStateOf("0") }

    var activeWpDropdownExpanded by remember { mutableStateOf(false) }

    // REQUIREMENT 1: Filter out FINISHED workplaces
    val availableWorkplaces = remember(workplaceViewModel.workplaces.size) {
        workplaceViewModel.workplaces.filter { it.status != "FINISHED" }
    }
    var targetWorkplace by remember { mutableStateOf(availableWorkplaces.firstOrNull()) }

    // Dialog state targets
    var pendingOverrideData by remember { mutableStateOf<WorkRecord?>(null) }
    var reviewedRecordDay by remember { mutableStateOf<WorkRecord?>(null) }
    var reviewedWorkplaceName by remember { mutableStateOf("") }

    // Sync database state pipelines on startup
    LaunchedEffect(calendarViewDate) {
        homeViewModel.loadMonthSummary(calendarViewDate)
        workplaceViewModel.loadWorkplaces()
    }

    LaunchedEffect(availableWorkplaces) {
        if (targetWorkplace == null || targetWorkplace !in availableWorkplaces) {
            targetWorkplace = availableWorkplaces.firstOrNull()
        }
    }

    val formDatePicker = DatePickerDialog(
        context, { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d, 0, 0, 0)
            logDate = cal
        },
        logDate.get(Calendar.YEAR), logDate.get(Calendar.MONTH), logDate.get(Calendar.DAY_OF_MONTH)
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- REQUIREMENT 3: CUSTOM CALENDAR TOP PANEL ---
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header Navigation row: [<] Month Year [>]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val newCal = calendarViewDate.clone() as Calendar
                            newCal.add(Calendar.MONTH, -1)
                            calendarViewDate = newCal
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Prev Month") }

                        // Fast Month Picker Action Trigger
                        Text(
                            text = monthFormat.format(calendarViewDate.time),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.clickable {
                                DatePickerDialog(context, { _, y, m, _ ->
                                    val fastCal = Calendar.getInstance()
                                    fastCal.set(y, m, 1)
                                    calendarViewDate = fastCal
                                }, calendarViewDate.get(Calendar.YEAR), calendarViewDate.get(Calendar.MONTH), 1).show()
                            }
                        )

                        IconButton(onClick = {
                            val newCal = calendarViewDate.clone() as Calendar
                            newCal.add(Calendar.MONTH, 1)
                            calendarViewDate = newCal
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Month") }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Matrix Days Render Layout
                    CalendarGridMatrix(
                        calendarContext = calendarViewDate,
                        recordsList = homeViewModel.currentMonthRecords,
                        onDayClick = { selectedDayCal ->
                            // REQUIREMENT 3.3: Day selection evaluation overlay pipeline
                            val matchedRecord = homeViewModel.currentMonthRecords.firstOrNull { log ->
                                val c1 = Calendar.getInstance().apply { timeInMillis = log.date }
                                c1.get(Calendar.DAY_OF_MONTH) == selectedDayCal.get(Calendar.DAY_OF_MONTH)
                            }
                            if (matchedRecord != null) {
                                reviewedRecordDay = matchedRecord
                                reviewedWorkplaceName = workplaceViewModel.workplaces.firstOrNull { it.id == matchedRecord.workplaceId }?.name ?: "Unknown"
                            }
                        }
                    )
                }
            }
        }

        // --- STATS OVERVIEW CARD COMPONENT ROW ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // First Row: 2-Column Split (Days vs OT Hours)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Calculate total equivalent days (Base Hours / 8)
                    val totalBaseHours = homeViewModel.currentMonthRecords.sumOf { it.baseHours.toDouble() }.toFloat()
                    val totalDays = totalBaseHours / 8f

                    // Calculate total OT Hours
                    val totalOtHours = homeViewModel.currentMonthRecords.sumOf { it.otHours.toDouble() }.toFloat()

                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Work Days", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (totalDays % 1f == 0f) totalDays.toInt() else String.format(Locale.getDefault(), "%.1f", totalDays)} Days",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Overtime", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$totalOtHours hrs", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }

                // Second Row: Full Width Estimated Income
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Est. Month Income", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$${String.format(Locale.getDefault(), "%.2f", homeViewModel.currentMonthSalary)}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // --- ADD WORKING RECORD FORM SHEET PANEL ---
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Add Working Record", style = MaterialTheme.typography.titleMedium)

                    if (availableWorkplaces.isEmpty()) {
                        Text("No active workplaces available. Head to Management settings tab.", color = Color.Gray)
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(onClick = { activeWpDropdownExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Workplace: ${targetWorkplace?.name ?: "Select Target"}")
                            }
                            DropdownMenu(expanded = activeWpDropdownExpanded, onDismissRequest = { activeWpDropdownExpanded = false }) {
                                availableWorkplaces.forEach { wp ->
                                    DropdownMenuItem(text = { Text(wp.name) }, onClick = { targetWorkplace = wp; activeWpDropdownExpanded = false })
                                }
                            }
                        }

                        OutlinedButton(onClick = { formDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                            Text("Date: ${dateFormat.format(logDate.time)}")
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("FULL_DAY" to "Full", "HALF_DAY" to "Half", "CUSTOM" to "Custom").forEach { preset ->
                                FilterChip(
                                    selected = selectedShiftType == preset.first,
                                    onClick = {
                                        selectedShiftType = preset.first
                                        when (preset.first) {
                                            "FULL_DAY" -> { manualBaseHours = "8"; manualOtHours = "0" }
                                            "HALF_DAY" -> { manualBaseHours = "4"; manualOtHours = "0" }
                                            "CUSTOM" -> { manualBaseHours = ""; manualOtHours = "0" }
                                        }
                                    },
                                    label = { Text(preset.second) }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(value = manualBaseHours, onValueChange = { if (selectedShiftType == "CUSTOM") manualBaseHours = it }, label = { Text("Base Hours") }, enabled = selectedShiftType == "CUSTOM", modifier = Modifier.weight(1f))
                            OutlinedTextField(value = manualOtHours, onValueChange = { manualOtHours = it }, label = { Text("OT Hours") }, modifier = Modifier.weight(1f))
                        }

                        Button(
                            onClick = {
                                val wpId = targetWorkplace?.id
                                val base = manualBaseHours.toFloatOrNull() ?: 0f
                                val ot = manualOtHours.toFloatOrNull() ?: 0f

                                if (wpId != null) {
                                    if (homeViewModel.checkConflict(wpId, logDate.timeInMillis)) {
                                        pendingOverrideData = WorkRecord(workplaceId = wpId, date = logDate.timeInMillis, shiftType = selectedShiftType, baseHours = base, otHours = ot)
                                    } else {
                                        homeViewModel.logShiftDirect(wpId, logDate.timeInMillis, selectedShiftType, base, ot, calendarViewDate)

                                        // RESET ALIGNMENT ACTION: Reset form tracking states back to today
                                        logDate = Calendar.getInstance()
                                        selectedShiftType = "FULL_DAY"
                                        manualBaseHours = "8"
                                        manualOtHours = "0"
                                    }
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Log Shift") }
                    }
                }
            }
        }
    }

    // --- REQUIREMENT 2.1: OVERRIDE PROTECTION CONFIRMATION DIALOG ---
    pendingOverrideData?.let { record ->
        AlertDialog(
            onDismissRequest = { pendingOverrideData = null },
            title = { Text("Override Existing Record?") },
            text = { Text("Your action will override the old record logged for this day. Do you want to continue?") },
            // Inside your AlertDialog confirmButton click listener:
            confirmButton = {
                Button(onClick = {
                    homeViewModel.logShiftDirect(record.workplaceId, record.date, record.shiftType, record.baseHours, record.otHours, calendarViewDate)
                    pendingOverrideData = null

                    // RESET ALIGNMENT ACTION
                    logDate = Calendar.getInstance()
                    selectedShiftType = "FULL_DAY"
                    manualBaseHours = "8"
                    manualOtHours = "0"
                }) { Text("Update") }
            },
            dismissButton = { TextButton(onClick = { pendingOverrideData = null }) { Text("Cancel") } }
        )
    }

    // --- REQUIREMENT 3.3: REVIEW / DELETE OVERLAY MODAL SHEET ---
    reviewedRecordDay?.let { record ->
        Dialog(onDismissRequest = { reviewedRecordDay = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Review Shift Log", style = MaterialTheme.typography.titleLarge)
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    Text("Workplace: $reviewedWorkplaceName")
                    Text("Date: ${dateFormat.format(Date(record.date))}")
                    Text("Base Hours Worked: ${record.baseHours}h")
                    Text("OT Hours Worked: ${record.otHours}h")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            onClick = {
                                homeViewModel.deleteRecord(record.id, calendarViewDate)
                                reviewedRecordDay = null
                            }
                        ) { Text("Remove Record") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { reviewedRecordDay = null }) { Text("Close") }
                    }
                }
            }
        }
    }
}

// Custom Grid Drawing Sub-composable Engine to render matrix grids
@Composable
fun CalendarGridMatrix(calendarContext: Calendar, recordsList: List<WorkRecord>, onDayClick: (Calendar) -> Unit) {
    val weekDaysLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    val gridCal = calendarContext.clone() as Calendar
    gridCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeekOffset = gridCal.get(Calendar.DAY_OF_WEEK) - 1
    val totalDaysInMonth = gridCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week headers row
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDaysLabels.forEach { label ->
                Text(text = label, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Grid Generator Matrix Engine loops
        var currentDayCounter = 1
        for (weekRow in 0..5) {
            if (currentDayCounter > totalDaysInMonth) break
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                for (dayCol in 0..6) {
                    val isActiveCellInMonthScope = (weekRow > 0 || dayCol >= firstDayOfWeekOffset) && currentDayCounter <= totalDaysInMonth

                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (isActiveCellInMonthScope) {
                            val thisCellDayNum = currentDayCounter
                            val targetedDayCal = (calendarContext.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, thisCellDayNum) }

                            // REQUIREMENT 3.2: Verify if this specific calendar index has an active database entry
                            val hasRecord = recordsList.any { log ->
                                val c2 = Calendar.getInstance().apply { timeInMillis = log.date }
                                c2.get(Calendar.DAY_OF_MONTH) == thisCellDayNum
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { onDayClick(targetedDayCal) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$thisCellDayNum",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (hasRecord) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
                                // Render notification visual dot markers under day cell tracking index
                                if (hasRecord) {
                                    Box(modifier = Modifier.size(5.dp).background(MaterialTheme.colorScheme.primary, shape = CircleShape))
                                }
                            }
                            currentDayCounter++
                        }
                    }
                }
            }
        }
    }
}
package com.example.workhourcounter.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workhourcounter.R
import com.example.workhourcounter.data.ShiftTypeOption
import com.example.workhourcounter.data.StatusOption
import com.example.workhourcounter.data.WorkRecord
import com.example.workhourcounter.viewModel.HomeViewModel
import com.example.workhourcounter.viewModel.WorkplaceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(homeViewModel: HomeViewModel, workplaceViewModel: WorkplaceViewModel) {
    val context = LocalContext.current
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dayTitleFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    // Calendar view state controller
    var calendarViewDate by remember { mutableStateOf(Calendar.getInstance()) }

    // Unified Popup Dialog Architecture
    var selectedCalendarDay by remember { mutableStateOf<Calendar?>(null) }
    var existingRecordForDay by remember { mutableStateOf<WorkRecord?>(null) }

    // Form selections
    var selectedShiftType by remember { mutableStateOf(ShiftTypeOption.FULL_DAY) }
    var manualBaseHours by remember { mutableStateOf("8") }
    var manualOtHours by remember { mutableStateOf("0") }
    var activeWpDropdownExpanded by remember { mutableStateOf(false) }

    // Filter out FINISHED workplaces
    val availableWorkplaces = remember(workplaceViewModel.workplaces.size) {
        workplaceViewModel.workplaces.filter { it.status != StatusOption.FINISHED.dbValue }
    }
    var targetWorkplace by remember { mutableStateOf(availableWorkplaces.firstOrNull()) }

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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item{
            Text(text = stringResource(id = R.string.home_title), style = MaterialTheme.typography.headlineMedium)
        }

        // --- CUSTOM CALENDAR TOP PANEL ---
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
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "上個月") }

                        // Fast Month Picker Action Trigger
                        Text(
                            text = monthFormat.format(calendarViewDate.time),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.clickable {
                                DatePickerDialog(
                                    context,
                                    { _, y, m, _ ->
                                        val fastCal = Calendar.getInstance()
                                        fastCal.set(y, m, 1)
                                        calendarViewDate = fastCal
                                    },
                                    calendarViewDate.get(Calendar.YEAR),
                                    calendarViewDate.get(Calendar.MONTH),
                                    1
                                ).show()
                            }
                        )

                        IconButton(onClick = {
                            val newCal = calendarViewDate.clone() as Calendar
                            newCal.add(Calendar.MONTH, 1)
                            calendarViewDate = newCal
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, "下個月") }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Matrix Days Render Layout
                    CalendarGridMatrix(
                        calendarContext = calendarViewDate,
                        recordsList = homeViewModel.currentMonthRecords,
                        onDayClick = { selectedDayCal ->
                            // Evaluate structural parameters when any day tile is activated
                            val match = homeViewModel.currentMonthRecords.firstOrNull { log ->
                                val checkCal =
                                    Calendar.getInstance().apply { timeInMillis = log.date }
                                checkCal.get(Calendar.DAY_OF_MONTH) == selectedDayCal.get(Calendar.DAY_OF_MONTH) &&
                                        checkCal.get(Calendar.MONTH) == selectedDayCal.get(Calendar.MONTH) &&
                                        checkCal.get(Calendar.YEAR) == selectedDayCal.get(Calendar.YEAR)
                            }

                            selectedCalendarDay = selectedDayCal
                            existingRecordForDay = match

                            if (match != null) {
                                // If editing an existing item, seed states with current historical values
                                selectedShiftType = ShiftTypeOption.fromDbValue(match.shiftType)
                                manualBaseHours = match.baseHours.toString()
                                manualOtHours = match.otHours.toString()
                                targetWorkplace =
                                    workplaceViewModel.workplaces.firstOrNull { it.id == match.workplaceId }
                            } else {
                                // Clear inputs out for clean entry setup
                                selectedShiftType = ShiftTypeOption.FULL_DAY
                                manualBaseHours = "8"
                                manualOtHours = "0"
                                if (availableWorkplaces.isNotEmpty()) targetWorkplace =
                                    availableWorkplaces.first()
                            }
                        }
                    )
                }
            }
        }
        // --- STATS OVERVIEW CARD COMPONENT ROW ---
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
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
                            Text(stringResource(id = R.string.home_month_day), style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (totalDays % 1f == 0f) totalDays.toInt() else String.format(Locale.getDefault(), "%.1f", totalDays)} ${stringResource(id = R.string.unit_day)}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(id = R.string.home_ot), style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$totalOtHours ${stringResource(id = R.string.unit_hour)}", style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
        }
    }
    selectedCalendarDay?.let { targetDayCal ->
        Dialog(onDismissRequest = { selectedCalendarDay = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    // Dialog Header with Exit Element
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (existingRecordForDay == null) stringResource(id = R.string.home_add_record) else stringResource(id = R.string.home_edit_record),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = dayTitleFormat.format(targetDayCal.time),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { selectedCalendarDay = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit Dialog")
                        }
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    if (availableWorkplaces.isEmpty() && existingRecordForDay == null) {
                        Text(stringResource(id = R.string.home_no_workplace), color = Color.Red, style = MaterialTheme.typography.bodyLarge)
                    } else {
                        // Workplace Target Selector Row
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { activeWpDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = existingRecordForDay == null // Keep workplace locked to avoid cross-shifting confusion
                            ) {
                                Text("${stringResource(id = R.string.home_wp)}: ${targetWorkplace?.name ?: stringResource(id = R.string.home_wp_select)}", style = MaterialTheme.typography.bodyLarge)
                            }
                            DropdownMenu(expanded = activeWpDropdownExpanded, onDismissRequest = { activeWpDropdownExpanded = false }) {
                                availableWorkplaces.forEach { wp ->
                                    DropdownMenuItem(text = { Text(wp.name, style = MaterialTheme.typography.bodyLarge) }, onClick = { targetWorkplace = wp; activeWpDropdownExpanded = false })
                                }
                            }
                        }

                        // Shift Presets Chips Layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Iterate directly over the Enum entries
                            ShiftTypeOption.entries.forEach { option ->
                                FilterChip(
                                    // Compare enum instances directly instead of strings
                                    selected = selectedShiftType == option,
                                    onClick = {
                                        selectedShiftType = option
                                        // Use compile-safe when expression over the enum
                                        when (option) {
                                            ShiftTypeOption.FULL_DAY -> { manualBaseHours = "8"; manualOtHours = "0" }
                                            ShiftTypeOption.HALF_DAY -> { manualBaseHours = "4"; manualOtHours = "0" }
                                            ShiftTypeOption.CUSTOM -> { manualBaseHours = ""; manualOtHours = "0" }
                                        }
                                    },
                                    // Resolve the string dynamically using the resource ID
                                    label = {
                                        Text(
                                            text = stringResource(id = option.labelResId),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                )
                            }
                        }

                        // Hour Manual Forms Input Fields Block
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = manualBaseHours,
                                onValueChange = { if (selectedShiftType == ShiftTypeOption.CUSTOM) manualBaseHours = it },
                                label = { Text(stringResource(id = R.string.home_basic_hour), style = MaterialTheme.typography.bodyLarge) },
                                enabled = selectedShiftType == ShiftTypeOption.CUSTOM,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = manualOtHours,
                                onValueChange = { manualOtHours = it },
                                label = { Text(stringResource(id = R.string.home_ot), style = MaterialTheme.typography.bodyLarge) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Bottom Actions Row Layer
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            // Render deletion toggle button ONLY if an entry has pre-existing records data
                            if (existingRecordForDay != null) {
                                TextButton(
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        homeViewModel.deleteRecord(existingRecordForDay!!.id, calendarViewDate)
                                        selectedCalendarDay = null
                                    }
                                ) {
                                    Text(stringResource(id = R.string.opt_del), style = MaterialTheme.typography.bodyLarge)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp)) // Spacer placeholder layout balancing alignment
                            }

                            Button(
                                onClick = {
                                    val wpId = targetWorkplace?.id
                                    val base = manualBaseHours.toFloatOrNull() ?: 0f
                                    val ot = manualOtHours.toFloatOrNull() ?: 0f

                                    if (wpId != null) {
                                        // Logging uses override logic naturally to streamline updates natively
                                        homeViewModel.logShiftDirect(wpId, targetDayCal.timeInMillis, selectedShiftType.dbValue, base, ot, calendarViewDate)
                                        selectedCalendarDay = null
                                    }
                                }
                            ) {
                                Text(if (existingRecordForDay == null) stringResource(id = R.string.opt_add) else stringResource(id = R.string.opt_save), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Grid Drawing Sub-composable Engine to render matrix grids
@Composable
fun CalendarGridMatrix(calendarContext: Calendar, recordsList: List<WorkRecord>, onDayClick: (Calendar) -> Unit) {
    val gridCal = calendarContext.clone() as Calendar
    gridCal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeekOffset = gridCal.get(Calendar.DAY_OF_WEEK) - 1
    val totalDaysInMonth = gridCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week headers row
        Row(modifier = Modifier.fillMaxWidth()) {
            intArrayOf(
                R.string.wd_sun,
                R.string.wd_mon,
                R.string.wd_tue,
                R.string.wd_wed,
                R.string.wd_thu,
                R.string.wd_fri,
                R.string.wd_sat
            ).forEach { label ->
                Text(text = stringResource(id = label), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge, color = Color.Gray)
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
                                    style = MaterialTheme.typography.titleLarge,
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
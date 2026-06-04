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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workhourcounter.Config
import com.example.workhourcounter.R
import com.example.workhourcounter.data.ShiftTypeOption
import com.example.workhourcounter.data.StatusOption
import com.example.workhourcounter.data.WorkRecord
import com.example.workhourcounter.ui.theme.AppDesignSystem
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

    // Settings Configuration Dialog State Hooks
    var isSettingsDialogVisible by remember { mutableStateOf(false) }

    // Form selections
    var selectedShiftType by remember { mutableStateOf(ShiftTypeOption.FULL_DAY) }
    var manualBaseHours by remember { mutableStateOf(Config.baseWorkHour.toString()) }
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

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(id = R.string.home_title), style = AppDesignSystem.getTitleStyle())
                IconButton(
                    // FINISHED: Connect Settings dialog activation hook click trigger
                    onClick = { isSettingsDialogVisible = true },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = stringResource(id = R.string.wp_edit_mode))
                }
            }
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
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.home_prev_month)) }

                        // Fast Month Picker Action Trigger
                        Text(
                            text = monthFormat.format(calendarViewDate.time),
                            style = AppDesignSystem.getSectionHeaderStyle(),
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
                        }) { Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.home_next_month)) }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid Matrix Days Render Layout
                    CalendarGridMatrix(
                        calendarContext = calendarViewDate,
                        recordsList = homeViewModel.currentMonthRecords,
                        onDayClick = { selectedDayCal ->
                            val match = homeViewModel.currentMonthRecords.firstOrNull { log ->
                                val checkCal = Calendar.getInstance().apply { timeInMillis = log.date }
                                checkCal.get(Calendar.DAY_OF_MONTH) == selectedDayCal.get(Calendar.DAY_OF_MONTH) &&
                                        checkCal.get(Calendar.MONTH) == selectedDayCal.get(Calendar.MONTH) &&
                                        checkCal.get(Calendar.YEAR) == selectedDayCal.get(Calendar.YEAR)
                            }

                            selectedCalendarDay = selectedDayCal
                            existingRecordForDay = match

                            if (match != null) {
                                selectedShiftType = ShiftTypeOption.fromDbValue(match.shiftType)
                                manualBaseHours = match.baseHours.toString()
                                manualOtHours = match.otHours.toString()
                                targetWorkplace = workplaceViewModel.workplaces.firstOrNull { it.id == match.workplaceId }
                            } else {
                                selectedShiftType = ShiftTypeOption.FULL_DAY
                                manualBaseHours = Config.baseWorkHour.toString()
                                manualOtHours = "0"
                                if (availableWorkplaces.isNotEmpty()) targetWorkplace = availableWorkplaces.first()
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
                    val totalBaseHours = homeViewModel.currentMonthRecords.sumOf { it.baseHours.toDouble() }.toFloat()
                    val totalDays = totalBaseHours / Config.baseWorkHour // Dynamically divide by configured variable baseline

                    val totalOtHours = homeViewModel.currentMonthRecords.sumOf { it.otHours.toDouble() }.toFloat()

                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(id = R.string.home_month_day), style = AppDesignSystem.getBodyStyle(), color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${if (totalDays % 1f == 0f) totalDays.toInt() else String.format(Locale.getDefault(), "%.1f", totalDays)} ${stringResource(id = R.string.unit_day)}",
                                style = AppDesignSystem.getSectionHeaderStyle()
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(id = R.string.home_ot), style = AppDesignSystem.getBodyStyle(), color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$totalOtHours ${stringResource(id = R.string.unit_hour)}", style = AppDesignSystem.getSectionHeaderStyle())
                        }
                    }
                }
            }
        }
    }

    // --- RENDER POPUP DIALOG FOR TRACKING LOGS MODIFICATION ---
    selectedCalendarDay?.let { targetDayCal ->
        var isInputValid by remember { mutableStateOf(true) }
        Dialog(onDismissRequest = { selectedCalendarDay = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (existingRecordForDay == null) stringResource(id = R.string.home_add_record) else stringResource(id = R.string.home_edit_record),
                                style = AppDesignSystem.getSectionHeaderStyle()
                            )
                            Text(
                                text = dayTitleFormat.format(targetDayCal.time),
                                style = AppDesignSystem.getBodyStyle(),
                                color = Color.Gray
                            )
                        }
                        IconButton(onClick = { selectedCalendarDay = null }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.opt_close))
                        }
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    if (availableWorkplaces.isEmpty() && existingRecordForDay == null) {
                        Text(stringResource(id = R.string.home_no_workplace), color = Color.Gray, style = AppDesignSystem.getSectionHeaderStyle())
                    } else {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { activeWpDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = existingRecordForDay == null
                            ) {
                                Text("${stringResource(id = R.string.home_wp)}: ${targetWorkplace?.name ?: stringResource(id = R.string.home_wp_select)}", style = AppDesignSystem.getBodyStyle())
                            }
                            DropdownMenu(expanded = activeWpDropdownExpanded, onDismissRequest = { activeWpDropdownExpanded = false }) {
                                availableWorkplaces.forEach { wp ->
                                    DropdownMenuItem(text = { Text(wp.name, style = AppDesignSystem.getBodyStyle()) }, onClick = { targetWorkplace = wp; activeWpDropdownExpanded = false })
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ShiftTypeOption.entries.forEach { option ->
                                FilterChip(
                                    selected = selectedShiftType == option,
                                    onClick = {
                                        selectedShiftType = option
                                        when (option) {
                                            ShiftTypeOption.FULL_DAY -> { manualBaseHours = Config.baseWorkHour.toString(); manualOtHours = "0" }
                                            ShiftTypeOption.HALF_DAY -> { manualBaseHours = String.format(Locale.getDefault(), "%.1f", Config.baseWorkHour/2) ; manualOtHours = "0" }
                                            ShiftTypeOption.CUSTOM -> { manualBaseHours = "0"; manualOtHours = "0" }
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(id = option.labelResId),
                                            style = AppDesignSystem.getBodyStyle()
                                        )
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualBaseHours,
                                    onValueChange = { input: String ->
                                        if (selectedShiftType == ShiftTypeOption.CUSTOM) {
                                            manualBaseHours = input
                                            isInputValid = Config.validateHourlyInput(input, manualOtHours)
                                        }
                                    },
                                    label = { Text(stringResource(id = R.string.home_basic_hour), style = AppDesignSystem.getBodyStyle()) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    isError = !isInputValid,
                                    textStyle = AppDesignSystem.getBodyStyle(),
                                    enabled = selectedShiftType == ShiftTypeOption.CUSTOM,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                OutlinedTextField(
                                    value = manualOtHours,
                                    onValueChange = { input: String ->
                                        manualOtHours = input
                                        isInputValid = Config.validateHourlyInput(input, manualBaseHours)
                                    },
                                    label = { Text(stringResource(id = R.string.home_ot), style = AppDesignSystem.getBodyStyle()) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    isError = !isInputValid,
                                    textStyle = AppDesignSystem.getBodyStyle(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        if (!isInputValid) {
                            Text(
                                text = stringResource(id = R.string.home_hour_error2),
                                color = MaterialTheme.colorScheme.error,
                                style = AppDesignSystem.getBodyStyle()
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (existingRecordForDay != null) {
                                TextButton(
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    onClick = {
                                        homeViewModel.deleteRecord(existingRecordForDay!!.id, calendarViewDate)
                                        selectedCalendarDay = null
                                    }
                                ) {
                                    Text(stringResource(id = R.string.opt_del), style = AppDesignSystem.getBodyStyle())
                                }
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }

                            Button(
                                enabled = isInputValid && manualBaseHours.isNotEmpty() && manualBaseHours != "." && manualOtHours.isNotEmpty() && manualOtHours != ".",
                                onClick = {
                                    val wpId = targetWorkplace?.id
                                    val base = manualBaseHours.toFloatOrNull() ?: 0f
                                    val ot = manualOtHours.toFloatOrNull() ?: 0f

                                    if (wpId != null) {
                                        homeViewModel.logShiftDirect(wpId, targetDayCal.timeInMillis, selectedShiftType.dbValue, base, ot, calendarViewDate)
                                        selectedCalendarDay = null
                                    }
                                }
                            ) {
                                Text(if (existingRecordForDay == null) stringResource(id = R.string.opt_add) else stringResource(id = R.string.opt_save), style = AppDesignSystem.getBodyStyle())
                            }
                        }
                    }
                }
            }
        }
    }

    // --- INTEGRATED APP PREFERENCES VIEW CONFIGURATION DIALOG ---
    if (isSettingsDialogVisible) {
        // Declaring these INSIDE the if-block forces them to grab fresh Config values every time it opens
        var localFontSizeSelection by remember { mutableIntStateOf(Config.fontSize) }
        var localBaseHoursInput by remember { mutableStateOf(Config.baseWorkHour.toString()) }
        var isInputValid by remember { mutableStateOf(true) }

        Dialog(onDismissRequest = { isSettingsDialogVisible = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Dialog Header Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.home_setting),
                            style = AppDesignSystem.getSectionHeaderStyle()
                        )
                        IconButton(onClick = { isSettingsDialogVisible = false }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.opt_close))
                        }
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    // Feature 1: Scale font size profiles using graduated "A" tokens
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = stringResource(R.string.home_fsize), style = AppDesignSystem.getBodyStyle(), fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Indexes match your 0, 1, 2 definitions perfectly
                            listOf(0, 1, 2).forEach { sizeIndex ->
                                FilterChip(
                                    // This comparison highlights the active item matching Config.fontSize
                                    selected = localFontSizeSelection == sizeIndex,
                                    onClick = { localFontSizeSelection = sizeIndex },
                                    label = {
                                        Text(
                                            text = "A",
                                            style = AppDesignSystem.getBodyStyle(sizeIndex),
                                            fontWeight = when (sizeIndex) {
                                                0 -> FontWeight.Normal
                                                1 -> FontWeight.SemiBold
                                                2 -> FontWeight.Bold
                                                else -> {
                                                    FontWeight.Normal
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Feature 2: Day Hour Configuration Tracker
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = localBaseHoursInput,
                            onValueChange = { input ->
                                localBaseHoursInput = input
                                isInputValid = Config.validateHourlyInput(input)
                            },
                            label = { Text(stringResource(R.string.home_base_hr), style = AppDesignSystem.getBodyStyle()) },
                            isError = !isInputValid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = AppDesignSystem.getBodyStyle(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!isInputValid) {
                            Text(
                                text = stringResource(id = R.string.home_hour_error1),
                                color = MaterialTheme.colorScheme.error,
                                style = AppDesignSystem.getBodyStyle()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Action Controls Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isSettingsDialogVisible = false }) {
                            Text(stringResource(id = R.string.opt_cancel), style = AppDesignSystem.getBodyStyle())
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = isInputValid && localBaseHoursInput.isNotEmpty() && localBaseHoursInput != ".",
                            onClick = {
                                // Committing the temporary variables back to global Config properties
                                Config.fontSize = localFontSizeSelection
                                Config.baseWorkHour = localBaseHoursInput.toFloat()

                                // Instantly re-trigger summary to adapt hours
                                homeViewModel.loadMonthSummary(calendarViewDate)
                                isSettingsDialogVisible = false
                            }
                        ) {
                            Text(stringResource(id = R.string.opt_save), style = AppDesignSystem.getBodyStyle())
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
                Text(text = stringResource(id = label), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = AppDesignSystem.getCalendarStyle(), color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

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
                                    style = AppDesignSystem.getCalendarStyle(),
                                    color = if (hasRecord) MaterialTheme.colorScheme.primary else Color.Unspecified
                                )
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


package com.example.workhourcounter.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workhourcounter.R
import com.example.workhourcounter.data.StatusOption
import com.example.workhourcounter.data.Workplace
import com.example.workhourcounter.viewModel.WorkplaceViewModel
import java.util.Date
import java.util.Locale

enum class ExecutionMode { NORMAL, PENDING_EDIT, PENDING_DELETE }

@Composable
fun WorkplaceScreen(viewModel: WorkplaceViewModel) {
    var workplaceName by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(StatusOption.WORKING) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    // Track deletion dialog state
    var workplaceToDelete by remember { mutableStateOf<Workplace?>(null) }
    var deleteConfirmationInput by remember { mutableStateOf("") }

    var currentMode by remember { mutableStateOf(ExecutionMode.NORMAL) }
    var workplaceForHistory by remember { mutableStateOf<Workplace?>(null) }

    // Dialog control states
    var isDialogOpen by remember { mutableStateOf(false) }
    var editingWorkplace by remember { mutableStateOf<Workplace?>(null) }

    Scaffold(
        // --- REQUIREMENT 1: FLOATING ACTION BUTTON ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingWorkplace = null
                    workplaceName = ""
                    selectedStatus = StatusOption.WORKING
                    isDialogOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Workplace")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(id = R.string.wp_title), style = MaterialTheme.typography.headlineMedium)
                Row {
                    // Edit Phase Toggle
                    IconButton(
                        onClick = {
                            currentMode =
                                if (currentMode == ExecutionMode.PENDING_EDIT) ExecutionMode.NORMAL else ExecutionMode.PENDING_EDIT
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (currentMode == ExecutionMode.PENDING_EDIT) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "開啟編輯模式")
                    }

                    // Delete Phase Toggle
                    IconButton(
                        onClick = {
                            currentMode =
                                if (currentMode == ExecutionMode.PENDING_DELETE) ExecutionMode.NORMAL else ExecutionMode.PENDING_DELETE
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (currentMode == ExecutionMode.PENDING_DELETE) MaterialTheme.colorScheme.errorContainer else Color.Transparent
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "開啟刪除模式")
                    }
                }
            }
            // Action Status Warning Banner
            if (currentMode != ExecutionMode.NORMAL) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentMode == ExecutionMode.PENDING_EDIT) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = if (currentMode == ExecutionMode.PENDING_EDIT) stringResource(id = R.string.wp_edit_tips) else stringResource(id = R.string.wp_del_tips),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.wp_check_tips),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            // --- DYNAMIC WORKPLACE LIST ---
            if (viewModel.workplaces.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(id = R.string.wp_no_wp),
                        color = Color.Gray,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Use 'items' to dynamically generate list elements!
                    items(viewModel.workplaces) { wp ->
                        WorkplaceItemRow(
                            workplace = wp,
                            onCardClick = {
                                // Route interaction based on current active execution engine state
                                when (currentMode) {
                                    ExecutionMode.PENDING_EDIT -> {
                                        editingWorkplace = wp
                                        workplaceName = wp.name
                                        selectedStatus = StatusOption.fromDbValue(wp.status)
                                        isDialogOpen = true
                                        currentMode = ExecutionMode.NORMAL // Clear immediately
                                    }

                                    ExecutionMode.PENDING_DELETE -> {
                                        workplaceToDelete = wp
                                        deleteConfirmationInput = ""
                                        currentMode = ExecutionMode.NORMAL // Clear immediately
                                    }

                                    ExecutionMode.NORMAL -> {
                                        workplaceForHistory = wp // Launch record history popup
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (isDialogOpen) {
        Dialog(onDismissRequest = { isDialogOpen = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    Alignment.CenterHorizontally
                ) {
                    // Dialog Title Bar Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingWorkplace == null) stringResource(id = R.string.wp_add) else stringResource(id = R.string.wp_edit),
                            style = MaterialTheme.typography.titleLarge
                        )
                        IconButton(onClick = { isDialogOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = "關閉")
                        }
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    // Name Input Fields Framework
                    OutlinedTextField(
                        value = workplaceName,
                        onValueChange = { workplaceName = it },
                        label = { Text(stringResource(id = R.string.wp_name), style = MaterialTheme.typography.bodyLarge) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { statusDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${stringResource(id = R.string.wp_status)}: ${stringResource(id = selectedStatus.labelResId)}", style = MaterialTheme.typography.bodyLarge)
                        }
                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            StatusOption.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = status.labelResId), style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        selectedStatus = status
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Bottom Action
                    Button(
                        onClick = {
                            if (workplaceName.isNotBlank()) {
                                if (editingWorkplace == null) {
                                    // Process add query insertion frameworks pipelines
                                    viewModel.addWorkplace(workplaceName, selectedStatus.dbValue)
                                } else {
                                    // Process modify tracking index pipelines updates parameters
                                    viewModel.updateWorkplace(
                                        editingWorkplace!!.id,
                                        workplaceName,
                                        selectedStatus.dbValue
                                    )
                                }
                                isDialogOpen = false
                            }
                        }
                    ) {
                        Text(text = if (editingWorkplace == null) stringResource(id = R.string.opt_add) else stringResource(id = R.string.opt_save))
                    }
                }
            }
        }
    }

        // --- POPUP DIALOG: HISTORY LIST VIEW ---
        workplaceForHistory?.let { workplace ->
            val records = remember(workplace.id) { viewModel.getRecordsForWorkplace(workplace.id) }
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            Dialog(onDismissRequest = { workplaceForHistory = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f).padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Dialog title bar with dismiss button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = workplace.name, style = MaterialTheme.typography.titleLarge)
                                Text(text = stringResource(id = R.string.wp_record_title), style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            }
                            IconButton(onClick = { workplaceForHistory = null }) {
                                Icon(Icons.Default.Close, contentDescription = "關閉")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (records.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text(stringResource(id = R.string.wp_no_record), color = Color.Gray, style = MaterialTheme.typography.titleLarge)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(records) { log ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Left side: Date string formatted neatly
                                        Text(
                                            text = dateFormat.format(Date(log.date)),
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        // Right side: Calculated cumulative work hours
                                        Text(
                                            text = "${log.baseHours + log.otHours} ${stringResource(id = R.string.unit_hour)}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    HorizontalDivider(
                                        Modifier,
                                        DividerDefaults.Thickness,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        workplaceToDelete?.let { wp ->
            AlertDialog(
                onDismissRequest = { workplaceToDelete = null },
                title = { Text(stringResource(id = R.string.wp_del_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = stringResource(id = R.string.wp_del_body ,wp.name),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(text = stringResource(id = R.string.wp_del_input), style = MaterialTheme.typography.bodyLarge)
                        OutlinedTextField(
                            value = deleteConfirmationInput,
                            onValueChange = { deleteConfirmationInput = it },
                            placeholder = { Text(stringResource(id = R.string.opt_del).lowercase(), style = MaterialTheme.typography.bodyLarge) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = deleteConfirmationInput.trim().lowercase() == stringResource(id = R.string.opt_del),
                        onClick = {
                            viewModel.deleteWorkplace(wp.id)
                            // If we were editing this specific workplace, clear out the form
                            if (editingWorkplace!!.id == wp.id) {
                                editingWorkplace = null
                                workplaceName = ""
                                selectedStatus = StatusOption.WORKING
                            }
                            workplaceToDelete = null
                        }
                    ) {Text(stringResource(id = R.string.opt_del), style = MaterialTheme.typography.bodyLarge)}
                },
                dismissButton = {
                    TextButton(onClick = { workplaceToDelete = null }) {Text(stringResource(id = R.string.opt_cancel), style = MaterialTheme.typography.bodyLarge)}
                }
            )
        }
    }


@Composable
fun WorkplaceItemRow(workplace: Workplace, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // Pushes contents to left & right edges
            verticalAlignment = Alignment.CenterVertically
        ){
            Column{
                Text(text = workplace.name, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(id = R.string.wp_status)}: ${workplace.status}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (StatusOption.fromDbValue(workplace.status)) {
                        StatusOption.WORKING -> Color(0xFF035707)
                        StatusOption.PENDING -> Color(0xFF021da6)
                        else -> Color(0xFF4d4d4d)
                    }
                )
            }
            Column( horizontalAlignment = Alignment.End ) {
                Text(
                    text = "${workplace.totalDays} ${stringResource(id = R.string.unit_day)}",
                    style = MaterialTheme.typography.headlineMedium, // Bold prominent number
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
package com.example.workhourcounter.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workhourcounter.Config
import com.example.workhourcounter.R
import com.example.workhourcounter.data.StatusOption
import com.example.workhourcounter.data.Workplace
import com.example.workhourcounter.ui.theme.AppDesignSystem
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        // --- FLOATING ACTION BUTTON ---
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
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.wp_add))
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
                Text(stringResource(id = R.string.wp_title), style = AppDesignSystem.getTitleStyle())
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
                        Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.wp_edit_mode))
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
                        Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.wp_del_mode))
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
                        style = AppDesignSystem.getBodyStyle(),
                        modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
                    )
                }
            }
            Text(
                text = stringResource(id = R.string.wp_check_tips),
                style = AppDesignSystem.getBodyStyle()
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
                        style = AppDesignSystem.getSectionHeaderStyle()
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
        var nameErrorResId by remember { mutableStateOf<Int?>(null) }
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
                            style = AppDesignSystem.getSectionHeaderStyle()
                        )
                        IconButton(onClick = { isDialogOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.opt_close))
                        }
                    }

                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                    // Name Input Fields Framework
                    OutlinedTextField(
                        value = workplaceName,
                        onValueChange = { input ->
                            if (input.length <= Config.MAX_TEXT_INPUT) {
                                workplaceName = input
                                nameErrorResId = null
                            }
                        },
                        label = { Text(stringResource(id = R.string.wp_name), style = AppDesignSystem.getBodyStyle()) },
                        isError = nameErrorResId != null,
                        textStyle = AppDesignSystem.getBodyStyle(),
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (nameErrorResId != null) {
                                Text(text = stringResource(nameErrorResId!!), color = MaterialTheme.colorScheme.error)
                            } else {
                                // Show a character counter so users know their limit
                                Text(text = "${workplaceName.length} / ${Config.MAX_TEXT_INPUT}", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                            }
                        },
                        singleLine = true
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { statusDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${stringResource(id = R.string.wp_status)}: ${stringResource(id = selectedStatus.labelResId)}", style = AppDesignSystem.getBodyStyle())
                        }
                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            StatusOption.entries.forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(id = status.labelResId), style = AppDesignSystem.getBodyStyle()) },
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
                            val trimName = workplaceName.trim()
                            if (trimName.isEmpty()) {
                                nameErrorResId = R.string.err_name_empty
                            } else {
                                val isDuplicate = viewModel.isNameDuplicate(trimName, editingWorkplace?.id)
                                if(isDuplicate){
                                    nameErrorResId = R.string.wp_name_duplicate
                                } else {
                                    if (editingWorkplace == null) {
                                        // Process add query insertion frameworks pipelines
                                        viewModel.addWorkplace(workplaceName, selectedStatus.dbValue)
                                    } else {
                                        // Process modify tracking index pipelines updates parameters
                                        viewModel.updateWorkplace(editingWorkplace!!.id, workplaceName, selectedStatus.dbValue)
                                    }
                                    nameErrorResId = null
                                    isDialogOpen = false
                                }
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
                                Text(text = workplace.name, style = AppDesignSystem.getSectionHeaderStyle())
                                Text(text = stringResource(id = R.string.wp_record_title), style = AppDesignSystem.getBodyStyle(), color = Color.Gray)
                            }
                            IconButton(onClick = { workplaceForHistory = null }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.opt_close))
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
                                Text(stringResource(id = R.string.wp_no_record), color = Color.Gray, style = AppDesignSystem.getSectionHeaderStyle())
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
                                            style = AppDesignSystem.getBodyStyle()
                                        )
                                        // Right side: Calculated cumulative work hours
                                        Text(
                                            text = "${log.baseHours + log.otHours} ${stringResource(id = R.string.unit_hour)}",
                                            style = AppDesignSystem.getBodyStyle(),
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
                            style = AppDesignSystem.getBodyStyle()
                        )
                        Text(text = stringResource(id = R.string.wp_del_input), style = AppDesignSystem.getBodyStyle())
                        OutlinedTextField(
                            value = deleteConfirmationInput,
                            onValueChange = { deleteConfirmationInput = it },
                            placeholder = { Text(stringResource(id = R.string.opt_del).lowercase(), style = AppDesignSystem.getBodyStyle()) },
                            textStyle = AppDesignSystem.getBodyStyle(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = deleteConfirmationInput.trim().equals(stringResource(id = R.string.opt_del), ignoreCase = true),
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
                    ) {Text(stringResource(id = R.string.opt_del), style = AppDesignSystem.getBodyStyle())}
                },
                dismissButton = {
                    TextButton(onClick = { workplaceToDelete = null }) {Text(stringResource(id = R.string.opt_cancel), style = AppDesignSystem.getBodyStyle())}
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
                Text(text = workplace.name, style = AppDesignSystem.getSectionHeaderStyle())
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stringResource(id = R.string.wp_status)}: ${stringResource(id = StatusOption.fromDbValue(workplace.status).labelResId)}",
                    style = AppDesignSystem.getBodyStyle(),
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
                    style = AppDesignSystem.getSectionHeaderStyle(), // Bold prominent number
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
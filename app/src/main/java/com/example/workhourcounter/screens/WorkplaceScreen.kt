package com.example.workhourcounter.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workhourcounter.data.Workplace
import com.example.workhourcounter.viewModel.WorkplaceViewModel
import java.util.Date
import java.util.Locale

enum class ExecutionMode { NORMAL, PENDING_EDIT, PENDING_DELETE }

@Composable
fun WorkplaceScreen(viewModel: WorkplaceViewModel) {
    var nameInput by remember { mutableStateOf("") }
    var statusInput by remember { mutableStateOf("主力盤") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Track if we are editing an existing item
    var editingWorkplaceId by remember { mutableStateOf<Long?>(null) }

    // Track deletion dialog state
    var workplaceToDelete by remember { mutableStateOf<Workplace?>(null) }
    var deleteConfirmationInput by remember { mutableStateOf("") }

    var currentMode by remember { mutableStateOf(ExecutionMode.NORMAL) }
    var workplaceForHistory by remember { mutableStateOf<Workplace?>(null) }

    val statusOptions = listOf("主力盤", "較少去", "已起貨")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "工作地盤管理", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // --- FORM PANEL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = if (editingWorkplaceId == null) "加新地盤" else "修改資訊",
                    style = MaterialTheme.typography.titleLarge)

                // Name Input
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("地盤名稱", style = MaterialTheme.typography.bodyLarge)},
                    modifier = Modifier.fillMaxWidth()
                )

                // Status Selector (Dropdown)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("狀態: $statusInput", style = MaterialTheme.typography.bodyLarge)
                    }
                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status, style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    statusInput = status
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Show a cancel button if we are in edit mode
                    if (editingWorkplaceId != null) {
                        TextButton(onClick = {
                            editingWorkplaceId = null
                            nameInput = ""
                            statusInput = "主力盤"
                        }) {
                            Text("取消", style = MaterialTheme.typography.bodyLarge)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                // Submit Button
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            val currentId = editingWorkplaceId
                            if (currentId == null) {
                                viewModel.addWorkplace(nameInput, statusInput)
                            } else {
                                viewModel.updateWorkplace(currentId, nameInput, statusInput)
                                editingWorkplaceId = null // Exit edit mode
                            }
                            nameInput = ""
                            statusInput = "主力盤"
                        }
                    }
                ) {
                    Text(if (editingWorkplaceId == null) "新增地盤" else "儲存變更", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("你的工作地點", style = MaterialTheme.typography.titleLarge)
            Row {
                // Edit Phase Toggle
                IconButton(
                    onClick = {
                        currentMode = if (currentMode == ExecutionMode.PENDING_EDIT) ExecutionMode.NORMAL else ExecutionMode.PENDING_EDIT
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (currentMode == ExecutionMode.PENDING_EDIT) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "開啟編輯地盤模式")
                }

                // Delete Phase Toggle
                IconButton(
                    onClick = {
                        currentMode = if (currentMode == ExecutionMode.PENDING_DELETE) ExecutionMode.NORMAL else ExecutionMode.PENDING_DELETE
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (currentMode == ExecutionMode.PENDING_DELETE) MaterialTheme.colorScheme.errorContainer else Color.Transparent
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "開啟刪除地盤模式")
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
                    text = if (currentMode == ExecutionMode.PENDING_EDIT) "🔧 點擊一個地盤以編輯." else "⚠️ 點擊一個地盤以刪除.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp).align(Alignment.CenterHorizontally)
                )
            }
        }
        Text(
            text = "點擊任意地盤查看出勤記錄",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        // --- DYNAMIC WORKPLACE LIST ---
        if (viewModel.workplaces.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("未有任何工作地點", color = Color.Gray, style = MaterialTheme.typography.titleLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Use 'items' to dynamically generate list elements!
                items(viewModel.workplaces) { workplace ->
                    WorkplaceItemRow(
                        workplace = workplace,
                        onCardClick = {
                            // Route interaction based on current active execution engine state
                            when (currentMode) {
                                ExecutionMode.PENDING_EDIT -> {
                                    editingWorkplaceId = workplace.id
                                    nameInput = workplace.name
                                    statusInput = workplace.status
                                    currentMode = ExecutionMode.NORMAL // Clear immediately
                                }
                                ExecutionMode.PENDING_DELETE -> {
                                    workplaceToDelete = workplace
                                    deleteConfirmationInput = ""
                                    currentMode = ExecutionMode.NORMAL // Clear immediately
                                }
                                ExecutionMode.NORMAL -> {
                                    workplaceForHistory = workplace // Launch record history popup
                                }
                            }
                        }
                    )
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
                                Text(text = "出勤記錄", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { workplaceForHistory = null }) {
                                Icon(Icons.Default.Close, contentDescription = "關閉")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        if (records.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                                Text("此地盤暫沒有任何出勤記錄", color = Color.Gray)
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
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        // Right side: Calculated cumulative work hours
                                        Text(
                                            text = "${log.baseHours + log.otHours} hrs",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        workplaceToDelete?.let { workplace ->
            AlertDialog(
                onDismissRequest = { workplaceToDelete = null },
                title = { Text("注意: 刪除地盤屬於不可撒消的操作") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "此行為將一併刪除所有 \"${workplace.name}\" 地盤的出勤記錄, 是否要刪除此地盤?",
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(text = "請在下方欄位輸入 \"刪除\" 以確認:")
                        OutlinedTextField(
                            value = deleteConfirmationInput,
                            onValueChange = { deleteConfirmationInput = it },
                            placeholder = { Text("刪除") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        enabled = deleteConfirmationInput.trim() == "刪除",
                        onClick = {
                            viewModel.deleteWorkplace(workplace.id)
                            // If we were editing this specific workplace, clear out the form
                            if (editingWorkplaceId == workplace.id) {
                                editingWorkplaceId = null
                                nameInput = ""
                                statusInput = "主力盤"
                            }
                            workplaceToDelete = null
                        }
                    ) {Text("刪除地盤")}
                },
                dismissButton = {
                    TextButton(onClick = { workplaceToDelete = null }) {Text("取消")}
                }
            )
        }
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
                    text = "狀態: ${workplace.status}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (workplace.status) {
                        "主力盤" -> Color(0xFF035707)
                        "較少去" -> Color(0xFF021da6)
                        else -> Color(0xFF4d4d4d)
                    }
                )
            }
            Column( horizontalAlignment = Alignment.End ) {
                Text(
                    text = "${workplace.totalDays}",
                    style = MaterialTheme.typography.headlineMedium, // Bold prominent number
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "日",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black
                )
            }
        }
    }
}
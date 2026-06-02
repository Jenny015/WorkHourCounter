package com.example.workhourcounter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.workhourcounter.Workplace
import com.example.workhourcounter.WorkplaceViewModel

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

    val statusOptions = listOf("主力盤", "較少去", "已起貨")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "工作地盤管理", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // --- ADD NEW WORKPLACE FORM ---
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

        Spacer(modifier = Modifier.height(24.dp))
        Text("你的工作地點", style = MaterialTheme.typography.titleLarge)
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
                            // When clicked, populate the form above for editing
                            editingWorkplaceId = workplace.id
                            nameInput = workplace.name
                            statusInput = workplace.status
                        },
                        onDeleteClick = {
                            // Trigger deletion dialog pipeline
                            workplaceToDelete = workplace
                            deleteConfirmationInput = ""
                        }
                    )
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
                    ) {
                        Text("刪除地盤")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { workplaceToDelete = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun WorkplaceItemRow(
    workplace: Workplace,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, // Pushes contents to left & right edges
            verticalAlignment = Alignment.CenterVertically
        ){
            Column(modifier = Modifier.weight(1f)){
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除地盤",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
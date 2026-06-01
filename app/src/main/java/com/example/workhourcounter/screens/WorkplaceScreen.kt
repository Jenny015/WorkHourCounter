package com.example.workhourcounter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
                Text("加新地盤", style = MaterialTheme.typography.titleLarge)

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

                // Submit Button
                Button(
                    onClick = {
                        if (nameInput.isNotBlank()) {
                            viewModel.addWorkplace(nameInput, statusInput)
                            nameInput = "" // Clear input after adding
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("新增地盤", style = MaterialTheme.typography.bodyLarge)
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
                // Notice how we use 'items' to dynamically generate list elements!
                items(viewModel.workplaces) { workplace ->
                    WorkplaceItemRow(workplace = workplace)
                }
            }
        }
    }
}

@Composable
fun WorkplaceItemRow(workplace: Workplace) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            Column(
                horizontalAlignment = Alignment.End
            ) {
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
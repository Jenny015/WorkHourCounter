package com.example.workhourcounter.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.workhourcounter.viewModel.CardModel
import com.example.workhourcounter.viewModel.CardsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.DatePickerDialog

@Composable
fun CardsScreen(viewModel: CardsViewModel) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<CardModel?>(null) }

    // Dialog state controllers
    var nameInput by remember { mutableStateOf("") }
    var noInput by remember { mutableStateOf("") }
    var expirationCalendar by remember { mutableStateOf(Calendar.getInstance()) }

    val openDatePicker = DatePickerDialog(
        context, { _, y, m, d ->
            val cal = Calendar.getInstance()
            cal.set(y, m, d, 0, 0, 0)
            expirationCalendar = cal
        },
        expirationCalendar.get(Calendar.YEAR), expirationCalendar.get(Calendar.MONTH), expirationCalendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCard = null
                    nameInput = ""
                    noInput = ""
                    expirationCalendar = Calendar.getInstance()
                    showDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, contentDescription = "Add New Card") }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text(text = "我的工作證件", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.cardsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("未有任何工作證件，按右下角「＋」新增。", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(viewModel.cardsList) { card ->
                        // Calculate time difference dynamically
                        val currentTimeMs = System.currentTimeMillis()
                        val oneMonthMs = 30L * 24 * 60 * 60 * 1000
                        val diff = card.expireDate - currentTimeMs

                        // Select background color based on warning condition
                        val cardBackgroundColor = if (diff in 0..oneMonthMs) {
                            Color(0xFFFFF9C4)
                        } else if(diff < 0){
                            Color(0xFFFFC4C4)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                editingCard = card
                                nameInput = card.name
                                noInput = card.no
                                expirationCalendar = Calendar.getInstance().apply { timeInMillis = card.expireDate }
                                showDialog = true
                            },
                            colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = card.name, style = MaterialTheme.typography.titleLarge)
                                    // Optional visual badge indicator
                                    if (diff <= oneMonthMs) {
                                        Text(
                                            text = if (diff > 0) "⚠️ 即將過期" else "⚠️ 已經過期",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "編號: ${card.no}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                                Text(text = "過期日: ${dateFormat.format(Date(card.expireDate))}", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- MANAGE / ADD / MODIFY UNIFIED DIALOG ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingCard == null) "新增工作證件" else "修改工作證件") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("證件名稱", style = MaterialTheme.typography.bodyLarge) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noInput, onValueChange = { noInput = it }, label = { Text("證件編號", style = MaterialTheme.typography.bodyLarge) }, modifier = Modifier.fillMaxWidth())

                    OutlinedButton(onClick = { openDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("過期日: ${dateFormat.format(expirationCalendar.time)}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nameInput.isNotBlank() && noInput.isNotBlank()) {
                        val activeItem = editingCard
                        if (activeItem == null) {
                            viewModel.addCard(nameInput, noInput, expirationCalendar.timeInMillis)
                        } else {
                            viewModel.updateCard(activeItem.id, nameInput, noInput, expirationCalendar.timeInMillis)
                        }
                        showDialog = false
                    }
                }) { Text("保存", style = MaterialTheme.typography.bodyLarge) }
            },
            dismissButton = {
                Row {
                    if (editingCard != null) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            onClick = { showDeleteConfirm = true }
                        ) { Text("刪除") }
                    }
                    TextButton(onClick = { showDialog = false }) { Text("取消", style = MaterialTheme.typography.bodyLarge) }
                }
            }
        )
    }

    // --- SECURE DELETION CONFIRMATION VALVE VALVE ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("刪除證件") },
            text = { Text("請問你確定要刪除這項資料嗎?", style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        editingCard?.let { viewModel.deleteCard(it.id) }
                        showDeleteConfirm = false
                        showDialog = false
                    }
                ) { Text("碓定", style = MaterialTheme.typography.bodyLarge) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("取消", style = MaterialTheme.typography.bodyLarge) } }
        )
    }
}
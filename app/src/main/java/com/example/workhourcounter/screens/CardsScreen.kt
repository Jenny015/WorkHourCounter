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
            Text(text = "Card Management", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.cardsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No cards stored yet.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    items(viewModel.cardsList) { card ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                editingCard = card
                                nameInput = card.name
                                noInput = card.no
                                expirationCalendar = Calendar.getInstance().apply { timeInMillis = card.expireDate }
                                showDialog = true
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = card.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "No: ${card.no}", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                Text(text = "Expires: ${dateFormat.format(Date(card.expireDate))}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
            title = { Text(if (editingCard == null) "Add New Card" else "Modify Card Parameters") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("Card Label Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noInput, onValueChange = { noInput = it }, label = { Text("Card Identifier No") }, modifier = Modifier.fillMaxWidth())

                    OutlinedButton(onClick = { openDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("Expiration Date: ${dateFormat.format(expirationCalendar.time)}")
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
                }) { Text("Save") }
            },
            dismissButton = {
                Row {
                    if (editingCard != null) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            onClick = { showDeleteConfirm = true }
                        ) { Text("Delete") }
                    }
                    TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

    // --- SECURE DELETION CONFIRMATION VALVE VALVE ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirm Erasure") },
            text = { Text("Are you absolutely sure you want to delete this card parameter data file?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        editingCard?.let { viewModel.deleteCard(it.id) }
                        showDeleteConfirm = false
                        showDialog = false
                    }
                ) { Text("Confirm Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}
package com.example.workhourcounter.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.workhourcounter.R
import com.example.workhourcounter.ui.theme.AppDesignSystem
import com.example.workhourcounter.viewModel.CardModel
import com.example.workhourcounter.viewModel.CardsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            ) { Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.cards_add_card)) }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Text(text = stringResource(id = R.string.cards_title), style = AppDesignSystem.getTitleStyle())
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.cardsList.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(id = R.string.cards_no_card), color = Color.Gray, style = AppDesignSystem.getSectionHeaderStyle())
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
                            Color(0xFFFFDEDE)
                        } else {
                            MaterialTheme.colorScheme.surface
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
                                    Text(text = card.name, style = AppDesignSystem.getSectionHeaderStyle())
                                    if (diff <= oneMonthMs) {
                                        Text(
                                            text = if (diff > 0) stringResource(id = R.string.cards_expired_soon) else stringResource(id = R.string.cards_expired),
                                            style = AppDesignSystem.getBodyStyle(),
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${stringResource(id = R.string.cards_no)}: ${card.no}", style = AppDesignSystem.getBodyStyle(), color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "${stringResource(id = R.string.cards_expired_date)}: ${dateFormat.format(Date(card.expireDate))}", style = AppDesignSystem.getBodyStyle())
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
            title = { Text(if (editingCard == null) stringResource(id = R.string.cards_add_card) else stringResource(id = R.string.cards_edit_card)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(stringResource(id = R.string.cards_name), style = AppDesignSystem.getBodyStyle()) },
                        textStyle = AppDesignSystem.getBodyStyle(),
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = noInput,
                        onValueChange = { noInput = it },
                        label = { Text(stringResource(id = R.string.cards_no), style = AppDesignSystem.getBodyStyle()) },
                        textStyle = AppDesignSystem.getBodyStyle(),
                        modifier = Modifier.fillMaxWidth())

                    OutlinedButton(onClick = { openDatePicker.show() }, modifier = Modifier.fillMaxWidth()) {
                        Text("${stringResource(id = R.string.cards_expired_date)}: ${dateFormat.format(expirationCalendar.time)}", style = AppDesignSystem.getBodyStyle())
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
                }) { Text(if (editingCard == null) stringResource(id = R.string.opt_add) else stringResource(id = R.string.opt_save), style = AppDesignSystem.getBodyStyle()) }
            },
            dismissButton = {
                Row {
                    if (editingCard != null) {
                        TextButton(
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                            onClick = { showDeleteConfirm = true }
                        ) { Text(stringResource(id = R.string.opt_del), style = AppDesignSystem.getBodyStyle()) }
                    }
                    TextButton(onClick = { showDialog = false }) { Text(stringResource(id = R.string.opt_cancel), style = AppDesignSystem.getBodyStyle()) }
                }
            }
        )
    }

    // --- SECURE DELETION CONFIRMATION VALVE VALVE ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(id = R.string.cards_del_title)) },
            text = { Text(stringResource(id = R.string.cards_del_body, editingCard?.name ?: stringResource(id = R.string.cards_del_no_name)), style = AppDesignSystem.getBodyStyle()) },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        editingCard?.let { viewModel.deleteCard(it.id) }
                        showDeleteConfirm = false
                        showDialog = false
                    }
                ) { Text(stringResource(id = R.string.opt_yes), style = AppDesignSystem.getBodyStyle()) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(id = R.string.opt_cancel), style = AppDesignSystem.getBodyStyle()) } }
        )
    }
}
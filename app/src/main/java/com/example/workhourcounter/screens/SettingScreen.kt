package com.example.workhourcounter.screens

import android.app.DatePickerDialog
import android.icu.text.NumberFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.workhourcounter.viewModel.SettingsViewModel
import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import java.util.*

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Form inputs state variables
    var salaryInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var paymentDayInput by remember { mutableStateOf(viewModel.paymentDay.toString()) }

    // System DatePickerDialog builder pipeline
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            selectedDate = cal
        },
        selectedDate.get(Calendar.YEAR),
        selectedDate.get(Calendar.MONTH),
        selectedDate.get(Calendar.DAY_OF_MONTH)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(text = "設定", style = MaterialTheme.typography.headlineMedium)
        }

        // --- SECTION 1: GLOBAL MONTHLY PAYMENT RECURRENCE DAY ---
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    //Text("Payment Target Rule", style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = paymentDayInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || (input.toIntOrNull() != null && input.toInt() in 1..31)) {
                                    paymentDayInput = input
                                }
                            },
                            label = { Text("發薪日", style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = {
                            val day = paymentDayInput.toIntOrNull() ?: 7
                            viewModel.updatePaymentDay(day)
                        }) {
                            Text("保存", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Text(
                        text = "設定每月 ${viewModel.paymentDay}號 為發薪日",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.Gray
                    )
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("更新月薪", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = salaryInput,
                        onValueChange = { salaryInput = it },
                        label = { Text("月薪 ($)", style = MaterialTheme.typography.bodyLarge) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "生效日期: ${dateFormat.format(selectedDate.time)}", style = MaterialTheme.typography.bodyLarge)
                        OutlinedButton(onClick = { datePickerDialog.show() }) {
                            Text("選擇日期", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Button(
                        onClick = {
                            val amount = salaryInput.toFloatOrNull()
                            if (amount != null && amount > 0f) {
                                // Zero out specific hours fields for accurate comparisons bounds
                                selectedDate.set(Calendar.HOUR_OF_DAY, 0)
                                selectedDate.set(Calendar.MINUTE, 0)
                                selectedDate.set(Calendar.SECOND, 0)
                                selectedDate.set(Calendar.MILLISECOND, 0)

                                viewModel.addSalaryRate(selectedDate.timeInMillis, amount)
                                salaryInput = ""
                                selectedDate = Calendar.getInstance()
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("更改月薪", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        // --- SECTION 3: DISPLAY OF SALARY HISTORICAL RECORDS ---
        item {
            Text("月薪更新記錄", style = MaterialTheme.typography.titleLarge)
        }

        if (viewModel.salaryHistory.isEmpty()) {
            item {
                Text(
                    "沒有找到月薪記錄，請從上方「更新月薪」表格建立新的月薪記錄。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            items(viewModel.salaryHistory) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "\$${NumberFormat.getNumberInstance(Locale.US).format(entry.second)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "生效日期: ${dateFormat.format(Date(entry.first))}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        IconButton(onClick = { viewModel.removeSalaryRate(entry.first) }) {
                            Icon(Icons.Default.Delete, contentDescription = "刪除記錄", tint = Color.Red)
                        }
                    }
                }
            }
        }
    }
}
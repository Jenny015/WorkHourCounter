package com.example.workhourcounter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workhourcounter.viewModel.StatisticsViewModel

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {

    // Refresh statistics every time the statistics screen loads
    LaunchedEffect(Unit) {
        viewModel.generateStatistics()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(text = "統計資料", style = MaterialTheme.typography.headlineMedium)
        }

        // --- SEGMENT 1: LIFETIME OVERVIEW GRAND SCOREBOARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("總工數", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Text("${viewModel.totalDaysLogged} 日", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("總工時", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Text("${viewModel.lifetimeHours}h小時", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- SEGMENT 2: BREAKDOWN BY INDIVIDUAL WORKPLACE ---
        item {
            Text("工作地點工時", style = MaterialTheme.typography.titleLarge)
        }

        if (viewModel.workplaceStatsList.isEmpty()) {
            item {
                Text("未有出勤記錄", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
            }
        } else {
            items(viewModel.workplaceStatsList) { stat ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(stat.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            "${stat.totalHours} 小時",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- SEGMENT 3: MONTHLY HISTORY TRENDS ---
        item {
            Text("每月出勤記錄", style = MaterialTheme.typography.titleLarge)
        }

        if (viewModel.monthlyTrendList.isEmpty()) {
            item {
                Text("未有出勤記錄。", color = Color.Gray, style = MaterialTheme.typography.titleMedium)
            }
        } else {
            items(viewModel.monthlyTrendList) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = entry.monthLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            text = "${entry.hours}小時",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
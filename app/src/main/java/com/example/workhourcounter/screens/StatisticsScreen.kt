package com.example.workhourcounter.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workhourcounter.R
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
            Text(text = stringResource(id = R.string.st_title), style = MaterialTheme.typography.headlineMedium)
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
                            Text(stringResource(id = R.string.st_total_day), style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Text("${viewModel.totalDaysLogged} ${stringResource(id = R.string.unit_day)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(stringResource(id = R.string.st_total_hr), style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                            Text("${viewModel.lifetimeHours} ${stringResource(id = R.string.unit_hour)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
            Text(stringResource(id = R.string.st_per_wp), style = MaterialTheme.typography.titleLarge)
        }

        if (viewModel.workplaceStatsList.isEmpty()) {
            item {
                Text(stringResource(id = R.string.st_no_record), color = Color.Gray, style = MaterialTheme.typography.titleMedium)
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
                            "${stat.totalHours} ${stringResource(id = R.string.unit_hour)}",
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
            Text(stringResource(id = R.string.st_per_month), style = MaterialTheme.typography.titleLarge)
        }

        if (viewModel.monthlyTrendList.isEmpty()) {
            item {
                Text(stringResource(id = R.string.st_no_record), color = Color.Gray, style = MaterialTheme.typography.titleMedium)
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
                            text = "${entry.hours} ${stringResource(id = R.string.unit_hour)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
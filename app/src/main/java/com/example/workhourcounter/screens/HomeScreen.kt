package com.example.workhourcounter.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(text = "Welcome to Home Screen", style = MaterialTheme.typography.headlineMedium) }
        item { Card(modifier = Modifier.fillMaxWidth()) { Text("Calendar Card", modifier = Modifier.padding(16.dp)) } }
        item { Card(modifier = Modifier.fillMaxWidth()) { Text("Add Record Card", modifier = Modifier.padding(16.dp)) } }
        item { Card(modifier = Modifier.fillMaxWidth()) { Text("This Month Hours: 0 hrs", modifier = Modifier.padding(16.dp)) } }
    }
}
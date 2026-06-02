package com.example.workhourcounter

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "首頁", Icons.Default.Home)
    data object Workplace : Screen("workplace", "地盤", Icons.Default.Place)
    data object Statistics : Screen("statistics", "統計", Icons.Default.DateRange)
    data object Cards : Screen("cards", "證件", Icons.Default.AccountBox)
}
package com.example.workhourcounter

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, @param:StringRes val title: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.home_title, Icons.Default.Home)
    data object Workplace : Screen("workplace", R.string.screen_wp, Icons.Default.Place)
    data object Statistics : Screen("statistics", R.string.screen_st, Icons.Default.DateRange)
    data object Cards : Screen("cards", R.string.screen_cards, Icons.Default.AccountBox)
}
package com.example.workhourcounter

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workhourcounter.screens.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workhourcounter.viewModel.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(Screen.Workplace, Screen.Cards, Screen.Home, Screen.Dashboard, Screen.Settings)
    val workplaceViewModel: WorkplaceViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Get the current active screen route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = {Text(text = screen.title, style = MaterialTheme.typography.titleLarge)},

                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // This NavHost controls which screen to actually render inside the Scaffold body
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Workplace.route) { WorkplaceScreen(viewModel = workplaceViewModel)}
            composable(Screen.Dashboard.route) { DashboardScreen() }
            composable(Screen.Settings.route) { SettingsScreen(viewModel = settingsViewModel) }
            composable(Screen.Cards.route) { CardScreen() }
        }
    }
}
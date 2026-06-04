package com.example.workhourcounter

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.workhourcounter.screens.CardsScreen
import com.example.workhourcounter.screens.HomeScreen
import com.example.workhourcounter.screens.StatisticsScreen
import com.example.workhourcounter.screens.WorkplaceScreen
import com.example.workhourcounter.ui.theme.AppDesignSystem
import com.example.workhourcounter.viewModel.CardsViewModel
import com.example.workhourcounter.viewModel.HomeViewModel
import com.example.workhourcounter.viewModel.StatisticsViewModel
import com.example.workhourcounter.viewModel.WorkplaceViewModel

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(Screen.Workplace, Screen.Home, Screen.Cards, Screen.Statistics)
    val workplaceViewModel: WorkplaceViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val statisticsViewModel: StatisticsViewModel = viewModel()
    val cardsViewModel: CardsViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Get the current active screen route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = stringResource(screen.title)) },
                        label = {Text(text = stringResource(screen.title), style = AppDesignSystem.getBodyStyle())},

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
                                    // re-selecting the same item
                                    launchSingleTop = true
                                    // Restore state when re-selecting a previously selected item
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
            composable(Screen.Home.route) { HomeScreen(homeViewModel = homeViewModel, workplaceViewModel = workplaceViewModel) }
            composable(Screen.Workplace.route) { WorkplaceScreen(viewModel = workplaceViewModel)}
            composable(Screen.Statistics.route) { StatisticsScreen(viewModel = statisticsViewModel) }
            composable(Screen.Cards.route) { CardsScreen(viewModel = cardsViewModel) }
        }
    }
}
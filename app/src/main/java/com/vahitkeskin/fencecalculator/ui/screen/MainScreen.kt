package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.ui.components.AnimatedWaveBottomBar
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home_tab", "Anasayfa", Icons.Default.Home)
    object Calculations : Screen("calculations_tab", "Hesaplar", Icons.Default.Calculate)
    object Custom : Screen("custom_cards_tab", "Özel", Icons.Default.Extension)
    object Profile : Screen("profile_tab", "Profil", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CalculatorViewModel,
    globalNavController: NavController
) {
    val innerNavController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Calculations, Screen.Custom, Screen.Profile)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
    
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    innerNavController.navigate(screen.route) {
                                        popUpTo(innerNavController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        )
                    }
            }
        }
    ) { innerPadding ->
        val imeBottom = with(LocalDensity.current) { WindowInsets.ime.getBottom(this).toDp() }
        val bottomPadding = max(innerPadding.calculateBottomPadding(), imeBottom)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = bottomPadding
                )
        ) {
            NavHost(
                navController = innerNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 72.dp)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(viewModel = viewModel, navController = globalNavController)
                }
                composable(Screen.Calculations.route) {
                    CalculationsScreen(viewModel = viewModel)
                }
                composable(Screen.Custom.route) {
                    CustomCardsScreen(viewModel = viewModel, navController = globalNavController)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel = viewModel, navController = globalNavController)
                }
            }
            
            AnimatedWaveBottomBar(
                totalCost = viewModel.grandTotalCost,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

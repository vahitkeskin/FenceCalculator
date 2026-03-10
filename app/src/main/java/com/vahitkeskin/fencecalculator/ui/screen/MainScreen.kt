package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.ui.components.AnimatedWaveBottomBar
import com.vahitkeskin.fencecalculator.ui.components.BannerAdView
import com.vahitkeskin.fencecalculator.ui.components.DnsBannerWarning
import com.vahitkeskin.fencecalculator.ui.components.PremiumDialog
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import com.vahitkeskin.fencecalculator.util.DnsDetector

sealed class Screen(
    val route: String,
    val title: (com.vahitkeskin.fencecalculator.util.AppStrings) -> String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : Screen("home_tab", { it.navHome }, Icons.Default.Home)
    object Calculations : Screen("calculations_tab", { it.navCalculations }, Icons.Default.Calculate)
    object Custom : Screen("custom_cards_tab", { it.navCustom }, Icons.Default.Extension)
    object Profile : Screen("profile_tab", { it.navProfile }, Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CalculatorViewModel,
    globalNavController: NavController
) {
    val innerNavController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Calculations, Screen.Custom, Screen.Profile)
    var showPremiumPopup by remember { mutableStateOf(false) }

    if (showPremiumPopup) {
        PremiumDialog(
            strings = viewModel.strings,
            onDismiss = { showPremiumPopup = false }
        )
    }

    val context = LocalContext.current

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            Column {
                AnimatedWaveBottomBar(
                    totalCost = viewModel.grandTotalCost,
                    isBlurred = false,
                    onClick = { showPremiumPopup = true }
                )
                if (viewModel.isPrivateDnsEnabled && !viewModel.isPremium) {
                    DnsBannerWarning(strings = viewModel.strings)
                } else {
                    BannerAdView()
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
    
                    items.forEach { screen ->
                        val title = screen.title(viewModel.strings)
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = title) },
                            label = { Text(title) },
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
                                } else {
                                    viewModel.requestScrollToTop(screen.route)
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
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = viewModel, 
                        navController = globalNavController,
                        onPremiumClick = { showPremiumPopup = true }
                    )
                }
                composable(Screen.Calculations.route) {
                    CalculationsScreen(
                        viewModel = viewModel,
                        onPremiumClick = { showPremiumPopup = true }
                    )
                }
                composable(Screen.Custom.route) {
                    CustomCardsScreen(
                        viewModel = viewModel, 
                        navController = globalNavController,
                        onPremiumClick = { showPremiumPopup = true }
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel = viewModel, navController = globalNavController)
                }
            }
        }
    }
}

@AppPreviews
@Composable
fun MainScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    val navController = rememberNavController()
    
    FenceCalculatorTheme {
        MainScreen(viewModel = viewModel, globalNavController = navController)
    }
}

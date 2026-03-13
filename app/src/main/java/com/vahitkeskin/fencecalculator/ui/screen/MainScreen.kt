package com.vahitkeskin.fencecalculator.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    
    // Custom history to track visited tabs (unique screens)
    val tabHistory = remember { mutableStateListOf<String>(Screen.Home.route) }

    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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
                /*
                if (viewModel.isPrivateDnsEnabled && !viewModel.isPremium) {
                    DnsBannerWarning(strings = viewModel.strings)
                } else {
                    BannerAdView(strings = viewModel.strings)
                }
                */
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
                                    // Update history: move/add to end of list
                                    tabHistory.remove(screen.route)
                                    tabHistory.add(screen.route)
                                    
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
        val density = LocalDensity.current
        val imeBottom = with(density) { WindowInsets.ime.getBottom(this).toDp() }
        val isKeyboardOpen = imeBottom > 0.dp
        
        // This is the height of the bottom bar (NavBar + Ad/DNS)
        val navBarHeight = innerPadding.calculateBottomPadding()

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Global BackHandler for tab navigation
            BackHandler(enabled = true) {
                if (currentRoute == Screen.Home.route) {
                    // Always exit immediately if we are on Home
                    (context as? android.app.Activity)?.finish()
                } else if (tabHistory.size > 1) {
                    // Navigate back through unique history
                    // Remove current route to reveal the previous one
                    currentRoute?.let { tabHistory.remove(it) }
                    val prev = tabHistory.last()
                    
                    if (prev == Screen.Home.route) {
                        innerNavController.popBackStack()
                    } else {
                        innerNavController.navigate(prev) {
                            popUpTo(innerNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                } else {
                    // History is empty or only contains current non-home tab, move to Home
                    innerNavController.navigate(Screen.Home.route) {
                        popUpTo(innerNavController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            NavHost(
                navController = innerNavController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        // Content should always be above the bottom bar, 
                        // even when keyboard is open we want content to be scrollable above keyboard
                        bottom = max(navBarHeight, imeBottom)
                    )
            ) {
                // ... composables remain the same
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
                        navController = globalNavController,
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

            // Total Cost card as an overlay
            // We use a Box with constraints or density to ensure it sits exactly where we want
            // The trick is to use a single padding that accounts for both navBar and keyboard
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    // We calculate a base padding that is navBarHeight when keyboard is closed
                    // and tapers to 0 as keyboard opens, but imePadding() handles the push.
                    // To avoid the glitch, we subtract the current keyboard height from the navBar height,
                    // clamping it at 0.
                    .padding(bottom = max(0.dp, navBarHeight - imeBottom))
                    .imePadding()
            ) {
                AnimatedWaveBottomBar(
                    totalCost = viewModel.grandTotalCost,
                    strings = viewModel.strings,
                    isBlurred = false,
                    onClick = { showPremiumPopup = true }
                )
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

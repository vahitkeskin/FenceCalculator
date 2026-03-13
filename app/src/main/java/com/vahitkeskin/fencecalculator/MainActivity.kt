package com.vahitkeskin.fencecalculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.fencecalculator.ui.components.AnimatedSplashScreen
import com.vahitkeskin.fencecalculator.ui.screen.OnboardingScreen
import com.vahitkeskin.fencecalculator.ui.screen.AddEditCardScreen
import com.vahitkeskin.fencecalculator.ui.screen.HomeScreen
import com.vahitkeskin.fencecalculator.ui.screen.SettingsScreen
import com.vahitkeskin.fencecalculator.ui.screen.PersonalInfoScreen
import com.vahitkeskin.fencecalculator.ui.screen.AboutScreen
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        /*
        MobileAds.initialize(this) { status ->
            Log.d("MainActivity", "AdMob Başlatıldı: $status")
            com.vahitkeskin.fencecalculator.util.AdManager.loadInterstitialAd(this)
        }
        */
        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }
            val navController = rememberNavController()

            FenceCalculatorTheme(
                appTheme = viewModel.currentTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        AnimatedSplashScreen(onAnimationFinished = {
                            showSplash = false
                        })
                    } else if (!viewModel.isOnboardingCompleted) {
                        OnboardingScreen(
                            viewModel = viewModel,
                            onFinish = { viewModel.setOnboardingCompleted() }
                        )
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = "main"
                        ) {
                            composable("main") {
                                com.vahitkeskin.fencecalculator.ui.screen.MainScreen(
                                    viewModel = viewModel,
                                    globalNavController = navController
                                )
                            }
                            composable("add_edit_card/{cardId}") { backStackEntry ->
                                val cardId = backStackEntry.arguments?.getString("cardId")
                                val editId = if (cardId == "new") null else cardId
                                AddEditCardScreen(
                                    viewModel = viewModel,
                                    editCardId = editId,
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                            composable("settings_detail") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }
                            composable("personal_info") {
                                com.vahitkeskin.fencecalculator.ui.screen.PersonalInfoScreen(
                                    viewModel = viewModel,
                                    navController = navController
                                )
                            }
                            composable("about") {
                                AboutScreen(
                                    navController = navController,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
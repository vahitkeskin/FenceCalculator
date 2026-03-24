package com.vahitkeskin.fencecalculator

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
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
import com.vahitkeskin.fencecalculator.ui.fence3d.Fence3DScreen
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.model.FenceResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
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
                            @OptIn(ExperimentalMaterial3Api::class)
                            composable("fence_3d") {
                                // Robust parsing handling Turkish commas and prioritizing user inputs/custom cards
                                val rawLengthStr = viewModel.totalLengthInput.replace(',', '.')
                                val rawLength = rawLengthStr.toFloatOrNull() ?: 0f
                                val length = if (rawLength > 0f) rawLength else 100.0f
                                
                                val allItems = viewModel.orderedVisibleItems
                                val strings = viewModel.strings

                                // 1. Posts (Priority: Standard ID > Title Match > Default)
                                val posts = (allItems.find { it.id == "direk" }?.quantity
                                    ?: allItems.find { it.title.contains(strings.direkTitle, ignoreCase = true) }?.quantity
                                    ?: 32.0).toInt()

                                // 2. Struts (Priority: Standard ID > Title Match > Default)
                                val struts = (allItems.find { it.id == "payanda" }?.quantity
                                    ?: allItems.find { it.title.contains(strings.payandaTitle, ignoreCase = true) }?.quantity
                                    ?: 8.0).toInt()

                                // 3. Dimensions (Prioritize input strings, then falls back)
                                val height = viewModel.fenceHeightInput.replace(',', '.').toFloatOrNull() ?: 2.0f
                                val spacing = viewModel.poleSpacingInput.replace(',', '.').toFloatOrNull() ?: 3.5f
                                val meshEye = viewModel.meshEyeInput.replace(',', '.').toFloatOrNull() ?: 6.5f

                                val result = FenceResult(
                                    totalLandLength = length,
                                    postCount = posts,
                                    strutCount = struts,
                                    height = height,
                                    spacing = spacing,
                                    meshEye = meshEye
                                )
                                
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .systemBarsPadding()
                                ) {
                                    MeshBackground()
                                    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                                        CenterAlignedTopAppBar(
                                            title = { 
                                                Text("3D SIMULATION", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                                            },
                                            navigationIcon = {
                                                IconButton(onClick = { navController.popBackStack() }) {
                                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                                }
                                            },
                                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                                        )
                                        androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                                            Fence3DScreen(
                                                fenceResult = result,
                                                onDismiss = { navController.popBackStack() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
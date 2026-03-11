package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import androidx.compose.ui.platform.LocalContext
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.vahitkeskin.fencecalculator.R
import com.vahitkeskin.fencecalculator.util.NavigationUtils.safePopBackStack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: CalculatorViewModel, navController: NavController) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()
        
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { 
                    Text(viewModel.strings.settings, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = viewModel.strings.back)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            // SettingsSheetContent'i burada kullanıyoruz ama "Kaydet ve Kapat" butonunu gizleyebiliriz veya işlevini değiştirebiliriz.
            // Parametre olarak onDismiss'i boş geçiyoruz çünkü artık bir tab.
            SettingsSheetContent(viewModel = viewModel, onDismiss = {})
        }
    }
}

@AppPreviews
@Composable
fun SettingsScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager, context) }
    val navController = rememberNavController()
    
    FenceCalculatorTheme {
        SettingsScreen(viewModel = viewModel, navController = navController)
    }
}

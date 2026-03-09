package com.vahitkeskin.fencecalculator.ui.screen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.fencecalculator.ui.components.MeshBackground
import com.vahitkeskin.fencecalculator.ui.components.SwapLayoutResultRow
import com.vahitkeskin.fencecalculator.ui.viewmodel.CalculatorViewModel
import androidx.compose.ui.platform.LocalContext
import com.vahitkeskin.fencecalculator.ui.previews.AppPreviews
import com.vahitkeskin.fencecalculator.ui.theme.FenceCalculatorTheme
import com.vahitkeskin.fencecalculator.util.DataStoreManager
import androidx.compose.runtime.remember

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CalculationsScreen(viewModel: CalculatorViewModel) {
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary
    
    // Filtrelenmiş ve sıralanmış varsayılan kartlar (custom_ ile başlamayanlar)
    val defaultItems = viewModel.orderedVisibleItems.filter { !it.id.startsWith("custom_") }
    val groupedItems = defaultItems.groupBy { it.category }

    Box(modifier = Modifier.fillMaxSize()) {
        MeshBackground()
        
        Column(modifier = Modifier.fillMaxSize()) {
            CenterAlignedTopAppBar(
                title = { 
                    Text("HAZIR HESAPLAR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                overscrollEffect = null
            ) {
                groupedItems.forEach { (category, items) ->
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = category,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = primaryColor,
                                letterSpacing = 2.sp
                            )
                        }
                    }

                    items(items, key = { it.id }) { item ->
                        SwapLayoutResultRow(
                            item = item,
                            currentPriceInput = viewModel.getPriceString(item.id),
                            onPriceChange = { viewModel.onPriceChange(item.id, it) },
                            onPinToggle = { viewModel.togglePin(item.id) }
                        )
                    }
                }
                
                // Klavye açıldığında en alttaki içeriğin yukarı kaydırılabilmesi için spacer
                item { Spacer(modifier = Modifier.height(90.dp)) }
            }
        }
    }
}

@AppPreviews
@Composable
fun CalculationsScreenPreview() {
    val context = LocalContext.current
    val dataStoreManager = remember { DataStoreManager(context) }
    val viewModel = remember { CalculatorViewModel(dataStoreManager) }
    
    FenceCalculatorTheme {
        CalculationsScreen(viewModel = viewModel)
    }
}
